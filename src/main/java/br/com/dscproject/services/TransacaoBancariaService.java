package br.com.dscproject.services;

import br.com.dscproject.domain.InstituicaoFinanceiraUsuario;
import br.com.dscproject.domain.TransacaoBancaria;
import br.com.dscproject.domain.Usuario;
import br.com.dscproject.dto.TransacaoBancariaDTO;
import br.com.dscproject.enums.CategoriaRegistroFinanceiro;
import br.com.dscproject.enums.TipoRegistroFinanceiro;
import br.com.dscproject.repository.InstituicaoFinanceiraUsuarioRepository;
import br.com.dscproject.repository.TransacaoBancariaRepository;
import br.com.dscproject.repository.UsuarioRepository;
import br.com.dscproject.services.exceptions.DataIntegrityException;
import br.com.dscproject.services.exceptions.ObjectNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class TransacaoBancariaService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private InstituicaoFinanceiraUsuarioRepository instituicaoFinanceiraUsuarioRepository;

    @Autowired
    private TransacaoBancariaRepository transacaoBancariaRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private EntityManager entityManager;


    @Transactional
    public List<TransacaoBancariaDTO> buscarTodosPorUsuario() {
        String loginUsuarioToken = tokenService.validarToken(tokenService.recuperarToken(request));
        Usuario usuario = usuarioRepository.findByLogin(loginUsuarioToken);

        List<TransacaoBancariaDTO> transacaoBancariaDTOList = new ArrayList<>();

        List<TransacaoBancaria> transacaoBancariaList = new ArrayList<>();
        transacaoBancariaList = transacaoBancariaRepository.findByInstituicaoFinanceiraUsuarioUsuario_Id(usuario.getId());

        for (TransacaoBancaria transacaoBancaria : transacaoBancariaList) {
            TransacaoBancariaDTO dto = new TransacaoBancariaDTO();
            BeanUtils.copyProperties(transacaoBancaria, dto);

            dto.setTipoRegistroFinanceiro(transacaoBancaria.getTipoRegistroFinanceiro().getCodigo());
            dto.setCategoriaRegistroFinanceiro(transacaoBancaria.getCategoriaRegistroFinanceiro().getCodigo());

            //Seta a instituição financeira usuario e a instituicao do usuario
            dto.setInstituicaoFinanceiraUsuarioId(transacaoBancaria.getInstituicaoFinanceiraUsuario().getId());

            if(transacaoBancaria.getDtLancamento() != null) {
                dto.setDtLancamento(transacaoBancaria.getDtLancamento().toString());
            }

            transacaoBancariaDTOList.add(dto);
        }

        return transacaoBancariaDTOList;
    }

    public TransacaoBancaria buscarPorId(Long id) {
        Optional<TransacaoBancaria> obj = transacaoBancariaRepository.findById(id);
        return obj.orElseThrow(()-> new ObjectNotFoundException("Objeto não encontrado!" + "Id:" + id + ", Tipo: "  + TransacaoBancaria.class.getName()));
    }

    public TransacaoBancaria inserir(TransacaoBancariaDTO data) {
        String loginUsuarioToken = tokenService.validarToken(tokenService.recuperarToken(request));
        Usuario usuario = usuarioRepository.findByLogin(loginUsuarioToken);

        TransacaoBancaria transacaoBancaria  = new TransacaoBancaria();
        BeanUtils.copyProperties(data, transacaoBancaria);

        transacaoBancaria.setTipoRegistroFinanceiro(TipoRegistroFinanceiro.toEnum(data.getTipoRegistroFinanceiro()));
        transacaoBancaria.setCategoriaRegistroFinanceiro(CategoriaRegistroFinanceiro.toEnum(data.getCategoriaRegistroFinanceiro()));

        transacaoBancaria.setDtLancamento(Instant.now());

        Optional<InstituicaoFinanceiraUsuario> ifu  = this.instituicaoFinanceiraUsuarioRepository.findById(data.getInstituicaoFinanceiraUsuarioId());
        if(ifu.isEmpty()){
            throw new ObjectNotFoundException("Não existe uma instituição financeira vinculada ao usuário com o id " + data.getInstituicaoFinanceiraUsuarioId() + ".");
        }

        InstituicaoFinanceiraUsuario instituicaoFinanceiraUsuario = ifu.get();

        transacaoBancaria.setInstituicaoFinanceiraUsuario(instituicaoFinanceiraUsuario);

        //Seta Auditoria
        transacaoBancaria.setCriadoPor(usuario.getLogin());

        //Por fim, gravo a transacaoBancaria
        transacaoBancariaRepository.saveAndFlush(transacaoBancaria);

        //Retorno a transacaoBancaria
        return transacaoBancaria;
    }

    @Transactional
    public void editar(TransacaoBancariaDTO data) throws ObjectNotFoundException {

        //Pega o usuário logado
        String loginUsuarioToken = tokenService.validarToken(tokenService.recuperarToken(request));
        Usuario usuario = usuarioRepository.findByLogin(loginUsuarioToken);

        //Busca o registro Financeiro do banco e altero seus campos que não são chaves
        TransacaoBancaria transacaoBancariaDB = this.buscarPorId(data.getId());

        transacaoBancariaDB.setDescricao(data.getDescricao());
        transacaoBancariaDB.setValor(data.getValor());
        transacaoBancariaDB.setTipoRegistroFinanceiro(TipoRegistroFinanceiro.toEnum(data.getTipoRegistroFinanceiro()));
        transacaoBancariaDB.setCategoriaRegistroFinanceiro(CategoriaRegistroFinanceiro.toEnum(data.getCategoriaRegistroFinanceiro()));
        transacaoBancariaDB.setAlteradoPor(usuario.getLogin()); //Auditoria

        //Altero Instituição Financeira do Usuario
        Optional<InstituicaoFinanceiraUsuario> ifuDB  = this.instituicaoFinanceiraUsuarioRepository.findById(data.getInstituicaoFinanceiraUsuarioId());
        if(ifuDB.isEmpty()){
            throw new ObjectNotFoundException("Não existe uma instituição financeira vinculada ao usuário com o id " + data.getInstituicaoFinanceiraUsuarioId() + ".");
        }
        InstituicaoFinanceiraUsuario instituicaoFinanceiraUsuario = ifuDB.get();
        transacaoBancariaDB.setInstituicaoFinanceiraUsuario(instituicaoFinanceiraUsuario);

        //Salvo a transacaoBancaria
        transacaoBancariaRepository.saveAndFlush(transacaoBancariaDB);
    }

    public void excluir(Long id) throws ObjectNotFoundException {
        try {
            transacaoBancariaRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityException("Não foi possível excluir este registro pois existem registros vinculados a ele.");
        }
    }
}


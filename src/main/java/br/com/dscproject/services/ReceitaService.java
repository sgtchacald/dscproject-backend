package br.com.dscproject.services;

import br.com.dscproject.domain.Receita;
import br.com.dscproject.domain.InstituicaoFinanceiraUsuario;
import br.com.dscproject.domain.Usuario;
import br.com.dscproject.dto.ReceitaDTO;
import br.com.dscproject.repository.*;
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
import java.util.*;

@Service
@Slf4j
public class ReceitaService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private InstituicaoFinanceiraUsuarioRepository instituicaoFinanceiraUsuarioRepository;

    @Autowired
    private ReceitaRepository receitaRepository;

    @Autowired
    private ReceitaRepositoryCustom receitaRepositoryCustom;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private EntityManager entityManager;


    @Transactional
    public List<ReceitaDTO> buscarTodosPorUsuario() {
        String loginUsuarioToken = tokenService.validarToken(tokenService.recuperarToken(request));
        Usuario usuario = usuarioRepository.findByLogin(loginUsuarioToken);

        List<ReceitaDTO> receitaDTOList = new ArrayList<>();

        List<Receita> receitaList = new ArrayList<>();
        receitaList = receitaRepositoryCustom.buscarReceitaPorUsuario(usuario);

        for (Receita receita : receitaList) {
            ReceitaDTO dto = new ReceitaDTO();
            BeanUtils.copyProperties(receita, dto);

            //Seta a instituição financeira usuario e a instituicao do usuario
            dto.setInstituicaoFinanceiraUsuarioId(receita.getInstituicaoFinanceiraUsuario().getId());
            dto.setInstituicaoFinanceiraId(receita.getInstituicaoFinanceiraUsuario().getInstituicaoFinanceira().getId());


            if(receita.getDtLancamento() != null) {
                dto.setDtLancamento(receita.getDtLancamento().toString());
            }

            receitaDTOList.add(dto);
        }

        return receitaDTOList;
    }

    public Receita buscarPorId(Long id) {
        Optional<Receita> obj = receitaRepository.findById(id);
        return obj.orElseThrow(()-> new ObjectNotFoundException("Objeto não encontrado!" + "Id:" + id + ", Tipo: "  + Receita.class.getName()));
    }

    public Receita inserir(ReceitaDTO data) {
        String loginUsuarioToken = tokenService.validarToken(tokenService.recuperarToken(request));
        Usuario usuario = usuarioRepository.findByLogin(loginUsuarioToken);

        Receita receita  = new Receita();
        BeanUtils.copyProperties(data, receita);

        receita.setDtLancamento(Instant.now());

        Optional<InstituicaoFinanceiraUsuario> ifu  = this.instituicaoFinanceiraUsuarioRepository.findById(data.getInstituicaoFinanceiraUsuarioId());
        if(ifu.isEmpty()){
            throw new ObjectNotFoundException("Não existe uma instituição financeira vinculada ao usuário com o id " + data.getInstituicaoFinanceiraUsuarioId() + ".");
        }

        InstituicaoFinanceiraUsuario instituicaoFinanceiraUsuario = ifu.get();

        receita.setInstituicaoFinanceiraUsuario(instituicaoFinanceiraUsuario);

        //Seta Auditoria
        receita.setCriadoPor(usuario.getLogin());

        //Por fim, gravo a receita
        receitaRepository.saveAndFlush(receita);

        //Retorno a receita
        return receita;
    }

    @Transactional
    public void editar(ReceitaDTO data) throws ObjectNotFoundException {

        //Pega o usuário logado
        String loginUsuarioToken = tokenService.validarToken(tokenService.recuperarToken(request));
        Usuario usuario = usuarioRepository.findByLogin(loginUsuarioToken);

        //Busca o registro Financeiro do banco e altero seus campos que não são chaves
        Receita receitaBanco = this.buscarPorId(data.getId());

        receitaBanco.setNome(data.getNome());
        receitaBanco.setDescricao(data.getDescricao());
        receitaBanco.setValor(data.getValor());
        receitaBanco.setTipoRegistroFinanceiro(data.getTipoRegistroFinanceiro());
        receitaBanco.setCategoriaRegistroFinanceiro(data.getCategoriaRegistroFinanceiro());
        receitaBanco.setAlteradoPor(usuario.getLogin()); //Auditoria

        //Altero Instituição Financeira do Usuario
        Optional<InstituicaoFinanceiraUsuario> ifuBanco  = this.instituicaoFinanceiraUsuarioRepository.findById(data.getInstituicaoFinanceiraUsuarioId());
        if(ifuBanco.isEmpty()){
            throw new ObjectNotFoundException("Não existe uma instituição financeira vinculada ao usuário com o id " + data.getInstituicaoFinanceiraUsuarioId() + ".");
        }
        InstituicaoFinanceiraUsuario instituicaoFinanceiraUsuario = ifuBanco.get();
        receitaBanco.setInstituicaoFinanceiraUsuario(instituicaoFinanceiraUsuario);

        //Salvo a receita
        receitaRepository.saveAndFlush(receitaBanco);
    }

    public void excluir(Long id) throws ObjectNotFoundException {
        try {
            receitaRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityException("Não foi possível excluir este registro pois existem registros vinculados a ele.");
        }
    }
}


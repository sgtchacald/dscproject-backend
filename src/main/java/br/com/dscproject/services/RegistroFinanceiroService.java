package br.com.dscproject.services;

import br.com.dscproject.domain.InstituicaoFinanceira;
import br.com.dscproject.domain.InstituicaoFinanceiraUsuario;
import br.com.dscproject.domain.RegistroFinanceiro;
import br.com.dscproject.domain.Usuario;
import br.com.dscproject.dto.InstituicaoFinanceiraUsuarioDTO;
import br.com.dscproject.dto.RegistroFinanceiroDTO;
import br.com.dscproject.repository.InstituicaoFinanceiraUsuarioRepository;
import br.com.dscproject.repository.RegistroFinanceiroRepository;
import br.com.dscproject.repository.RegistroFinanceiroRepositoryCustom;
import br.com.dscproject.repository.UsuarioRepository;
import br.com.dscproject.services.exceptions.DataIntegrityException;
import br.com.dscproject.services.exceptions.ObjectNotFoundException;
import br.com.dscproject.utils.DateUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.buf.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
@Slf4j
public class RegistroFinanceiroService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private InstituicaoFinanceiraUsuarioRepository instituicaoFinanceiraUsuarioRepository;

    @Autowired
    private RegistroFinanceiroRepository registroFinanceiroRepository;

    @Autowired
    private RegistroFinanceiroRepositoryCustom registroFinanceiroRepositoryCustom;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private HttpServletRequest request;

    @Transactional
    public List<RegistroFinanceiroDTO> buscarTodosPorUsuario() {
        String loginUsuarioToken = tokenService.validarToken(tokenService.recuperarToken(request));
        Usuario usuario = usuarioRepository.findByLogin(loginUsuarioToken);

        List<RegistroFinanceiroDTO> registroFinanceiroDTOList = new ArrayList<>();

        List<RegistroFinanceiro> registroFinanceiroList = new ArrayList<>();
        registroFinanceiroList = registroFinanceiroRepositoryCustom.buscarRegistroFinanceiroPorUsuario(usuario);

        for (RegistroFinanceiro registroFinanceiro : registroFinanceiroList) {
            RegistroFinanceiroDTO dto = new RegistroFinanceiroDTO();
            BeanUtils.copyProperties(registroFinanceiro, dto);
            dto.setInstituicaoFinanceiraUsuarioId(registroFinanceiro.getInstituicaoFinanceiraUsuario().getId());

            if(registroFinanceiro.getDtLancamento() != null) {
                dto.setDtLancamento(registroFinanceiro.getDtLancamento().toString());
            }

            if(registroFinanceiro.getDtVencimento() != null) {
                dto.setDtVencimento(registroFinanceiro.getDtVencimento().toString());
            }

            Set<Long> usuariosResponsaveis = new HashSet<Long>();
            if(registroFinanceiro.getId() != null) {
                usuariosResponsaveis = registroFinanceiroRepository.findUsuariosByRegistroFinanceiroId(registroFinanceiro.getId());
            }

            if(!usuariosResponsaveis.isEmpty()){
                for (Long usuarioId : usuariosResponsaveis) {
                  dto.getUsuariosResponsaveis().add(usuarioId);
                }
            }

            registroFinanceiroDTOList.add(dto);
        }

        return registroFinanceiroDTOList;
    }

    public RegistroFinanceiro buscarPorId(Long id) {
        Optional<RegistroFinanceiro> obj = registroFinanceiroRepository.findById(id);
        return obj.orElseThrow(
                ()-> new ObjectNotFoundException(
                        "Objeto não encontrado!" + "Id:" + id + ", Tipo: "  + RegistroFinanceiro.class.getName()
                )
        );
    }

    @Transactional
    public RegistroFinanceiro inserir(RegistroFinanceiroDTO data) {
        RegistroFinanceiro registroFinanceiro  = new RegistroFinanceiro();
        BeanUtils.copyProperties(data, registroFinanceiro);

        registroFinanceiro.setDtLancamento(Instant.now());

        if(data.getDtVencimento() != null) {
            registroFinanceiro.setDtVencimento(DateUtils.retornaLocalDate(data.getDtVencimento(), "dd/MM/yyyy"));
        }

        Optional<InstituicaoFinanceiraUsuario> ifu  = this.instituicaoFinanceiraUsuarioRepository.findById(data.getInstituicaoFinanceiraUsuarioId());
        if(ifu.isEmpty()){
            throw new ObjectNotFoundException("Não existe uma instituição financeira vinculada ao usuário com o id " + data.getInstituicaoFinanceiraUsuarioId() + ".");
        }

        InstituicaoFinanceiraUsuario instituicaoFinanceiraUsuario = ifu.get();

        registroFinanceiro.setInstituicaoFinanceiraUsuario(instituicaoFinanceiraUsuario);

        registroFinanceiro.setStatusPagamento(data.getStatusPagamento());

        Set<Usuario> usuarios = new HashSet<Usuario>();

        for(Long usuarioId : data.getUsuariosResponsaveis()){
            Optional<Usuario> usuario = usuarioRepository.findById(usuarioId);
            if(usuario.isEmpty()){
                throw new ObjectNotFoundException("Não foi possível encontrar o usuario com o id  " + usuarioId + ".");
            }
            usuarios.add(usuario.get());
        }

        if(!usuarios.isEmpty()){
            registroFinanceiro.getUsuariosResponsaveis().addAll(usuarios);
        }

        //registroFinanceiroRepository.save(registroFinanceiro);

        return registroFinanceiro;
    }

    public RegistroFinanceiro editar(RegistroFinanceiroDTO data) throws ObjectNotFoundException {

        RegistroFinanceiro registroFinanceiroBanco = this.buscarPorId(data.getId());

        registroFinanceiroBanco.setDescricao(data.getDescricao());
        registroFinanceiroBanco.setValor(data.getValor());
        registroFinanceiroBanco.setTipoRegistroFinanceiro(data.getTipoRegistroFinanceiro());
        registroFinanceiroBanco.setCategoriaRegistroFinanceiro(data.getCategoriaRegistroFinanceiro());

        if(data.getDtVencimento() != null) {
            registroFinanceiroBanco.setDtVencimento(DateUtils.retornaLocalDate(data.getDtVencimento(), "dd/MM/yyyy"));
        }

        Optional<InstituicaoFinanceiraUsuario> ifu  = this.instituicaoFinanceiraUsuarioRepository.findById(data.getInstituicaoFinanceiraUsuarioId());
        if(ifu.isEmpty()){
            throw new ObjectNotFoundException("Não existe uma instituição financeira vinculada ao usuário com o id " + data.getInstituicaoFinanceiraUsuarioId() + ".");
        }

        InstituicaoFinanceiraUsuario instituicaoFinanceiraUsuario = ifu.get();

        registroFinanceiroBanco.setInstituicaoFinanceiraUsuario(instituicaoFinanceiraUsuario);


        Set<Usuario> usuarios = new HashSet<Usuario>();
        for(Long usuarioId : data.getUsuariosResponsaveis()){
            Optional<Usuario> usuario = usuarioRepository.findById(usuarioId);
            if(usuario.isEmpty()){
                throw new ObjectNotFoundException("Não foi possível encontrar o usuario com o id  " + usuarioId + ".");
            }
            usuario.ifPresent(usuarios::add);
        }

        if(!usuarios.isEmpty()){
            registroFinanceiroBanco.setUsuariosResponsaveis(usuarios);
        }

        return registroFinanceiroRepository.save(registroFinanceiroBanco);
    }

    public void excluir(Long id) throws ObjectNotFoundException {
        this.buscarPorId(id);
        try {
            registroFinanceiroRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityException("Não foi possível excluir este registro pois existem registros vinculados a ele.");
        }
    }
}


package br.com.dscproject.services;

import br.com.dscproject.domain.*;
import br.com.dscproject.dto.RegistroFinanceiroDTO;
import br.com.dscproject.dto.UsuarioResponsavelDTO;
import br.com.dscproject.repository.*;
import br.com.dscproject.services.exceptions.DataIntegrityException;
import br.com.dscproject.services.exceptions.ObjectNotFoundException;
import br.com.dscproject.utils.DateUtils;
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
import java.util.stream.Collectors;

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
    private RegistroFinanceiroUsuarioRepository registroFinanceiroUsuarioRepository;

    @Autowired
    private RegistroFinanceiroRepositoryCustom registroFinanceiroRepositoryCustom;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private EntityManager entityManager;


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

            //Seta a instituição financeira usuario e a instituicao do usuario
            dto.setInstituicaoFinanceiraUsuarioId(registroFinanceiro.getInstituicaoFinanceiraUsuario().getId());
            dto.setInstituicaoFinanceiraId(registroFinanceiro.getInstituicaoFinanceiraUsuario().getInstituicaoFinanceira().getId());


            if(registroFinanceiro.getDtLancamento() != null) {
                dto.setDtLancamento(registroFinanceiro.getDtLancamento().toString());
            }

            if(registroFinanceiro.getDtVencimento() != null) {
                dto.setDtVencimento(registroFinanceiro.getDtVencimento().toString());
            }

            Set<UsuarioResponsavelDTO> usuariosResponsaveis = new HashSet<UsuarioResponsavelDTO>();
            if(registroFinanceiro.getId() != null) {
                usuariosResponsaveis = registroFinanceiroRepository.findUsuariosByRegistroFinanceiroId(registroFinanceiro.getId());
            }

            if(!usuariosResponsaveis.isEmpty()){
                for (UsuarioResponsavelDTO usuarioResponsavel : usuariosResponsaveis) {
                  dto.getUsuariosResponsaveis().add(usuarioResponsavel);
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

    public RegistroFinanceiro inserir(RegistroFinanceiroDTO data) {
        String loginUsuarioToken = tokenService.validarToken(tokenService.recuperarToken(request));
        Usuario usuario = usuarioRepository.findByLogin(loginUsuarioToken);

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


        //Busca dados para fazer a divisão da despesa
        Set<UsuarioResponsavelDTO> usuariosResponsaveisData = new HashSet<>(Set.of()); //utilizo um [Set] para não haver registros repetidos
        usuariosResponsaveisData.addAll(data.getUsuariosResponsaveis());

        //Transformo em um list para ficar melhor de manipular
        List<UsuarioResponsavelDTO> usuariosResponsaveisDataList = new ArrayList<UsuarioResponsavelDTO>(usuariosResponsaveisData);

        //Busco os usuarios no banco para a divisão de gastos.
        List<Usuario> usuarioList = usuariosResponsaveisDataList
            .stream()
            .map(u -> usuarioRepository
                    .findById(u.getId())
                    .orElseThrow(() -> new ObjectNotFoundException("Não foi possível encontrar o usuário com o id " + usuario.getId() + "."))
                )
            .toList();

        //Adiciono os usuarios responsáveis na lista
        //registroFinanceiro.setUsuariosResponsaveis(usuarioList);

        //Seta Auditoria
        registroFinanceiro.setCriadoPor(usuario.getLogin());

        //Por fim, gravo o registro financeiro
        registroFinanceiroRepository.saveAndFlush(registroFinanceiro);

        //Se houver usuario para divisão do registro financeiro, insere na tabela associativa REGISTRO_FINANCEIRO_USUARIO
        if(!usuarioList.isEmpty()) {
            for (Usuario u : usuarioList) {
                RegistroFinanceiroUsuario registroFinanceiroUsuario = new RegistroFinanceiroUsuario();
                registroFinanceiroUsuario.setRegistroFinanceiro(registroFinanceiro);
                registroFinanceiroUsuario.setUsuario(u);
                registroFinanceiroUsuario.setCriadoPor(usuario.getLogin());
                registroFinanceiroUsuarioRepository.saveAndFlush(registroFinanceiroUsuario);
            }
        }

        //Retorno o registro financeiro
        return registroFinanceiro;
    }

    @Transactional
    public void editar(RegistroFinanceiroDTO data) throws ObjectNotFoundException {

        //Pega o usuário logado
        String loginUsuarioToken = tokenService.validarToken(tokenService.recuperarToken(request));
        Usuario usuario = usuarioRepository.findByLogin(loginUsuarioToken);

        //Busca o registro Financeiro do banco e altero seus campos que não são chaves
        RegistroFinanceiro registroFinanceiroBanco = this.buscarPorId(data.getId());

        registroFinanceiroBanco.setDescricao(data.getDescricao());
        registroFinanceiroBanco.setValor(data.getValor());
        registroFinanceiroBanco.setTipoRegistroFinanceiro(data.getTipoRegistroFinanceiro());
        registroFinanceiroBanco.setCategoriaRegistroFinanceiro(data.getCategoriaRegistroFinanceiro());
        registroFinanceiroBanco.setAlteradoPor(usuario.getLogin()); //Auditoria

        if(data.getDtVencimento() != null) {
            registroFinanceiroBanco.setDtVencimento(DateUtils.parseData(data.getDtVencimento()));
        }

        //Altero Instituição Financeira do Usuario
        Optional<InstituicaoFinanceiraUsuario> ifuBanco  = this.instituicaoFinanceiraUsuarioRepository.findById(data.getInstituicaoFinanceiraUsuarioId());
        if(ifuBanco.isEmpty()){
            throw new ObjectNotFoundException("Não existe uma instituição financeira vinculada ao usuário com o id " + data.getInstituicaoFinanceiraUsuarioId() + ".");
        }
        InstituicaoFinanceiraUsuario instituicaoFinanceiraUsuario = ifuBanco.get();
        registroFinanceiroBanco.setInstituicaoFinanceiraUsuario(instituicaoFinanceiraUsuario);

        //Busca dados para fazer a divisão da despesa
        Set<UsuarioResponsavelDTO> usuariosResponsaveisData = new HashSet<>(Set.of()); //utilizo um [Set] para não haver registros repetidos
        usuariosResponsaveisData.addAll(data.getUsuariosResponsaveis());
        List<UsuarioResponsavelDTO> usuariosResponsaveisDataList = new ArrayList<UsuarioResponsavelDTO>(usuariosResponsaveisData);

        //Busco os usuarios no banco para a divisão de gastos.
        List<Usuario> usuarioList = usuariosResponsaveisDataList
            .stream()
            .map(u -> usuarioRepository
                    .findById(u.getId())
                    .orElseThrow(() -> new ObjectNotFoundException("Não foi possível encontrar o usuário com o id " + usuario.getId() + "."))
            )
            .toList();

        //Se houver usuario para divisão do registro financeiro, insere na tabela associativa REGISTRO_FINANCEIRO_USUARIO
        List<RegistroFinanceiroUsuario> registrosFinanceirosTelaList = new ArrayList<RegistroFinanceiroUsuario>();
        if(!usuarioList.isEmpty()) {
            for (Usuario u : usuarioList) {
                RegistroFinanceiroUsuario rfu = registroFinanceiroUsuarioRepository.findByUsuarioAndRegistroFinanceiro(u,registroFinanceiroBanco);

                RegistroFinanceiroUsuario registroFinanceiroUsuario = new RegistroFinanceiroUsuario();
                registroFinanceiroUsuario.setId((rfu != null) ? rfu.getId() : null);
                registroFinanceiroUsuario.setRegistroFinanceiro(registroFinanceiroBanco);
                registroFinanceiroUsuario.setUsuario(u);
                registroFinanceiroUsuario.setCriadoPor(usuario.getLogin());
                registrosFinanceirosTelaList.add(registroFinanceiroUsuario);
            }
        }

        List<RegistroFinanceiroUsuario> registrosFinanceirosUsuarioBancoList = new ArrayList<>();
        registrosFinanceirosUsuarioBancoList =  registroFinanceiroUsuarioRepository.findByRegistroFinanceiro(Optional.of(registroFinanceiroBanco));
        List<RegistroFinanceiroUsuario> registrosFinanceirosUsuarioAExcluir = new ArrayList<>();

        // Cria Lista de registrosFinanceirosUsuarios que serão excluídos
        for (RegistroFinanceiroUsuario registroFinanceiroUsuarioBanco : registrosFinanceirosUsuarioBancoList) {
            boolean existeNaTela = false;

            for (RegistroFinanceiroUsuario registroFinanceiroTela : registrosFinanceirosTelaList) {
                if (registroFinanceiroTela.getId().equals(registroFinanceiroUsuarioBanco.getId())) {
                    existeNaTela = true;
                    break; // Sai do loop se encontrar o ID
                }
            }

            if (!existeNaTela) {
                registrosFinanceirosUsuarioAExcluir.add(registroFinanceiroUsuarioBanco);
            }
        }

        //Por fim, gravo o registro financeiro
        registroFinanceiroRepository.saveAndFlush(registroFinanceiroBanco);

        // Agora você pode salvar os objetos que precisam ser salvos
        if(!registrosFinanceirosTelaList.isEmpty()){
           registroFinanceiroUsuarioRepository.saveAllAndFlush(registrosFinanceirosTelaList);
        }

        // E excluir os objetos que precisam ser excluídos
        if(!registrosFinanceirosUsuarioAExcluir.isEmpty()){
            for (RegistroFinanceiroUsuario rfue : registrosFinanceirosUsuarioAExcluir){
                if(rfue.getRegistroFinanceiro().getId() != null && rfue.getUsuario().getId() != null && rfue.getId() != null){
                    registroFinanceiroUsuarioRepository.deleteById(rfue.getId());
                }
            }
        }

    }

    public void excluir(Long id) throws ObjectNotFoundException {
        Optional<RegistroFinanceiro> registroFinanceiro = registroFinanceiroRepository.findById(id);


        List<RegistroFinanceiroUsuario> registrosFinanceirosUsuarioList = new ArrayList<>();

        if(registroFinanceiro.isPresent()){
            registrosFinanceirosUsuarioList = registroFinanceiroUsuarioRepository.findByRegistroFinanceiro(registroFinanceiro);
        }

        try {
            //exclui os filhos
            if(!registrosFinanceirosUsuarioList.isEmpty()){
                for(RegistroFinanceiroUsuario refiu : registrosFinanceirosUsuarioList){
                    registroFinanceiroUsuarioRepository.deleteById(refiu.getId());
                }
            }

            //exclui o pai
            registroFinanceiroRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityException("Não foi possível excluir este registro pois existem registros vinculados a ele.");
        }
    }
}


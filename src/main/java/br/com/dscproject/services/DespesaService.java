package br.com.dscproject.services;

import br.com.dscproject.domain.*;
import br.com.dscproject.dto.DespesaDTO;
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

@Service
@Slf4j
public class DespesaService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private InstituicaoFinanceiraUsuarioRepository instituicaoFinanceiraUsuarioRepository;

    @Autowired
    private DespesaRepository despesaRepository;

    @Autowired
    private DespesaUsuarioRepository despesaUsuarioRepository;

    @Autowired
    private DespesaRepositoryCustom despesaRepositoryCustom;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private EntityManager entityManager;


    @Transactional
    public List<DespesaDTO> buscarTodosPorUsuario() {
        String loginUsuarioToken = tokenService.validarToken(tokenService.recuperarToken(request));
        Usuario usuario = usuarioRepository.findByLogin(loginUsuarioToken);

        List<DespesaDTO> despesaDTOList = new ArrayList<>();

        List<Despesa> despesaList = new ArrayList<>();
        despesaList = despesaRepositoryCustom.buscarDespesaPorUsuario(usuario);

        for (Despesa despesa : despesaList) {
            DespesaDTO dto = new DespesaDTO();
            BeanUtils.copyProperties(despesa, dto);

            //Seta a instituição financeira usuario e a instituicao do usuario
            dto.setInstituicaoFinanceiraUsuarioId(despesa.getInstituicaoFinanceiraUsuario().getId());
            dto.setInstituicaoFinanceiraId(despesa.getInstituicaoFinanceiraUsuario().getInstituicaoFinanceira().getId());


            if(despesa.getDtLancamento() != null) {
                dto.setDtLancamento(despesa.getDtLancamento().toString());
            }

            if(despesa.getDtVencimento() != null) {
                dto.setDtVencimento(despesa.getDtVencimento().toString());
            }

            Set<UsuarioResponsavelDTO> usuariosResponsaveis = new HashSet<UsuarioResponsavelDTO>();
            if(despesa.getId() != null) {
                usuariosResponsaveis = despesaRepository.findUsuariosByDespesaId(despesa.getId());
            }

            if(!usuariosResponsaveis.isEmpty()){
                for (UsuarioResponsavelDTO usuarioResponsavel : usuariosResponsaveis) {
                  dto.getUsuariosResponsaveis().add(usuarioResponsavel);
                }
            }


            despesaDTOList.add(dto);
        }

        return despesaDTOList;
    }

    public Despesa buscarPorId(Long id) {
        Optional<Despesa> obj = despesaRepository.findById(id);
        return obj.orElseThrow(
                ()-> new ObjectNotFoundException(
                        "Objeto não encontrado!" + "Id:" + id + ", Tipo: "  + Despesa.class.getName()
                )
        );
    }

    public Despesa inserir(DespesaDTO data) {
        String loginUsuarioToken = tokenService.validarToken(tokenService.recuperarToken(request));
        Usuario usuario = usuarioRepository.findByLogin(loginUsuarioToken);

        Despesa despesa  = new Despesa();
        BeanUtils.copyProperties(data, despesa);

        despesa.setDtLancamento(Instant.now());

        if(data.getDtVencimento() != null) {
            despesa.setDtVencimento(DateUtils.retornaLocalDate(data.getDtVencimento(), "dd/MM/yyyy"));
        }

        Optional<InstituicaoFinanceiraUsuario> ifu  = this.instituicaoFinanceiraUsuarioRepository.findById(data.getInstituicaoFinanceiraUsuarioId());
        if(ifu.isEmpty()){
            throw new ObjectNotFoundException("Não existe uma instituição financeira vinculada ao usuário com o id " + data.getInstituicaoFinanceiraUsuarioId() + ".");
        }

        InstituicaoFinanceiraUsuario instituicaoFinanceiraUsuario = ifu.get();

        despesa.setInstituicaoFinanceiraUsuario(instituicaoFinanceiraUsuario);

        despesa.setStatusPagamento(data.getStatusPagamento());


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
        //despesa.setUsuariosResponsaveis(usuarioList);

        //Seta Auditoria
        despesa.setCriadoPor(usuario.getLogin());

        //Por fim, gravo a despesa
        despesaRepository.saveAndFlush(despesa);

        //Se houver usuario para divisão da despesa, insere na tabela associativa REGISTRO_FINANCEIRO_USUARIO
        if(!usuarioList.isEmpty()) {
            for (Usuario u : usuarioList) {
                DespesaUsuario despesaUsuario = new DespesaUsuario();
                despesaUsuario.setDespesa(despesa);
                despesaUsuario.setUsuario(u);
                despesaUsuario.setCriadoPor(usuario.getLogin());
                despesaUsuarioRepository.saveAndFlush(despesaUsuario);
            }
        }

        //Retorno a despesa
        return despesa;
    }

    @Transactional
    public void editar(DespesaDTO data) throws ObjectNotFoundException {

        //Pega o usuário logado
        String loginUsuarioToken = tokenService.validarToken(tokenService.recuperarToken(request));
        Usuario usuario = usuarioRepository.findByLogin(loginUsuarioToken);

        //Busca o registro Financeiro do banco e altero seus campos que não são chaves
        Despesa despesaBanco = this.buscarPorId(data.getId());

        despesaBanco.setNome(data.getNome());
        despesaBanco.setDescricao(data.getDescricao());
        despesaBanco.setValor(data.getValor());
        despesaBanco.setTipoRegistroFinanceiro(data.getTipoRegistroFinanceiro());
        despesaBanco.setCategoriaRegistroFinanceiro(data.getCategoriaRegistroFinanceiro());
        despesaBanco.setAlteradoPor(usuario.getLogin()); //Auditoria

        if(data.getDtVencimento() != null) {
            despesaBanco.setDtVencimento(DateUtils.parseData(data.getDtVencimento()));
        }

        //Altero Instituição Financeira do Usuario
        Optional<InstituicaoFinanceiraUsuario> ifuBanco  = this.instituicaoFinanceiraUsuarioRepository.findById(data.getInstituicaoFinanceiraUsuarioId());
        if(ifuBanco.isEmpty()){
            throw new ObjectNotFoundException("Não existe uma instituição financeira vinculada ao usuário com o id " + data.getInstituicaoFinanceiraUsuarioId() + ".");
        }
        InstituicaoFinanceiraUsuario instituicaoFinanceiraUsuario = ifuBanco.get();
        despesaBanco.setInstituicaoFinanceiraUsuario(instituicaoFinanceiraUsuario);

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

        //Se houver usuario para divisão da despesa, insere na tabela associativa REGISTRO_FINANCEIRO_USUARIO
        List<DespesaUsuario> registrosFinanceirosTelaList = new ArrayList<DespesaUsuario>();
        if(!usuarioList.isEmpty()) {
            for (Usuario u : usuarioList) {
                DespesaUsuario rfu = despesaUsuarioRepository.findByUsuarioAndDespesa(u,despesaBanco);

                DespesaUsuario despesaUsuario = new DespesaUsuario();
                despesaUsuario.setId((rfu != null) ? rfu.getId() : null);
                despesaUsuario.setDespesa(despesaBanco);
                despesaUsuario.setUsuario(u);
                despesaUsuario.setCriadoPor(usuario.getLogin());
                registrosFinanceirosTelaList.add(despesaUsuario);
            }
        }

        List<DespesaUsuario> registrosFinanceirosUsuarioBancoList = new ArrayList<>();
        registrosFinanceirosUsuarioBancoList =  despesaUsuarioRepository.findByDespesa(Optional.of(despesaBanco));
        List<DespesaUsuario> registrosFinanceirosUsuarioAExcluir = new ArrayList<>();

        // Cria Lista de registrosFinanceirosUsuarios que serão excluídos
        for (DespesaUsuario despesaUsuarioBanco : registrosFinanceirosUsuarioBancoList) {
            boolean existeNaTela = false;

            for (DespesaUsuario despesaTela : registrosFinanceirosTelaList) {
                if (despesaTela.getId().equals(despesaUsuarioBanco.getId())) {
                    existeNaTela = true;
                    break; // Sai do loop se encontrar o ID
                }
            }

            if (!existeNaTela) {
                registrosFinanceirosUsuarioAExcluir.add(despesaUsuarioBanco);
            }
        }

        //Por fim, gravo a despesa
        despesaRepository.saveAndFlush(despesaBanco);

        // Agora você pode salvar os objetos que precisam ser salvos
        if(!registrosFinanceirosTelaList.isEmpty()){
           despesaUsuarioRepository.saveAllAndFlush(registrosFinanceirosTelaList);
        }

        // E excluir os objetos que precisam ser excluídos
        if(!registrosFinanceirosUsuarioAExcluir.isEmpty()){
            for (DespesaUsuario du : registrosFinanceirosUsuarioAExcluir){
                if(du.getDespesa().getId() != null && du.getUsuario().getId() != null && du.getId() != null){
                    despesaUsuarioRepository.deleteById(du.getId());
                }
            }
        }

    }

    public void excluir(Long id) throws ObjectNotFoundException {
        Optional<Despesa> despesa = despesaRepository.findById(id);


        List<DespesaUsuario> registrosFinanceirosUsuarioList = new ArrayList<>();

        if(despesa.isPresent()){
            registrosFinanceirosUsuarioList = despesaUsuarioRepository.findByDespesa(despesa);
        }

        try {
            //exclui os filhos
            if(!registrosFinanceirosUsuarioList.isEmpty()){
                for(DespesaUsuario du : registrosFinanceirosUsuarioList){
                    despesaUsuarioRepository.deleteById(du.getId());
                }
            }

            //exclui o pai
            despesaRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityException("Não foi possível excluir este registro pois existem registros vinculados a ele.");
        }
    }
}


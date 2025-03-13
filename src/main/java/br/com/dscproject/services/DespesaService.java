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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

import static java.lang.Integer.valueOf;

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
        despesaList = despesaRepository.findDespesasByUsuarioId(usuario.getId());

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

            if(despesa.isExisteParcela()){
                dto.setValor(despesa.getValorTotalADividir());
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
                .map(u -> {
                    Usuario uStream = usuarioRepository
                            .findById(u.getId())
                            .orElseThrow(() -> new ObjectNotFoundException("Não foi possível encontrar o usuário com o id " + u.getId() + "."));

                    uStream.setValorDividido(u.getValorDividido());

                    return uStream;
                })
                .toList();

        //Adiciono os usuarios responsáveis na lista
        //despesa.setUsuariosResponsaveis(usuarioList);

        //Seta Auditoria
        despesa.setCriadoPor(usuario.getLogin());

        BigDecimal valorParcela = new BigDecimal("0.00");

        if (despesa.isExisteParcela()) {
            valorParcela = despesa.getValorParcelado().divide(new BigDecimal(despesa.getQtdParcela()), 2, RoundingMode.HALF_UP);
            despesa.setValor(valorParcela);
        }

        if(!usuarioList.isEmpty()){
            BigDecimal valorTotalADividir = new BigDecimal("0.00");
            BigDecimal valorASubtrair = new BigDecimal("0.00");
            for (Usuario u : usuarioList) {
                if(u.getValorDividido() != null){
                    valorTotalADividir = valorTotalADividir.add(u.getValorDividido());
                }

                if(!Objects.equals(u.getId(), usuario.getId())){
                    valorASubtrair = valorASubtrair.add(u.getValorDividido());
                }
            }

            despesa.setValorTotalADividir(valorTotalADividir);

            despesa.setValorParcelado(valorTotalADividir);

            if((valorTotalADividir.subtract(valorASubtrair)).compareTo(BigDecimal.ZERO) > 0) {
                despesa.setValor(valorTotalADividir.subtract(valorASubtrair));
            }
        }

        //Por fim, gravo a despesa
        despesaRepository.saveAndFlush(despesa);

        //Se houver usuario para divisão da despesa, insere na tabela associativa REGISTRO_FINANCEIRO_USUARIO
        if(!usuarioList.isEmpty()) {
            for (Usuario u : usuarioList) {
                DespesaUsuario despesaUsuario = new DespesaUsuario();
                despesaUsuario.setDespesa(despesa);
                despesaUsuario.setUsuario(u);
                despesaUsuario.setCriadoPor(usuario.getLogin());
                despesaUsuario.setValor(u.getValorDividido());
                despesaUsuarioRepository.saveAndFlush(despesaUsuario);
            }
        }

        //Aplicando as parcelas para outras competências
        List<Despesa> despesaParcelaList = new ArrayList<Despesa>();

        if(despesa.isExisteParcela()){
            despesaParcelaList.add(despesa);

            for (int i = 1; i <= despesa.getQtdParcela(); i++) {
                if(i != 1){
                    //Cria uma nova despesa de arcordo com a
                    Despesa despesaParcelada = new Despesa();
                    BeanUtils.copyProperties(despesa, despesaParcelada);

                    despesaParcelada.setId(null);
                    despesaParcelada.setCompetencia(gerarNovaCompetencia(despesaParcelaList));
                    despesaParcelada.setDtVencimento(despesaParcelaList.getLast().getDtVencimento().plusMonths(1));
                    despesaParcelada.setExisteParcela(true);
                    despesaParcelada.setIdParcelaPai(despesa.getId());
                    despesaParcelada.setNrParcela(i);
                    despesaParcelada.setQtdParcela(despesa.getQtdParcela());

                    despesaParcelada.setValorParcelado(despesa.getValorParcelado());
                    despesaParcelada.setValor(valorParcela);

                    despesaParcelada.setUsuariosResponsaveis(usuarioList);

                    //cria a despesa parcelada para as competências posteriores
                    //Por fim, gravo a despesa que foi clonada e reajustada
                    despesaRepository.saveAndFlush(despesaParcelada);

                    //Se houver usuario para divisão da despesa, insere na tabela associativa REGISTRO_FINANCEIRO_USUARIO
                    if(!usuarioList.isEmpty()) {
                        for (Usuario u : usuarioList) {
                            DespesaUsuario despesaUsuario = new DespesaUsuario();
                            despesaUsuario.setDespesa(despesaParcelada);
                            despesaUsuario.setUsuario(u);
                            despesaUsuario.setCriadoPor(usuario.getLogin());
                            despesaUsuarioRepository.saveAndFlush(despesaUsuario);
                        }
                    }

                    despesaParcelaList.add(despesaParcelada);
                }
            }
        }

        //Retorno a despesa
        return despesa;
    }

    private static String gerarNovaCompetencia(List<Despesa> despesaParcelaList) {
        String[] competenciaSplit = despesaParcelaList.getLast().getCompetencia().split("-");
        int anoCompetencia = Integer.parseInt(competenciaSplit[0]);
        int mesCompetencia = Integer.parseInt(competenciaSplit[1]);
        boolean mesCompetenciaAtualEhDezembro = mesCompetencia == 12;

        //Gerando a nova competência
        String competenciaStr = "";
        competenciaStr += mesCompetenciaAtualEhDezembro ? String.valueOf(anoCompetencia + 1) : anoCompetencia;
        competenciaStr += "-";
        competenciaStr += (mesCompetencia + 1) < 10 ? "0" : "";
        competenciaStr += mesCompetenciaAtualEhDezembro ? String.valueOf(1) : mesCompetencia + 1;
        return competenciaStr;
    }

    @Transactional
    public void editar(DespesaDTO data) throws ObjectNotFoundException {

        //Pega o usuário logado
        String loginUsuarioToken = tokenService.validarToken(tokenService.recuperarToken(request));
        Usuario usuario = usuarioRepository.findByLogin(loginUsuarioToken);

        //Busca o registro Financeiro do banco e altero seus campos que não são chaves
        Despesa despesaBanco = this.buscarPorId(data.getId());

        despesaBanco.setCompetencia(data.getCompetencia());
        despesaBanco.setNome(data.getNome());
        despesaBanco.setDescricao(data.getDescricao());
        despesaBanco.setValor(data.getValor());
        despesaBanco.setTipoRegistroFinanceiro(data.getTipoRegistroFinanceiro());
        despesaBanco.setCategoriaRegistroFinanceiro(data.getCategoriaRegistroFinanceiro());
        despesaBanco.setStatusPagamento(data.getStatusPagamento());
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
                .map(u -> {
                    Usuario uStream = usuarioRepository
                            .findById(u.getId())
                            .orElseThrow(() -> new ObjectNotFoundException("Não foi possível encontrar o usuário com o id " + u.getId() + "."));

                    uStream.setValorDividido(u.getValorDividido());

                    return uStream;
                })
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
                despesaUsuario.setValor(u.getValorDividido());
                despesaUsuario.setCriadoPor(usuario.getLogin());

                if(despesaUsuario.getId() == null) { //trata-se do usuaario adicionando uma divisão para uma despesa sem parcela
                    despesaUsuarioRepository.saveAndFlush(despesaUsuario);
                }

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
                if(despesaTela.getDespesa() != null && despesaUsuarioBanco.getId() != null){
                    if (despesaTela.getId().equals(despesaUsuarioBanco.getId())) {
                        existeNaTela = true;
                        break; // Sai do loop se encontrar o ID
                    }
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


package br.com.dscproject.services;

import br.com.dscproject.domain.*;
import br.com.dscproject.dto.DespesaDTO;
import br.com.dscproject.dto.UsuarioResponsavelDTO;
import br.com.dscproject.enums.StatusPagamento;
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
        Usuario usuarioLogado = usuarioRepository.findByLogin(loginUsuarioToken);

        List<DespesaDTO> despesaDTOList = new ArrayList<>();

        List<Despesa> despesaList = new ArrayList<>();
        despesaList = despesaRepository.findDespesasByUsuarioId(usuarioLogado.getId());

        for (Despesa despesa : despesaList) {
            DespesaDTO dto = new DespesaDTO();
            BeanUtils.copyProperties(despesa, dto);

            //Seta a instituição financeira usuario e a instituicao do usuario
            dto.setInstituicaoFinanceira(despesa.getInstituicaoFinanceiraUsuario().getInstituicaoFinanceira().getNome());
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
                  if(usuarioResponsavel.getId().equals(usuarioLogado.getId())){
                      usuarioResponsavel.setNome("Minha Cota");
                      usuarioResponsavel.setLogado(true);

                      if(usuarioResponsavel.getValorDividido() != null){
                          dto.setValorDividido(usuarioResponsavel.getValorDividido());
                      }
                  }
                  dto.getUsuariosResponsaveis().add(usuarioResponsavel);
                }

                if(usuariosResponsaveis.size() > 1) {
                    dto.setExisteDivisao(true);
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
        Usuario usuarioLogado = usuarioRepository.findByLogin(loginUsuarioToken);

        Despesa despesa  = new Despesa();
        BeanUtils.copyProperties(data, despesa);

        if(data.getDtLancamento() != null) {
            despesa.setDtLancamento(DateUtils.retornaLocalDate(data.getDtLancamento(), "yyyy-MM-dd"));
        }

        if(data.getDtVencimento() != null) {
            despesa.setDtVencimento(DateUtils.retornaLocalDate(data.getDtVencimento(), "yyyy-MM-dd"));
        }

        Optional<InstituicaoFinanceiraUsuario> ifu  = this.instituicaoFinanceiraUsuarioRepository.findById(data.getInstituicaoFinanceiraUsuarioId());
        if(ifu.isEmpty()){
            throw new ObjectNotFoundException("Não existe uma instituição financeira vinculada ao usuário com o id " + data.getInstituicaoFinanceiraUsuarioId() + ".");
        }

        InstituicaoFinanceiraUsuario instituicaoFinanceiraUsuario = ifu.get();

        despesa.setInstituicaoFinanceiraUsuario(instituicaoFinanceiraUsuario);

        despesa.setStatusPagamento(StatusPagamento.NAO);

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

        //Seta Auditoria
        despesa.setCriadoPor(usuarioLogado.getLogin());

        //Valor a dividir da parcela
        if(data.isExisteParcela()) {
            despesa.setValorTotalADividir(despesa.getValor());
        }

        //Por fim, gravo a despesa
        despesaRepository.saveAndFlush(despesa);

        //Se houver usuario para divisão da despesa, insere na tabela associativa REGISTRO_FINANCEIRO_USUARIO
        if(!usuarioList.isEmpty()) {
            Integer qtdPagamentos = 0;

            for (Usuario u : usuarioList) {
                DespesaUsuario despesaUsuario = new DespesaUsuario();
                despesaUsuario.setDespesa(despesa);
                despesaUsuario.setUsuario(u);
                despesaUsuario.setCriadoPor(usuarioLogado.getLogin());
                despesaUsuario.setValor(u.getValorDividido());

                boolean despesaFoiPaga = data.getUsuariosResponsaveis().stream().anyMatch(uDTO -> uDTO.getId().equals(despesaUsuario.getUsuario().getId()) && uDTO.isStatusPagamento());
                despesaUsuario.setStatusPagamento(despesaFoiPaga);

                despesaUsuarioRepository.saveAndFlush(despesaUsuario);

                if(despesaFoiPaga){
                    qtdPagamentos++;
                }
            }

            if(qtdPagamentos == usuarioList.size()){
                pagarDespesa(despesa, usuarioLogado);
            }
        }

        //Faz o parcelamento se houver
        if(data.isExisteParcela()) {
            gerarParcelamento(despesa, usuarioList, usuarioLogado);
        }

        return despesa;
    }

    private void pagarDespesa(Despesa despesa, Usuario usuarioLogado){
        despesa.setStatusPagamento(StatusPagamento.SIM);
        despesa.setAlteradoPor(usuarioLogado.getLogin());
        despesa.setDataAlteracao(Instant.now());

        despesaRepository.saveAndFlush(despesa);
    }

    private void gerarParcelamento(Despesa despesaOriginal, List<Usuario> usuarioList, Usuario usuarioLogado){
        //Aplicando as parcelas para outras competências
        List<Despesa> despesaParcelaList = new ArrayList<Despesa>();

        if(despesaOriginal.isExisteParcela()){
            despesaParcelaList.add(despesaOriginal);

            for (int i = 1; i <= despesaOriginal.getQtdParcela(); i++) {
                if(i != 1){
                    //Cria uma nova despesa de arcordo com a
                    Despesa despesaParcelada = new Despesa();
                    BeanUtils.copyProperties(despesaOriginal, despesaParcelada);

                    despesaParcelada.setId(null);
                    despesaParcelada.setCompetencia(gerarNovaCompetencia(despesaParcelaList));
                    despesaParcelada.setDtVencimento(despesaParcelaList.getLast().getDtVencimento().plusMonths(1));
                    despesaParcelada.setExisteParcela(true);
                    despesaParcelada.setIdParcelaPai(despesaOriginal.getId());
                    despesaParcelada.setNrParcela(i);
                    despesaParcelada.setQtdParcela(despesaOriginal.getQtdParcela());
                    despesaParcelada.setStatusPagamento(StatusPagamento.NAO);

                    despesaParcelada.setValorParcelado(despesaOriginal.getValorParcelado());
                    despesaParcelada.setValor(despesaOriginal.getValorParcelado());

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
                            despesaUsuario.setCriadoPor(usuarioLogado.getLogin());
                            despesaUsuario.setStatusPagamento(false);
                            despesaUsuarioRepository.saveAndFlush(despesaUsuario);
                        }
                    }

                    despesaParcelaList.add(despesaParcelada);
                }
            }
        }
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
        Usuario usuarioLogado = usuarioRepository.findByLogin(loginUsuarioToken);

        //Busca o registro Financeiro do banco e altero seus campos que não são chaves
        Despesa despesaBanco = this.buscarPorId(data.getId());

        despesaBanco.setCompetencia(data.getCompetencia());
        despesaBanco.setNome(data.getNome());
        despesaBanco.setDescricao(data.getDescricao());
        despesaBanco.setValor(data.getValor());
        despesaBanco.setTipoRegistroFinanceiro(data.getTipoRegistroFinanceiro());
        despesaBanco.setCategoriaRegistroFinanceiro(data.getCategoriaRegistroFinanceiro());
        despesaBanco.setStatusPagamento(StatusPagamento.NAO);
        despesaBanco.setAlteradoPor(usuarioLogado.getLogin()); //Auditoria

        if(data.getDtLancamento() != null) {
            despesaBanco.setDtLancamento(DateUtils.retornaLocalDate(data.getDtLancamento(), "yyyy-MM-dd"));
        }

        if(data.getDtVencimento() != null) {
            despesaBanco.setDtVencimento(DateUtils.retornaLocalDate(data.getDtVencimento(), "yyyy-MM-dd"));
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
        List<DespesaUsuario> despesaUsuarioList = new ArrayList<DespesaUsuario>();
        if(!usuarioList.isEmpty()) {
            Integer qtdPagamentos = 0;

            for (Usuario u : usuarioList) {
                DespesaUsuario rfu = despesaUsuarioRepository.findByUsuarioAndDespesa(u,despesaBanco);

                DespesaUsuario despesaUsuario = new DespesaUsuario();
                despesaUsuario.setId((rfu != null) ? rfu.getId() : null);
                despesaUsuario.setDespesa(despesaBanco);
                despesaUsuario.setUsuario(u);
                despesaUsuario.setValor(u.getValorDividido());
                despesaUsuario.setCriadoPor(usuarioLogado.getLogin());

                boolean despesaFoiPaga = data.getUsuariosResponsaveis().stream().anyMatch(uDTO -> uDTO.getId().equals(despesaUsuario.getUsuario().getId()) && uDTO.isStatusPagamento());
                despesaUsuario.setStatusPagamento(despesaFoiPaga);

                if(despesaFoiPaga){
                    qtdPagamentos++;
                }

                if(despesaUsuario.getId() == null) { //trata-se do usuaario adicionando uma divisão para uma despesa sem parcela
                    despesaUsuarioRepository.saveAndFlush(despesaUsuario);
                }

                despesaUsuarioList.add(despesaUsuario);
            }

            if(qtdPagamentos == usuarioList.size()){
                pagarDespesa(despesaBanco, usuarioLogado);
            }
        }

        List<DespesaUsuario> registrosFinanceirosUsuarioBancoList = new ArrayList<>();
        registrosFinanceirosUsuarioBancoList =  despesaUsuarioRepository.findByDespesa(Optional.of(despesaBanco));
        List<DespesaUsuario> registrosFinanceirosUsuarioAExcluir = new ArrayList<>();

        // Cria Lista de registrosFinanceirosUsuarios que serão excluídos
        for (DespesaUsuario despesaUsuarioBanco : registrosFinanceirosUsuarioBancoList) {
            boolean existeNaTela = false;

            for (DespesaUsuario despesaTela : despesaUsuarioList) {
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

        HashSet<DespesaUsuario> despesaUsuarioSet = new HashSet<>(despesaUsuarioList);


        // Agora você pode salvar os objetos que precisam ser salvos
        if(!despesaUsuarioList.isEmpty()){
           despesaUsuarioRepository.saveAllAndFlush(despesaUsuarioSet);
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


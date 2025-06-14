package br.com.dscproject.services;

import br.com.dscproject.domain.Despesa;
import br.com.dscproject.domain.DespesaUsuario;
import br.com.dscproject.domain.InstituicaoFinanceiraUsuario;
import br.com.dscproject.domain.Usuario;
import br.com.dscproject.dto.DespesaDTO;
import br.com.dscproject.dto.UsuarioResponsavelDTO;
import br.com.dscproject.dto.UsuarioResponsavelQueryDTO;
import br.com.dscproject.enums.StatusPagamento;
import br.com.dscproject.enums.TipoRegistroFinanceiro;
import br.com.dscproject.repository.DespesaRepository;
import br.com.dscproject.repository.DespesaUsuarioRepository;
import br.com.dscproject.repository.InstituicaoFinanceiraUsuarioRepository;
import br.com.dscproject.repository.UsuarioRepository;
import br.com.dscproject.services.exceptions.DataIntegrityException;
import br.com.dscproject.services.exceptions.ObjectNotFoundException;
import br.com.dscproject.utils.DateUtils;
import com.webcohesion.ofx4j.domain.data.MessageSetType;
import com.webcohesion.ofx4j.domain.data.ResponseEnvelope;
import com.webcohesion.ofx4j.domain.data.ResponseMessageSet;
import com.webcohesion.ofx4j.domain.data.common.Transaction;
import com.webcohesion.ofx4j.domain.data.creditcard.CreditCardAccountDetails;
import com.webcohesion.ofx4j.domain.data.creditcard.CreditCardResponseMessageSet;
import com.webcohesion.ofx4j.domain.data.creditcard.CreditCardStatementResponse;
import com.webcohesion.ofx4j.domain.data.creditcard.CreditCardStatementResponseTransaction;
import com.webcohesion.ofx4j.io.AggregateUnmarshaller;
import com.webcohesion.ofx4j.io.OFXParseException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
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
    private TokenService tokenService;

    @Autowired
    private HttpServletRequest request;

    @Transactional
    public List<DespesaDTO> buscarTodosPorUsuario() {

        List<DespesaDTO> despesaDTOList = new ArrayList<>();

        List<Despesa> despesaList = new ArrayList<>();
        despesaList = despesaRepository.findDespesasByUsuarioId(this.retornaUsuarioLogado().getId());

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

            Set<UsuarioResponsavelQueryDTO> usuariosResponsaveisQuery = new HashSet<UsuarioResponsavelQueryDTO>();

            if(despesa.getId() != null) {
                usuariosResponsaveisQuery = despesaRepository.findUsuariosByDespesaId(despesa.getId());
            }


            if(!usuariosResponsaveisQuery.isEmpty()){
                for (UsuarioResponsavelQueryDTO urq : usuariosResponsaveisQuery) {

                    UsuarioResponsavelDTO ur = new UsuarioResponsavelDTO();
                    BeanUtils.copyProperties(urq, ur);


                    if(ur.getId().equals(this.retornaUsuarioLogado().getId())){
                        ur.setNome("Minha Cota");
                        ur.setLogado(true);

                      if(ur.getValorDividido() != null){
                          dto.setValorDividido(ur.getValorDividido());
                      }
                  }
                  dto.getUsuariosResponsaveis().add(ur);
                }

                if(usuariosResponsaveisQuery.size() > 1) {
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

    @Transactional
    public Despesa inserir(DespesaDTO data) {

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
        despesa.setCriadoPor(this.retornaUsuarioLogado().getLogin());

        //Valor a dividir da parcela
        if(data.isExisteParcela()) {
            despesa.setValorTotalADividir(despesa.getValor());
        }

        despesaRepository.saveAndFlush(despesa);

        //Se houver usuario para divisão da despesa, insere na tabela associativa REGISTRO_FINANCEIRO_USUARIO
        if(!usuarioList.isEmpty()) {
            Integer qtdPagamentos = 0;

            for (Usuario u : usuarioList) {
                DespesaUsuario despesaUsuario = new DespesaUsuario();
                despesaUsuario.setDespesa(despesa);
                despesaUsuario.setUsuario(u);
                despesaUsuario.setCriadoPor(this.retornaUsuarioLogado().getLogin());
                despesaUsuario.setValor(u.getValorDividido());

                boolean despesaFoiPaga = data.getUsuariosResponsaveis().stream().anyMatch(uDTO -> uDTO.getId().equals(despesaUsuario.getUsuario().getId()) && uDTO.getStatusPagamento());
                despesaUsuario.setStatusPagamento(despesaFoiPaga);

                despesaUsuarioRepository.saveAndFlush(despesaUsuario);

                if(despesaFoiPaga){
                    qtdPagamentos++;
                }
            }

            if(qtdPagamentos == usuarioList.size()){
                pagarDespesa(despesa, this.retornaUsuarioLogado());
            }
        }

        //Faz o parcelamento se houver
        if(data.isExisteParcela()) {
            gerarParcelamento(despesa, usuarioList, this.retornaUsuarioLogado());
        }

        return despesa;
    }

    public void pagarDespesa(Despesa despesa, Usuario usuarioLogado){
        despesa.setStatusPagamento(StatusPagamento.SIM);
        despesa.setAlteradoPor(usuarioLogado.getLogin());
        despesa.setDataAlteracao(Instant.now());
    }

    @Transactional
    public void gerarParcelamento(Despesa despesaOriginal, List<Usuario> usuarios, Usuario usuarioLogado) {
        List<Despesa> parcelasList = new ArrayList<>();

        parcelasList.add(despesaOriginal);

        for (int i = 1; i <= despesaOriginal.getQtdParcela(); i++) {
            if (i == 1) continue;

            Despesa parcela = new Despesa();
            BeanUtils.copyProperties(despesaOriginal, parcela, "id", "despesaUsuarios");
            parcela.setCompetencia(gerarNovaCompetencia(parcelasList));
            parcela.setDtVencimento(parcelasList.getLast().getDtVencimento().plusMonths(1));
            parcela.setIdParcelaPai(despesaOriginal.getId());
            parcela.setNrParcela(i);
            parcela.setStatusPagamento(StatusPagamento.NAO);

            for (Usuario usuario : usuarios) {
                DespesaUsuario du = new DespesaUsuario();
                du.setDespesa(parcela);
                du.setUsuario(usuario);
                du.setValor(usuario.getValorDividido());
                du.setStatusPagamento(false);
                du.setCriadoPor(usuarioLogado.getLogin());
                parcela.getDespesaUsuarios().add(du);
            }

            despesaRepository.save(parcela);

            parcelasList.add(parcela);
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

        //Busca o registro Financeiro do banco e altero seus campos que não são chaves
        Despesa despesaBanco = this.buscarPorId(data.getId());

        despesaBanco.setCompetencia(data.getCompetencia());
        despesaBanco.setNome(data.getNome());
        despesaBanco.setDescricao(data.getDescricao());
        despesaBanco.setValor(data.getValor());
        despesaBanco.setTipoRegistroFinanceiro(data.getTipoRegistroFinanceiro());
        despesaBanco.setCategoriaRegistroFinanceiro(data.getCategoriaRegistroFinanceiro());
        despesaBanco.setStatusPagamento(StatusPagamento.NAO);
        despesaBanco.setAlteradoPor(this.retornaUsuarioLogado().getLogin()); //Auditoria

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
                despesaUsuario.setCriadoPor(this.retornaUsuarioLogado().getLogin());

                boolean despesaFoiPaga = data.getUsuariosResponsaveis().stream().anyMatch(uDTO -> uDTO.getId().equals(despesaUsuario.getUsuario().getId()) && uDTO.getStatusPagamento());
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
                pagarDespesa(despesaBanco, this.retornaUsuarioLogado());
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

    @Transactional
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

    public String importarDadosCartaoCreditoOfx(MultipartFile file, String bancoCodigo, String competencia) throws OFXParseException, IOException {

        if(this.retornaUsuarioLogado() == null){
            throw new RuntimeException("Usuário não está logado, por favor, faça Login.");
        }

        //Obtendo dados da instituição financeira vinculada ao usuário
        InstituicaoFinanceiraUsuario instituicaoFinanceiraUsuario = this.instituicaoFinanceiraUsuarioRepository.findByUsuario_IdAndInstituicaoFinanceira_Codigo(this.retornaUsuarioLogado().getId(), bancoCodigo);
        if(instituicaoFinanceiraUsuario == null){
            throw new RuntimeException("Não foi encontrada a instituição financeira de codigo " + bancoCodigo + " vinculada a este usuario");
        }


        InputStreamReader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
        AggregateUnmarshaller a = new AggregateUnmarshaller(ResponseEnvelope.class);
        ResponseEnvelope re = (ResponseEnvelope) a.unmarshal(reader);

        MessageSetType type = MessageSetType.creditcard;
        ResponseMessageSet message = re.getMessageSet(type);

        List<Despesa> despesaList = new ArrayList<>();

        if(message != null){

            List<CreditCardStatementResponseTransaction> cartaoList = ((CreditCardResponseMessageSet) message).getStatementResponses();
            if(cartaoList.isEmpty()){
                throw new RuntimeException("Não foi possível obter resposta do conteúdo do arquivo, verifique se é um arquivo OFX válido.");
            }

            for(CreditCardStatementResponseTransaction cartao : cartaoList){

                CreditCardStatementResponse response = cartao.getMessage();

                if(response == null){
                    throw new RuntimeException("Não foi possível obter mensagem de resposta do arquivo, verifique se é um arquivo OFX válido.");
                }

                CreditCardAccountDetails conta = response.getAccount();

                List<Transaction> transacoes = response.getTransactionList().getTransactions();
                if(transacoes.isEmpty()){
                    throw new RuntimeException("Não existem transações nesse arquivo OFX.");
                }

                for(Transaction tb : transacoes){
                    Despesa despesa = new Despesa();
                    despesa.setId(null);
                    despesa.setCompetencia(competencia);
                    despesa.setNome(tb.getMemo());
                    despesa.setValor(BigDecimal.valueOf(tb.getAmount()).abs());

                    Date date = tb.getDatePosted();

                    LocalDate localDate = date.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();

                    despesa.setDtLancamento(localDate);

                    despesa.setOfxTransacaoId(tb.getId());
                    despesa.setTipoRegistroFinanceiro(TipoRegistroFinanceiro.DESPESA);
                    despesa.setCategoriaRegistroFinanceiro(null);
                    despesa.setInstituicaoFinanceiraUsuario(instituicaoFinanceiraUsuario);

                    despesa.setCriadoPor(this.retornaUsuarioLogado().getLogin());
                    despesa.setDataCriacao(Instant.now());

                    despesa.setAlteradoPor(null);
                    despesa.setDataAlteracao(null);

                    if(!"Pagamento recebido".equals(despesa.getNome())){
                        despesaList.add(despesa);
                    }
                }
            }
        }

        if(despesaList.isEmpty()){
            throw new RuntimeException("Não foi possível obter as transações do cartão de crédito do arquivo OFX.");
        }

        validaExistenciaDespesaImportadaNoBanco(despesaList);

        despesaRepository.saveAllAndFlush(despesaList);

        return "Arquivo OFX Importado com sucesso!";
    }

    public void validaExistenciaDespesaImportadaNoBanco(List<Despesa> despesaList) {
        for (Despesa despesa : despesaList) {
            boolean existeRegistroNoBanco = false;

            List<Despesa> despesaBancoList = despesaRepository.findByCompetenciaAndNomeAndDescricaoAndDtLancamentoAndOfxTransacaoIdAndInstituicaoFinanceiraUsuario_Id(
                despesa.getCompetencia(),
                despesa.getNome(),
                despesa.getDescricao(),
                despesa.getDtLancamento(),
                despesa.getOfxTransacaoId(),
                despesa.getInstituicaoFinanceiraUsuario().getId()
            );

            existeRegistroNoBanco = despesaRepository.existsByCompetenciaAndNomeAndDescricaoAndDtLancamentoAndOfxTransacaoIdAndInstituicaoFinanceiraUsuario_Id(
                    despesa.getCompetencia(),
                    despesa.getNome(),
                    despesa.getDescricao(),
                    despesa.getDtLancamento(),
                    despesa.getOfxTransacaoId(),
                    despesa.getInstituicaoFinanceiraUsuario().getId()
            );

            if (existeRegistroNoBanco) {
                throw new RuntimeException("A despesa de id " + despesa.getOfxTransacaoId() + ", nome '" + despesa.getNome() + "' já existe no banco de dados.");
            }
        }
    }



    public Usuario retornaUsuarioLogado(){
        String loginUsuarioToken = this.tokenService.validarToken(tokenService.recuperarToken(request));
       return this.usuarioRepository.findByLogin(loginUsuarioToken);
    }

}




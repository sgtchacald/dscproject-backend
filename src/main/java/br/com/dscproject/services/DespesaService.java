package br.com.dscproject.services;

import br.com.dscproject.domain.*;
import br.com.dscproject.dto.*;
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
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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

    private Usuario retornaUsuarioLogado(){
        String loginUsuarioToken = this.tokenService.validarToken(tokenService.recuperarToken(request));
        return this.usuarioRepository.findByLogin(loginUsuarioToken);
    }

    private InstituicaoFinanceiraUsuario retornaInstituicaoFinanceiraUsuario(String bancoCodigo){
        InstituicaoFinanceiraUsuario instituicaoFinanceiraUsuario = this.instituicaoFinanceiraUsuarioRepository.findByUsuario_IdAndInstituicaoFinanceira_Codigo(this.retornaUsuarioLogado().getId(), bancoCodigo);

        if(instituicaoFinanceiraUsuario == null){
            throw new RuntimeException("Não foi encontrada a instituição financeira de codigo " + bancoCodigo + " vinculada a este usuario");
        }
        return instituicaoFinanceiraUsuario;
    }

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

        if(!despesaRepository.getById(despesa.getId()).getStatusPagamento().equals(StatusPagamento.SIM)){
            despesa.setDtPagamento(LocalDate.now());
        }

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

    public void validaExistenciaDespesaImportacao(List<Despesa> despesaList) {
        for (Despesa despesa : despesaList) {
            boolean existeRegistroNoBanco = false;

            existeRegistroNoBanco = despesaRepository.existsByCompetenciaAndNomeAndDescricaoAndDtLancamentoAndTransacaoIdAndInstituicaoFinanceiraUsuario_Id(
                    despesa.getCompetencia(),
                    despesa.getNome(),
                    despesa.getDescricao(),
                    despesa.getDtLancamento(),
                    despesa.getTransacaoId(),
                    despesa.getInstituicaoFinanceiraUsuario().getId()
            );

            if (existeRegistroNoBanco) {
                throw new RuntimeException("A despesa de id " + despesa.getTransacaoId() + ", nome '" + despesa.getNome() + "' já existe no banco de dados.");
            }
        }
    }

    public Despesa setDespesaImportacao(Despesa despesa, String competencia, String bancoCodigo, String dtVencimento, HSSFCell celulaIdTransacao) {

        despesa.setDescricao(despesa.getNome());
        despesa.setCompetencia(competencia);
        despesa.setTipoRegistroFinanceiro(TipoRegistroFinanceiro.DESPESA);
        despesa.setInstituicaoFinanceiraUsuario(retornaInstituicaoFinanceiraUsuario(bancoCodigo));
        despesa.setStatusPagamento(StatusPagamento.NAO);
        despesa.setTransacaoId("TRANSACAO_ID_IMPORTACAO_EXCEL_" + this.retornaInstituicaoFinanceiraUsuario(bancoCodigo).getInstituicaoFinanceira().getNome().toUpperCase() + "_" + this.retornaUsuarioLogado().getId() + "_" + competencia.replaceAll("-", ""));

        Instant instant = Instant.parse(dtVencimento.replaceAll("\"", ""));
        LocalDate localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
        despesa.setDtVencimento(localDate);

        despesa.setCriadoPor(this.retornaUsuarioLogado().getLogin());
        despesa.setDataCriacao(Instant.now());

        despesa.setAlteradoPor(null);
        despesa.setDataAlteracao(null);

        return despesa;
    }

    public void setDespesaUsuarioImportacao(List <Despesa> despesaList){
        if(despesaList.isEmpty()){
            throw new RuntimeException("Nao foi possível salvar dados na tabela DESPESA_USUARIO.");
        }
        for(Despesa despesa : despesaList){
            Usuario usuario = retornaUsuarioLogado();

            DespesaUsuario despesaUsuario = new DespesaUsuario();
            despesaUsuario.setDespesa(despesa);
            despesaUsuario.setUsuario(usuario);
            despesaUsuario.setValor(despesa.getValor());
            despesaUsuario.setStatusPagamento(false);

            despesaUsuario.setDataCriacao(Instant.now());
            despesaUsuario.setAlteradoPor(usuario.getAlteradoPor());

            despesaUsuario.setAlteradoPor(null);
            despesaUsuario.setDataAlteracao(null);

            despesaUsuarioRepository.saveAndFlush(despesaUsuario);
        }
    }

    public String importarDadosCartaoCredito(MultipartFile file, String competencia, String bancoCodigo, String dtVencimento) throws IOException, OFXParseException {

        if(this.retornaUsuarioLogado() == null){
            throw new RuntimeException("Usuário não está logado, por favor, faça Login.");
        }

        String tipoImportacao = (bancoCodigo.equals("260"))
                ? "ofx"
                : this.retornaInstituicaoFinanceiraUsuario(bancoCodigo).getInstituicaoFinanceira().getNome().replaceAll(" ", "").toLowerCase();

        return switch (tipoImportacao) {
            case "bradesco" -> importarDadosCartaoCreditoExcelBradesco(file, bancoCodigo, competencia, dtVencimento);
            case "itaú"     -> importarDadosCartaoCreditoExcelItau(file, bancoCodigo, competencia, dtVencimento);
            case "c6bank"   -> importarDadosCartaoCreditoExcelC6Bank(file, bancoCodigo, competencia, dtVencimento);
            case "ofx"      -> importarDadosCartaoCreditoOfx(file, bancoCodigo, competencia, dtVencimento);
            default         -> "Não foi possível fazer a importação do arquivo";
        };
    }

    public String importarDadosCartaoCreditoExcelItau(MultipartFile file, String competencia, String bancoCodigo, String dtVencimento) throws IOException {
        InputStream arquivoExcel = file.getInputStream();

        HSSFWorkbook workbook = new HSSFWorkbook(arquivoExcel);

        HSSFSheet sheet = workbook.getSheetAt(0);

        if(sheet == null  || sheet.getPhysicalNumberOfRows() == 0){
            throw new RuntimeException("Não existem planilhas para serem lidas, tente um arquivo válido.");
        }

        List<Despesa> despesasList = new ArrayList<Despesa>();

        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            HSSFRow row = sheet.getRow(i);
            if (row == null) continue;

            HSSFCell cellA = row.getCell(0); // Data
            HSSFCell cellB = row.getCell(1); // Descrição
            HSSFCell cellD = row.getCell(3); // Valor

            if(cellA == null || cellB == null || cellD == null) continue;

            String valorA = cellA.toString().trim();
            String valorB = cellB.toString().trim();
            String valorD = cellD.toString().trim();

            if (valorA.isBlank() || valorB.isBlank() || valorD.isBlank()) continue;
            if("PAGAMENTO EFETUADO".equals(valorB.trim())) continue;
            if ("data".equals(valorA) || "lançamento".equalsIgnoreCase(valorB) || "valor".equals(valorD)) continue;

            Despesa despesa = new Despesa();

            if (cellA.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cellA)) {
                LocalDate localDate = cellA.getLocalDateTimeCellValue().toLocalDate();
                despesa.setDtLancamento(localDate);
            } else {
                // ou você pode tentar parsear se vier como string no formato dd/MM/yyyy
                String texto = cellA.getStringCellValue().trim();
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    LocalDate localDate = LocalDate.parse(texto, formatter);
                    despesa.setDtLancamento(localDate);

                } catch (DateTimeParseException e) {
                    throw new RuntimeException("Data inválida na célula A: " + texto);
                }
            }
            despesa.setNome(cellB.toString());
            despesa.setValor(BigDecimal.valueOf(cellD.getNumericCellValue()));

            despesa = setDespesaImportacao(despesa, bancoCodigo, competencia, dtVencimento, null);

            despesasList.add(despesa);
        }

        workbook.close();

        validaExistenciaDespesaImportacao(despesasList);

        despesaRepository.saveAllAndFlush(despesasList);
        setDespesaUsuarioImportacao(despesasList);

        return "Arquivo .xls Importado com sucesso!";
    }

    public String importarDadosCartaoCreditoExcelBradesco(MultipartFile file, String competencia, String bancoCodigo, String dtVencimento) throws OFXParseException, IOException {

        InputStream arquivoExcel = file.getInputStream();

        HSSFWorkbook workbook = new HSSFWorkbook(arquivoExcel);

        HSSFSheet sheet = workbook.getSheetAt(0);

        if(sheet == null  || sheet.getPhysicalNumberOfRows() == 0){
            throw new RuntimeException("Não existem planilhas para serem lidas, tente um arquivo válido.");
        }

        List<Despesa> despesasList = new ArrayList<Despesa>();

        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            HSSFRow row = sheet.getRow(i);
            if (row == null) continue;

            HSSFCell cellA = row.getCell(0); // Data
            HSSFCell cellB = row.getCell(1); // Descrição
            HSSFCell cellE = row.getCell(4); // Valor

            if(cellA == null || cellB == null || cellE == null) continue;

            String valorA = cellA.toString().trim();
            String valorB = cellB.toString().trim();
            String valorE = cellE.toString().trim();

            if (valorA.isBlank() || valorB.isBlank() || valorE.isBlank()) continue;
            if("PAGAMENTO EFETUADO".equals(valorB.trim())) continue;
            if ("Data".equalsIgnoreCase(valorA) || "Histórico".equalsIgnoreCase(valorB) || "Valor(R$)".equalsIgnoreCase(valorE)) continue;

            Despesa despesa = new Despesa();

            if (cellA.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cellA)) {
                LocalDate localDate = cellA.getLocalDateTimeCellValue().toLocalDate();
                despesa.setDtLancamento(localDate);
            } else {
                // ou você pode tentar parsear se vier como string no formato dd/MM/yyyy
                String texto = cellA.getStringCellValue().trim() +  "/" + LocalDate.now().getYear();
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    LocalDate localDate = LocalDate.parse(texto, formatter);
                    despesa.setDtLancamento(localDate);

                } catch (DateTimeParseException e) {
                    throw new RuntimeException("Data inválida na célula A: " + texto);
                }
            }

            despesa.setNome(cellB.toString());

            despesa.setValor(BigDecimal.valueOf(Double.parseDouble(cellE.getStringCellValue().replaceAll(",", "."))));

            despesa = setDespesaImportacao(despesa, bancoCodigo, competencia, dtVencimento, null);

            despesasList.add(despesa);
        }

        workbook.close();

        validaExistenciaDespesaImportacao(despesasList);

        despesaRepository.saveAllAndFlush(despesasList);

        setDespesaUsuarioImportacao(despesasList);

        return "Arquivo .xls Importado com sucesso!";
    }

    public String importarDadosCartaoCreditoExcelC6Bank(MultipartFile file, String competencia, String bancoCodigo, String dtVencimento) throws IOException {
        InputStream arquivoExcel = file.getInputStream();

        HSSFWorkbook workbook = new HSSFWorkbook(arquivoExcel);

        HSSFSheet sheet = workbook.getSheetAt(0);

        if(sheet == null  || sheet.getPhysicalNumberOfRows() == 0){
            throw new RuntimeException("Não existem planilhas para serem lidas, tente um arquivo válido.");
        }

        List<Despesa> despesasList = new ArrayList<Despesa>();

        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            HSSFRow row = sheet.getRow(i);
            if (row == null) continue;

            HSSFCell cellA = row.getCell(0); // Data
            HSSFCell cellE = row.getCell(4); // Descrição
            HSSFCell cellI = row.getCell(8); // Valor

            if(cellA == null || cellE == null || cellI == null) continue;

            String valorA = cellA.toString().trim();
            String valorB = cellE.toString().trim();
            String valorD = cellI.toString().trim();

            if (valorA.isBlank() || valorB.isBlank() || valorD.isBlank()) continue;
            if("PAGAMENTO EFETUADO".equalsIgnoreCase(valorB.trim())) continue;
            if ("Data de compra".equalsIgnoreCase(valorA) || "Descrição".equalsIgnoreCase(valorB) || "Valor (em R$)".equalsIgnoreCase(valorD)) continue;

            Despesa despesa = new Despesa();

            if (cellA.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cellA)) {
                LocalDate localDate = cellA.getLocalDateTimeCellValue().toLocalDate();
                despesa.setDtLancamento(localDate);
            } else {
                // ou você pode tentar parsear se vier como string no formato dd/MM/yyyy
                String texto = cellA.getStringCellValue().trim();
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    LocalDate localDate = LocalDate.parse(texto, formatter);
                    despesa.setDtLancamento(localDate);

                } catch (DateTimeParseException e) {
                    throw new RuntimeException("Data inválida na célula A: " + texto);
                }
            }

            despesa.setNome(cellE.toString());
            despesa.setValor(BigDecimal.valueOf(cellI.getNumericCellValue()));

            despesa = setDespesaImportacao(despesa, bancoCodigo, competencia, dtVencimento, null);

            despesasList.add(despesa);
        }

        workbook.close();

        validaExistenciaDespesaImportacao(despesasList);

        despesaRepository.saveAllAndFlush(despesasList);

        setDespesaUsuarioImportacao(despesasList);

        return "Arquivo .xls Importado com sucesso!";
    }

    public String importarDadosCartaoCreditoOfx(MultipartFile file, String bancoCodigo, String competencia, String dtVencimento) throws OFXParseException, IOException {

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

                    despesa.setTransacaoId("TRANSACAO_ID_IMPORTACAO_EXCEL_" + this.retornaInstituicaoFinanceiraUsuario(bancoCodigo).getInstituicaoFinanceira().getNome().toUpperCase() + "_" + this.retornaUsuarioLogado().getId() + "_" + competencia.replaceAll("-", "") + "_" + tb.getId());
                    despesa.setTipoRegistroFinanceiro(TipoRegistroFinanceiro.DESPESA);
                    despesa.setCategoriaRegistroFinanceiro(null);
                    despesa.setInstituicaoFinanceiraUsuario(retornaInstituicaoFinanceiraUsuario(bancoCodigo));
                    despesa.setTransacaoId("TRANSACAO_ID_IMPORTACAO_EXCEL_" + this.retornaInstituicaoFinanceiraUsuario(bancoCodigo).getInstituicaoFinanceira().getNome().toUpperCase() + "_" + this.retornaUsuarioLogado().getId() + "_" + competencia.replaceAll("-", ""));

                    Instant instant = Instant.parse(dtVencimento.replaceAll("\"", ""));
                    LocalDate localDateDtVencimento = instant.atZone(ZoneId.systemDefault()).toLocalDate();
                    despesa.setDtVencimento(localDateDtVencimento);

                    despesa.setStatusPagamento(StatusPagamento.NAO);

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

        validaExistenciaDespesaImportacao(despesaList);

        despesaRepository.saveAllAndFlush(despesaList);

        setDespesaUsuarioImportacao(despesaList);

        return "Arquivo OFX Importado com sucesso!";
    }

    public DashboardCardSaldoDTO buscarTotalPorCompetencia(String competencia) {
        Usuario usuario = retornaUsuarioLogado();

        BigDecimal total = BigDecimal.ZERO;

        List<DespesaUsuario> despesaList = despesaUsuarioRepository.findByUsuario_IdAndDespesa_Competencia(usuario.getId(), competencia);

        if(!despesaList.isEmpty()){
            for(DespesaUsuario du : despesaList){
                total = total.add(du.getValor());
            }
        }

        DashboardCardSaldoDTO dashboardCardSaldo = new DashboardCardSaldoDTO();

        dashboardCardSaldo.setValor(NumberFormat.getCurrencyInstance().format(total).replace("R$ ", ""));

        return dashboardCardSaldo;
    }


    public void pagarDespesas(List<Long> idDespesaList) {

        List<Optional<Despesa>> despesaList = new ArrayList<>();

        for (Long id : idDespesaList) {
            despesaList.add(despesaRepository.findById(id));
        }

        try {
            if(!despesaList.isEmpty()){
                for(Optional<Despesa> d : despesaList){
                    d.get().setStatusPagamento(StatusPagamento.SIM);
                    d.get().setDtPagamento(LocalDate.now());
                    d.get().setAlteradoPor(retornaUsuarioLogado().getLogin());
                    d.get().setDataAlteracao(Instant.now());

                    List<DespesaUsuario> despesaUsuarioList = despesaUsuarioRepository.findByDespesa(d);

                    try {

                        for(DespesaUsuario du : despesaUsuarioList){
                            du.setStatusPagamento(true);
                            despesaUsuarioRepository.save(du);
                        }

                        despesaRepository.save(d.get());

                    }catch (RuntimeException e){
                        throw new RuntimeException("Problema ao efetuar o pagamento da despesa " + d.get().getNome());
                    }
                }
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    public void compartilharDespesas(DespesaCompartilharDTO dto) {

        List<Despesa> despesasCompartilhadasList = new ArrayList<Despesa>();
        List<Usuario> usuariosQueIraoDividirDespesaList = new ArrayList<Usuario>();

        for(Long idDespesa : dto.getIdDespesaList()){
            despesasCompartilhadasList.add(despesaRepository.findById(idDespesa).get());
        }

        for(Long idUsuario: dto.getIdusuariosACompartilharList()){
            usuariosQueIraoDividirDespesaList.add(usuarioRepository.findById(idUsuario).get());
        }

        if(despesasCompartilhadasList.isEmpty() ||usuariosQueIraoDividirDespesaList.isEmpty()){
            throw new RuntimeException("Usuários ou despesas não selecionadas, por favor selecione pelo menos um de cada para compartilhar despesas!");
        }

        for(Despesa despesa: despesasCompartilhadasList) {
            List<DespesaUsuario> despesaUsuarioList = new ArrayList<>();
            for (Usuario usuario : usuariosQueIraoDividirDespesaList) {
                DespesaUsuario du = despesaUsuarioRepository.findByUsuarioAndDespesa(usuario, despesa);

                DespesaUsuario despesaUsuario = new DespesaUsuario();
                despesaUsuario.setId((du != null) ? du.getId() : null);
                despesaUsuario.setDespesa(despesa);
                despesaUsuario.setUsuario(usuario);

                BigDecimal valorDividido = BigDecimal.ZERO;
                BigDecimal valorDespesa = despesa.getValor();
                BigDecimal divisor = BigDecimal.valueOf(usuariosQueIraoDividirDespesaList.size());

                if(valorDespesa != null && divisor != null){
                    valorDividido = valorDividido.add(valorDespesa.divide(divisor, 2,  BigDecimal.ROUND_HALF_UP));
                }

                despesaUsuario.setValor(valorDividido);

                despesaUsuario.setCriadoPor(this.retornaUsuarioLogado().getLogin());

                if (despesaUsuario.getId() == null) {
                    despesaUsuarioRepository.saveAndFlush(despesaUsuario);
                }

                despesaUsuarioList.add(despesaUsuario);
            }

            List<DespesaUsuario> registrosFinanceirosUsuarioBancoList = new ArrayList<>();
            registrosFinanceirosUsuarioBancoList = despesaUsuarioRepository.findByDespesa(Optional.of(despesa));
            List<DespesaUsuario> despesaUsuarioAExcluir = new ArrayList<>();

            for (DespesaUsuario despesaUsuarioBanco : registrosFinanceirosUsuarioBancoList) {
                boolean existeNaTela = false;

                for (DespesaUsuario despesaTela : despesaUsuarioList) {
                    if (despesaTela.getDespesa() != null && despesaUsuarioBanco.getId() != null) {
                        if (despesaTela.getId().equals(despesaUsuarioBanco.getId())) {
                            existeNaTela = true;
                            break; // Sai do loop se encontrar o ID
                        }
                    }
                }

                if (!existeNaTela) {
                    despesaUsuarioAExcluir.add(despesaUsuarioBanco);
                }
            }

            //Por fim, gravo a despesa
            despesaRepository.saveAndFlush(despesa);

            HashSet<DespesaUsuario> despesaUsuarioSet = new HashSet<>(despesaUsuarioList);


            // Agora você pode salvar os objetos que precisam ser salvos
            despesaUsuarioRepository.saveAllAndFlush(despesaUsuarioSet);

            // E excluir os objetos que precisam ser excluídos
            if(!despesaUsuarioAExcluir.isEmpty()){
                for (DespesaUsuario du : despesaUsuarioAExcluir){
                    if(du.getDespesa().getId() != null && du.getUsuario().getId() != null && du.getId() != null){
                        despesaUsuarioRepository.deleteById(du.getId());
                    }
                }
            }
        }
    }
}




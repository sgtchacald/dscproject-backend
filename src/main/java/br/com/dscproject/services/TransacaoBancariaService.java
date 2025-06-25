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
import com.webcohesion.ofx4j.domain.data.MessageSetType;
import com.webcohesion.ofx4j.domain.data.ResponseEnvelope;
import com.webcohesion.ofx4j.domain.data.ResponseMessageSet;
import com.webcohesion.ofx4j.domain.data.banking.BankAccountDetails;
import com.webcohesion.ofx4j.domain.data.banking.BankStatementResponse;
import com.webcohesion.ofx4j.domain.data.banking.BankStatementResponseTransaction;
import com.webcohesion.ofx4j.domain.data.banking.BankingResponseMessageSet;
import com.webcohesion.ofx4j.domain.data.common.Transaction;
import com.webcohesion.ofx4j.io.AggregateUnmarshaller;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
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

    public Usuario retornaUsuarioLogado(){
        String loginUsuarioToken = this.tokenService.validarToken(tokenService.recuperarToken(request));
        return this.usuarioRepository.findByLogin(loginUsuarioToken);
    }
    
    @Transactional
    public List<TransacaoBancariaDTO> buscarTodosPorUsuario() {

        List<TransacaoBancariaDTO> transacaoBancariaDTOList = new ArrayList<>();

        List<TransacaoBancaria> transacaoBancariaList = new ArrayList<>();
        transacaoBancariaList = transacaoBancariaRepository.findByInstituicaoFinanceiraUsuarioUsuario_Id(this.retornaUsuarioLogado().getId());

        for (TransacaoBancaria transacaoBancaria : transacaoBancariaList) {
            TransacaoBancariaDTO dto = new TransacaoBancariaDTO();
            BeanUtils.copyProperties(transacaoBancaria, dto);

            dto.setCodigoBanco(transacaoBancaria.getInstituicaoFinanceiraUsuario().getInstituicaoFinanceira().getCodigo());
            dto.setTipoRegistroFinanceiro(transacaoBancaria.getTipoRegistroFinanceiro().getCodigo());
            dto.setCategoriaRegistroFinanceiro((transacaoBancaria.getCategoriaRegistroFinanceiro() != null) ? transacaoBancaria.getCategoriaRegistroFinanceiro().getCodigo() : null);

            //Seta a instituição financeira usuario e a instituicao do usuario
            dto.setInstituicaoFinanceiraUsuarioId(transacaoBancaria.getInstituicaoFinanceiraUsuario().getId());

            if(transacaoBancaria.getDtLancamento() != null) {
                dto.setDtLancamento(transacaoBancaria.getDtLancamento().toString());
            }

            dto.setOfxTransacaoId(transacaoBancaria.getOfxTransacaoId());

            transacaoBancariaDTOList.add(dto);
        }

        return transacaoBancariaDTOList;
    }

    public TransacaoBancaria buscarPorId(Long id) {
        Optional<TransacaoBancaria> obj = transacaoBancariaRepository.findById(id);
        return obj.orElseThrow(()-> new ObjectNotFoundException("Objeto não encontrado!" + "Id:" + id + ", Tipo: "  + TransacaoBancaria.class.getName()));
    }

    public TransacaoBancaria inserir(TransacaoBancariaDTO data) {
        TransacaoBancaria transacaoBancaria  = new TransacaoBancaria();
        BeanUtils.copyProperties(data, transacaoBancaria);

        transacaoBancaria.setTipoRegistroFinanceiro(TipoRegistroFinanceiro.toEnum(data.getTipoRegistroFinanceiro()));
        transacaoBancaria.setCategoriaRegistroFinanceiro(CategoriaRegistroFinanceiro.toEnum(data.getCategoriaRegistroFinanceiro()));

        //transacaoBancaria.setDtLancamento(Instant.now());

        Optional<InstituicaoFinanceiraUsuario> ifu  = this.instituicaoFinanceiraUsuarioRepository.findById(data.getInstituicaoFinanceiraUsuarioId());
        if(ifu.isEmpty()){
            throw new ObjectNotFoundException("Não existe uma instituição financeira vinculada ao usuário com o id " + data.getInstituicaoFinanceiraUsuarioId() + ".");
        }

        InstituicaoFinanceiraUsuario instituicaoFinanceiraUsuario = ifu.get();

        transacaoBancaria.setInstituicaoFinanceiraUsuario(instituicaoFinanceiraUsuario);

        //Seta Auditoria
        transacaoBancaria.setCriadoPor(this.retornaUsuarioLogado().getLogin());

        //Por fim, gravo a transacaoBancaria
        transacaoBancariaRepository.saveAndFlush(transacaoBancaria);

        //Retorno a transacaoBancaria
        return transacaoBancaria;
    }

    @Transactional
    public void editar(TransacaoBancariaDTO data) throws ObjectNotFoundException {

        //Busca o registro Financeiro do banco e altero seus campos que não são chaves
        TransacaoBancaria transacaoBancariaDB = this.buscarPorId(data.getId());

        transacaoBancariaDB.setDescricao(data.getDescricao());
        transacaoBancariaDB.setValor(data.getValor());
        transacaoBancariaDB.setTipoRegistroFinanceiro(TipoRegistroFinanceiro.toEnum(data.getTipoRegistroFinanceiro()));
        transacaoBancariaDB.setCategoriaRegistroFinanceiro(CategoriaRegistroFinanceiro.toEnum(data.getCategoriaRegistroFinanceiro()));
        transacaoBancariaDB.setAlteradoPor(this.retornaUsuarioLogado().getLogin()); //Auditoria

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

    public String importarDadosBancariosOfx(MultipartFile file, String competencia, String bancoCodigo) {

        if(this.retornaUsuarioLogado() == null){
            throw new RuntimeException("Usuário não está logado, por favor, faça Login.");
        }

        InstituicaoFinanceiraUsuario instituicaoFinanceiraUsuario = this.instituicaoFinanceiraUsuarioRepository.findByUsuario_IdAndInstituicaoFinanceira_Codigo(this.retornaUsuarioLogado().getId(), bancoCodigo);
        if(instituicaoFinanceiraUsuario == null){
            throw new RuntimeException("Não foi encontrada a instituição financeira de codigo " + bancoCodigo + " vinculada a este usuario");
        }

        try {
            InputStreamReader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
            AggregateUnmarshaller a = new AggregateUnmarshaller(ResponseEnvelope.class);
            ResponseEnvelope re = (ResponseEnvelope) a.unmarshal(reader);

            MessageSetType type = MessageSetType.banking;
            ResponseMessageSet message = re.getMessageSet(type);

            List<TransacaoBancaria> transacaoBancariaList = new ArrayList<>();

            if(message != null){
                TransacaoBancariaDTO dto = new TransacaoBancariaDTO();
                List<BankStatementResponseTransaction> bancoList = ((BankingResponseMessageSet) message).getStatementResponses();

                if(bancoList.isEmpty()){
                    throw new RuntimeException("Não foi possível obter resposta do conteúdo do arquivo, verifique se é um arquivo OFX válido.");
                }

                for(BankStatementResponseTransaction banco : bancoList){

                    BankStatementResponse response = banco.getMessage();

                    if(response == null){
                        throw new RuntimeException("Não foi possível obter mensagem de resposta do arquivo, verifique se é um arquivo OFX válido.");
                    }

                    BankAccountDetails contaBancaria = response.getAccount();

                    List<Transaction> transacoes = response.getTransactionList().getTransactions();
                    if(transacoes.isEmpty()){
                        throw new RuntimeException("Não existem transações nesse arquivo OFX.");
                    }

                    for(Transaction tb : transacoes){
                      TransacaoBancaria transacaoBancaria = new TransacaoBancaria();
                      transacaoBancaria.setId(null);
                      transacaoBancaria.setDescricao(tb.getMemo());
                      transacaoBancaria.setValor(BigDecimal.valueOf(tb.getAmount()));
                      transacaoBancaria.setDtLancamento(tb.getDatePosted());
                      transacaoBancaria.setOfxTransacaoId(tb.getId());
                      transacaoBancaria.setTipoRegistroFinanceiro(TipoRegistroFinanceiro.retornaEnumOFX(tb.getTransactionType().toString()));
                      transacaoBancaria.setCategoriaRegistroFinanceiro(null);
                      transacaoBancaria.setInstituicaoFinanceiraUsuario(instituicaoFinanceiraUsuario);

                      transacaoBancaria.setCriadoPor(this.retornaUsuarioLogado().getLogin());
                      transacaoBancaria.setDataCriacao(Instant.now());

                      transacaoBancaria.setAlteradoPor(null);
                      transacaoBancaria.setDataAlteracao(null);

                      transacaoBancariaList.add(transacaoBancaria);
                    }
                }
            }

            if(transacaoBancariaList.isEmpty()){
                throw new RuntimeException("Não foi possível obter as transações do arquivo OFX.");
            }

            transacaoBancariaRepository.saveAllAndFlush(transacaoBancariaList);

        } catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("Não foi possível fazer upload do arquivo OFX.");
        }

        return "Arquivo OFX Importado com sucesso!";
    }

}

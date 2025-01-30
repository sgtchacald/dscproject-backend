package br.com.dscproject.dto;

import br.com.dscproject.enums.CategoriaRegistroFinanceiro;
import br.com.dscproject.enums.StatusPagamento;
import br.com.dscproject.enums.TipoRegistroFinanceiro;
import br.com.dscproject.validation.constraints.RegistroFinanceiro;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RegistroFinanceiro
@Data
public class RegistroFinanceiroDTO {

    private Long id;

    private String descricao;

    private BigDecimal valor;

    private int qtdParcela;

    private String dtVencimento;

    //private int diaVencimento;

    private CategoriaRegistroFinanceiro categoriaRegistroFinanceiro;

    private TipoRegistroFinanceiro tipoRegistroFinanceiro;

    private StatusPagamento statusPagamento;

    private Long instituicaoFinanceiraUsuarioId;

    private List<Long> usuariosResponsaveis = new ArrayList<Long>();

}
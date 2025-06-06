package br.com.dscproject.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class TransacaoBancariaDTO {

    private Long id;

    private String descricao;

    private BigDecimal valor;

    private String dtLancamento;

    private String tipoRegistroFinanceiro;

    private String categoriaRegistroFinanceiro;

    private Long instituicaoFinanceiraUsuarioId;

}
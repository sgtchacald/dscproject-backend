package br.com.dscproject.dto;

import br.com.dscproject.enums.CategoriaRegistroFinanceiro;
import br.com.dscproject.enums.StatusPagamento;
import br.com.dscproject.enums.TipoRegistroFinanceiro;
import br.com.dscproject.validation.constraints.Despesa;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class ReceitaDTO {

    private Long id;

    private String competencia;

    private String nome;

    private String descricao;

    private BigDecimal valor;

    private String dtLancamento;

    private CategoriaRegistroFinanceiro categoriaRegistroFinanceiro;

    private TipoRegistroFinanceiro tipoRegistroFinanceiro;

    private Long instituicaoFinanceiraUsuarioId;

    private Long instituicaoFinanceiraId;

}
package br.com.dscproject.dto;

import br.com.dscproject.enums.CategoriaRegistroFinanceiro;
import br.com.dscproject.enums.StatusPagamento;
import br.com.dscproject.enums.TipoRegistroFinanceiro;
import br.com.dscproject.validation.constraints.Despesa;
import jakarta.persistence.Column;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Despesa
@Data
public class DespesaDTO {

    private Long id;

    private String competencia;

    private String nome;

    private String descricao;

    private String dtVencimento;

    private String dtLancamento;

    private boolean existeParcela;

    private Long idParcelaPai;

    private int nrParcela;

    private int qtdParcela;

    private BigDecimal valorParcelado;

    private BigDecimal valorTotalADividir;

    private BigDecimal valor;

    private CategoriaRegistroFinanceiro categoriaRegistroFinanceiro;

    private TipoRegistroFinanceiro tipoRegistroFinanceiro;

    private StatusPagamento statusPagamento;

    private Long instituicaoFinanceiraId;

    private Long instituicaoFinanceiraUsuarioId;

    private String instituicaoFinanceira;

    private List<UsuarioResponsavelDTO> usuariosResponsaveis = new ArrayList<UsuarioResponsavelDTO>();

}
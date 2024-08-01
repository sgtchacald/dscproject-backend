package br.com.dscproject.dto;

import br.com.dscproject.enums.TipoReceitaDespesa;
import br.com.dscproject.enums.TipoRegistroFinanceiro;
import br.com.dscproject.validation.constraints.RegistroFinanceiro;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RegistroFinanceiro
@Data
public class RegistroFinanceiroDTO {

    private Long id;

    @Length(max=100, message="O campo deve ter no máximo 100 caracteres.")
    private String descricao;

    private BigDecimal valor;

    private int qtdParcela;

    private String dtVencimento;

    private int diaVencimento;

    private TipoRegistroFinanceiro tipoRegistroFinanceiro;

    private TipoReceitaDespesa tipoReceitaDespesa;

    private Long instituicaoFinanceiraUsuarioId;

    private List<Long> usuariosResponsaveis = new ArrayList<Long>();

}
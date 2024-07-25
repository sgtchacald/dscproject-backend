package br.com.dscproject.dto;

import br.com.dscproject.domain.InstituicaoFinanceiraUsuario;
import br.com.dscproject.domain.Usuario;
import br.com.dscproject.enums.TipoReceita;
import br.com.dscproject.enums.TipoRegistroFinanceiro;
import br.com.dscproject.validation.constraints.UsuarioNovo;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RegistroFinanceiroDTO {

    private Long id;

    @NotEmpty(message="Preenchimento Obrigatório.")
    @Length(max=100, message="Este campo deve ter no máximo 100 caracteres.")
    private String descricao;

    @NotNull(message="Preenchimento Obrigatório.")
    @DecimalMin(value = "0.01", message="Este campo deve igual a zero.")
    private BigDecimal valor;

    private String dtVencimento;

    @NotNull(message="Preenchimento Obrigatório.")
    private int qtdParcela;

    private TipoRegistroFinanceiro tipoRegistroFinanceiro;

    private TipoReceita tipoReceita;

    @NotNull(message="Preenchimento Obrigatório.")
    private Long instituicaoFinanceiraUsuarioId;

    @NotNull(message="Preenchimento Obrigatório.")
    private List<Long> usuariosResponsaveis = new ArrayList<Long>();

}
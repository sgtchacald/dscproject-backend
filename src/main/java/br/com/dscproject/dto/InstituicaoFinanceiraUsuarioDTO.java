package br.com.dscproject.dto;

import br.com.dscproject.domain.InstituicaoFinanceira;
import br.com.dscproject.domain.Usuario;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InstituicaoFinanceiraUsuarioDTO {
    private Long id;

    @NotEmpty(message="Preenchimento Obrigatório.")
    @Length(max=30, message="Este campo deve ter no máximo 30 caracteres.")
    private String agencia;

    @NotEmpty(message="Preenchimento Obrigatório.")
    @Length(max=30, message="Este campo deve ter no máximo 30 caracteres.")
    private String conta;

    @NotEmpty(message="Preenchimento Obrigatório.")
    @Length(max=100, message="Este campo deve ter no máximo 100 caracteres.")
    private String nomeGerente;

    @NotEmpty(message="Preenchimento Obrigatório.")
    @Length(max=20, message="Este campo deve ter no máximo 20 caracteres.")
    private String telefoneGerente;

    private InstituicaoFinanceiraDTO instituicaoFinanceira;
}

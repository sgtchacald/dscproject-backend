package br.com.dscproject.dto;

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
public class CredenciaisDTO {
    @NotEmpty(message="Preenchimento Obrigatório")
    @Length(max=40, message="Este campo deve ter no máximo 40 caracteres.")
    private String login;

    @NotEmpty(message="Preenchimento Obrigatório")
    @Length(max=60, message="Este campo deve ter no máximo 60 caracteres.")
    private String senha;
}

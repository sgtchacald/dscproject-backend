package br.com.dscproject.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UsuarioNovoDTO {

    @NotEmpty(message="Preenchimento Obrigatório.")
    @Length(max=100, message="Este campo deve ter no máximo 100 caracteres.")
    private String nome;

    @NotEmpty(message="Preenchimento Obrigatório.")
    @Length(max=512, message="Este campo deve ter no máximo 512 caracteres.")
    @Email(message="Este campo deve ter um e-mail válido.")
    private String email;

    @NotEmpty(message="Preenchimento Obrigatório")
    @Length(min=4, max=40, message="Este campo deve ter no no mínimo 4 e no máximo 40 caracteres.")
    private String login;

    @NotEmpty(message="Preenchimento Obrigatório")
    @Length(max=60, message="Este campo deve ter no máximo 60 caracteres.")
    private String senha;
}

package br.com.dscproject.dto;

import br.com.dscproject.model.Usuario;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {

    private Long id;

    @NotEmpty(message="Preenchimento Obrigatório.")
    @Length(min=5, max=80, message="O tamanho deve ser entre 5 e 80 caracteres.")
    private String nome;

    @NotEmpty(message="Preenchimento Obrigatório.")
    @Length(min=1, max=1, message="O tamanho deve ser 1 caracter.")
    private String genero;

    @NotEmpty(message="Preenchimento Obrigatório.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss",timezone = "America/Sao_Paulo")
    private Date nascimento;

    @NotEmpty(message="Preenchimento obrigatório.")
    @Email(message="Email inválido.")
    private String email;

    @NotEmpty(message="Preenchimento Obrigatório.")
    @Length(min=5, max=45, message="O tamanho deve ser entre 5 e 45 caracteres.")
    private String login;

    @NotEmpty(message="Preenchimento Obrigatório.")
    @Length(min=6, max=25, message="O tamanho deve ser entre 6 e 25 caracteres.")
    private String senha;

    public UsuarioDTO(Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.genero = usuario.getGenero();
        this.nascimento = usuario.getNascimento();
        this.email = usuario.getEmail();
        this.login = usuario.getLogin();
        this.senha = usuario.getSenha();
    }

}

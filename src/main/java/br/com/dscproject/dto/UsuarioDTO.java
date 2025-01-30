package br.com.dscproject.dto;

import br.com.dscproject.enums.Genero;
import br.com.dscproject.enums.Perfis;
import br.com.dscproject.domain.Usuario;
import br.com.dscproject.validation.constraints.UsuarioNovo;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@UsuarioNovo
public class UsuarioDTO {

    private Long id;

    @NotEmpty(message="Preenchimento Obrigatório.")
    @Length(max=100, message="Este campo deve ter no máximo 100 caracteres.")
    private String nome;

    private Genero genero;

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

    private Perfis perfil;


    public UsuarioDTO(Usuario usuario) {
        this.id     = usuario.getId();
        this.nome   = usuario.getNome();
        this.genero = usuario.getGenero();
        this.email  = usuario.getEmail();
        this.login  = usuario.getLogin();
        this.senha  = usuario.getSenha();
        this.perfil = usuario.getPerfil();
    }
}

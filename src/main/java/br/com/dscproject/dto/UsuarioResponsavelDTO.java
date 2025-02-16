package br.com.dscproject.dto;

import br.com.dscproject.domain.Usuario;
import br.com.dscproject.enums.Genero;
import br.com.dscproject.enums.Perfis;
import br.com.dscproject.validation.constraints.UsuarioNovo;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@Data
public class UsuarioResponsavelDTO {

    private Long id;

    private String nome;

    private String login;

    private String email;

    public UsuarioResponsavelDTO(Long id, String nome, String login, String email) {
        this.id = id;
        this.nome = nome;
        this.login = login;
        this.email = email;
    }

}

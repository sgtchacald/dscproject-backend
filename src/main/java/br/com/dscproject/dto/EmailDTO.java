package br.com.dscproject.dto;

import br.com.dscproject.domain.Usuario;
import br.com.dscproject.enums.Perfis;
import br.com.dscproject.validation.constraints.UsuarioNovo;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class EmailDTO {

    @NotEmpty(message="Preenchimento Obrigatório.")
    @Email(message="Este campo deve ter um e-mail válido.")
    private String email;

}

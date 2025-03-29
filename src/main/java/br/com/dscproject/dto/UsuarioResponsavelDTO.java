package br.com.dscproject.dto;

import br.com.dscproject.domain.Usuario;
import br.com.dscproject.enums.Genero;
import br.com.dscproject.enums.Perfis;
import br.com.dscproject.validation.constraints.UsuarioNovo;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;

@Data
public class UsuarioResponsavelDTO {

    private Long id;

    private String nome;

    private String login;

    private String email;

    private String genero;

    private BigDecimal valorDividido;

    private boolean statusPagamento;

    public UsuarioResponsavelDTO(Long id, String nome, String login, String email, String genero, BigDecimal valorDividido, boolean statusPagamento) {
        this.id = id;
        this.nome = nome;
        this.login = login;
        this.email = email;
        this.genero = genero;
        this.valorDividido = valorDividido;
        this.statusPagamento = statusPagamento;
    }

}

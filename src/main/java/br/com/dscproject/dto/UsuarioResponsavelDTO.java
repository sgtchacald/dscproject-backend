package br.com.dscproject.dto;

import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponsavelDTO {

    private Long id;

    private String nome;

    private String login;

    private String email;

    private String genero;

    private BigDecimal valorDividido;

    private Boolean statusPagamento = Boolean.FALSE;

    private Boolean logado = Boolean.FALSE;

}

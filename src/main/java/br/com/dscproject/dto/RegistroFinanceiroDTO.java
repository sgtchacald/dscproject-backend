package br.com.dscproject.dto;

import br.com.dscproject.domain.InstituicaoFinanceiraUsuario;
import br.com.dscproject.domain.Usuario;
import br.com.dscproject.enums.Genero;
import br.com.dscproject.enums.Perfis;
import br.com.dscproject.enums.TipoEntrada;
import br.com.dscproject.enums.TipoTransacao;
import br.com.dscproject.validation.constraints.UsuarioNovo;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@UsuarioNovo
public class RegistroFinanceiroDTO {

    private Long id;

    private String descricao;

    private BigDecimal valor;

    private LocalDate dtLancamento;

    private LocalDate dtVencimento;

    private int qtdParcela;

    private TipoTransacao tipoTransacao;

    private TipoEntrada tipoEntrada;

    private InstituicaoFinanceiraUsuario instituicaoFinanceiraUsuario;

}

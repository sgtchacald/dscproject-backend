package br.com.dscproject.dto;

import br.com.dscproject.domain.InstituicaoFinanceira;
import br.com.dscproject.domain.Usuario;
import br.com.dscproject.enums.Genero;
import br.com.dscproject.enums.Perfis;
import br.com.dscproject.enums.TipoInstituicaoFinanceira;
import br.com.dscproject.validation.constraints.UsuarioNovo;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.UniqueElements;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor
public class InstituicaoFinanceiraDTO {

    private Long id;

    private String nome;

    private String codigo;

    private TipoInstituicaoFinanceira tipoInstituicao;

    public InstituicaoFinanceiraDTO(InstituicaoFinanceira instituicaoFinanceira) {
        this.nome = instituicaoFinanceira.getNome();
        this.codigo = instituicaoFinanceira.getCodigo();
        this.tipoInstituicao = instituicaoFinanceira.getTipoInstituicao();
    }
}

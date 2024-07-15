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

    @NotEmpty(message="Preenchimento Obrigatório.")
    @Length(max=100, message="Este campo deve ter no máximo 100 caracteres.")
    private String nome;

    private TipoInstituicaoFinanceira tipoInstituicao;

    public InstituicaoFinanceiraDTO(InstituicaoFinanceira instituicaoFinanceira) {
        this.nome = instituicaoFinanceira.getNome();
        this.tipoInstituicao = instituicaoFinanceira.getTipoInstituicao();
    }
}

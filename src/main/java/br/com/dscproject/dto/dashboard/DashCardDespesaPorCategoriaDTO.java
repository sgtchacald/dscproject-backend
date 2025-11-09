package br.com.dscproject.dto.dashboard;

import br.com.dscproject.enums.CategoriaRegistroFinanceiro;
import lombok.Data;

@Data
public class DashCardDespesaPorCategoriaDTO {

    private CategoriaRegistroFinanceiro categoria;
    private String nome;
    private Long valor;

    public DashCardDespesaPorCategoriaDTO(CategoriaRegistroFinanceiro categoria, Long valor) {
        this.categoria = categoria;
        this.valor = valor;
        this.nome = categoria.getDescricao();
    }


}
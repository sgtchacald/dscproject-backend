package br.com.dscproject.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public enum TipoRegistroFinanceiro {

    RECEITA("R", "Receita"),
    DESPESA("D", "Despesa");

    @Getter
    @Setter
    private String codigo;

    @Getter
    @Setter
    private String descricao;

    public static TipoRegistroFinanceiro toEnum(String codigo){

        if(!codigo.isBlank())
            for(TipoRegistroFinanceiro x : TipoRegistroFinanceiro.values()) {
                if(codigo.equals(x.getCodigo())) {
                    return x;
                }
            }

        throw new IllegalArgumentException("Código: " + codigo + " inválido");
    }

}

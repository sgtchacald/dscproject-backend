package br.com.dscproject.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public enum TipoInstituicaoFinanceira {
    BANCO("B", "Banco"),
    FINANCEIRA("F", "Financeira");

    @Getter
    @Setter
    private String codigo;

    @Getter
    @Setter
    private String descricao;

    public static Genero toEnum(String codigo){

        if(!codigo.isBlank())
            for(Genero x : Genero.values()) {
                if(codigo.equals(x.getCodigo())) {
                    return x;
                }
            }

        throw new IllegalArgumentException("Código: " + codigo + " inválido");
    }
}


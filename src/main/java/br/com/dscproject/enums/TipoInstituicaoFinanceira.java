package br.com.dscproject.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public enum TipoInstituicaoFinanceira {
    BANCO("B", "Banco"),
    CORRETORA("C", "Corretora");


    @Setter
    private String codigo;

    @Setter
    private String descricao;

    public static TipoInstituicaoFinanceira toEnum(String codigo){

        if(!codigo.isBlank())
            for(TipoInstituicaoFinanceira x : TipoInstituicaoFinanceira.values()) {
                if(codigo.equals(x.getCodigo())) {
                    return x;
                }
            }

        throw new IllegalArgumentException("Código: " + codigo + " inválido");
    }

}


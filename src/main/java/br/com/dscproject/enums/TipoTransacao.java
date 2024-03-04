package br.com.dscproject.enums;

import lombok.*;
@AllArgsConstructor
public enum TipoTransacao {

    ENTRADA("E", "Entrada"),
    SAIDA("S", "Saida");

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

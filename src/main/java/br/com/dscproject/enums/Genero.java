package br.com.dscproject.enums;

import lombok.*;

@AllArgsConstructor
public enum Genero {

    FEMININO("F", "Feminino"),
    MASCULINO("M", "Masculino"),
    OUTRO("O", "Outro");

    @Getter
    @Setter
    private String codigo;

    @Getter
    @Setter
    private String descricao;

    public static Genero toEnum(String codigo){

        if(codigo == null) {
            return null;
        }

        for(Genero x : Genero.values()) {
            if(codigo.equals(x.getCodigo())) {
                return x;
            }
        }

        throw new IllegalArgumentException("Código inválido" + codigo);
    }

}

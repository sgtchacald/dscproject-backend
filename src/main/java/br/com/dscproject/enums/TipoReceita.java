package br.com.dscproject.enums;

import lombok.*;
@AllArgsConstructor
public enum TipoReceita {

    SALARIO("S", "Pagamento"),
    SALARIO_DECIMO_TERCEIRO("'D'", "13º Salário"),
    EXTRA("E", "Extra"),
    FERIAS("F", "Férias"),
    INVESTIMENTO("I", "Investimento"),
    OUTRO("O", "Investimento");

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

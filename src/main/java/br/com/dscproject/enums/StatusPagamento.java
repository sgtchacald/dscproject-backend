package br.com.dscproject.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public enum StatusPagamento {

    SIM("SIM", "Sim"),
    NAO("NAO", "Não"),
    NAO_SE_APLICA("NAO_SE_APLICA", "Não Se Aplica");

    @Getter
    @Setter
    private String codigo;

    @Getter
    @Setter
    private String descricao;

    public static StatusPagamento toEnum(String codigo){

        if(!codigo.isBlank())
            for(StatusPagamento x : StatusPagamento.values()) {
                if(codigo.equals(x.getCodigo())) {
                    return x;
                }
            }

        throw new IllegalArgumentException("Código: " + codigo + " inválido");
    }

}

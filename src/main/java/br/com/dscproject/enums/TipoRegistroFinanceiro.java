package br.com.dscproject.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.lang.runtime.SwitchBootstraps;

@AllArgsConstructor
public enum TipoRegistroFinanceiro {

    RECEITA("R", "Receita"),
    DESPESA("D", "Despesa"),
    CREDITO("C", "Crédito"),
    DEBITO("D", "Débito"),
    ENTRADA("E", "Entrada"),
    SAIDA("S", "Saída");

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

    public static TipoRegistroFinanceiro retornaEnumOFX(String codigo){
        switch (codigo){
            case "DEBIT":
                return TipoRegistroFinanceiro.DEBITO;
            case "CREDIT":
                return TipoRegistroFinanceiro.CREDITO;
            default:
                return null;
        }
    }

}

package br.com.dscproject.enums;

import lombok.*;
@AllArgsConstructor
public enum TipoReceitaDespesa {

    //Receitas
    SALARIO("SALARIO", "Pagamento"),
    SALARIO_DECIMO_TERCEIRO("SALARIO_DECIMO_TERCEIRO", "13º Salário"),
    EXTRA("EXTRA", "Extra"),
    FERIAS("FERIAS", "Férias"),
    INVESTIMENTO("INVESTIMENTO", "Investimento"),

    //Despesas
    MORADIA("MORADIA", "Moradia"),
    ALIMENTACAO("ALIMENTACAO", "Alimentação"),
    LAZER("LAZER", "Lazer"),
    VESTUARIO("VESTUARIO", "Vestuário"),
    TRANSPORTE("TRANSPORTE", "Transporte"),
    CARRO("CARRO", "Carro"),
    SAUDE("SAUDE", "Saúde"),
    EDUCACAO("EDUCACAO", "Educação"),
    SERVICOS("SERVICOS", "Serviços"),
    EMPRESTIMOS("EMPRESTIMOS", "Empréstimos"),
    OUTRO("OUTRO", "Outro");

    @Getter
    @Setter
    private String codigo;

    @Getter
    @Setter
    private String descricao;

    public static TipoReceitaDespesa toEnum(String codigo){

        if(!codigo.isBlank())
            for(TipoReceitaDespesa x : TipoReceitaDespesa.values()) {
                if(codigo.equals(x.getCodigo())) {
                    return x;
                }
            }

        throw new IllegalArgumentException("Código: " + codigo + " inválido");
    }

}

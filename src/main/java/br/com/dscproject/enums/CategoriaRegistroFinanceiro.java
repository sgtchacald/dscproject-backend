package br.com.dscproject.enums;

import lombok.*;
@AllArgsConstructor
public enum CategoriaRegistroFinanceiro {

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
    VESTUARIO("VESTUARIO", ""),
    TRANSPORTE("TRANSPORTE", ""),
    CARRO("CARRO", "Carro"),
    SAUDE("SAUDE", "Saúde"),
    EDUCACAO("EDUCACAO", "Educação"),
    SERVICOS("SERVICOS", "Serviços"),
    EMPRESTIMOS("EMPRESTIMOS", "Empréstimos"),
    CARTAO_DE_CREDITO("CARTAO_DE_CREDITO", "Cartão de Crédito"),
    TAXAS_EMPRESA("TAXAS_EMPRESA", "Taxas PJ"),
    OUTRO("OUTRO", "Outro");

    @Getter
    @Setter
    private String codigo;

    @Getter
    @Setter
    private String descricao;

    public static CategoriaRegistroFinanceiro toEnum(String codigo){

        if(!codigo.isBlank())
            for(CategoriaRegistroFinanceiro x : CategoriaRegistroFinanceiro.values()) {
                if(codigo.equals(x.getCodigo())) {
                    return x;
                }
            }

        throw new IllegalArgumentException("Código: " + codigo + " inválido");
    }

}

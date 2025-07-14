package br.com.dscproject.dto;

import br.com.dscproject.enums.CategoriaRegistroFinanceiro;
import br.com.dscproject.enums.TipoRegistroFinanceiro;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardCardSaldoDTO {

    private String valor;

}
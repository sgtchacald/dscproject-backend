package br.com.dscproject.dto;

import lombok.Data;

import java.util.List;

@Data
public class DespesaCompartilharDTO {

    private List<Long> idDespesaList;
    private List<Long> idusuariosACompartilharList;

}
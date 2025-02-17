package br.com.dscproject.repository;

import br.com.dscproject.domain.Despesa;
import br.com.dscproject.domain.Receita;
import br.com.dscproject.domain.Usuario;

import java.util.List;

public interface ReceitaRepositoryCustom {

    List<Receita> buscarReceitaPorUsuario(Usuario usuario);

}

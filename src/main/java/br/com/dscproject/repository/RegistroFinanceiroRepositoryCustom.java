package br.com.dscproject.repository;

import br.com.dscproject.domain.RegistroFinanceiro;
import br.com.dscproject.domain.Usuario;

import java.util.List;

public interface RegistroFinanceiroRepositoryCustom {
    List<RegistroFinanceiro> buscarRegistroFinanceiroPorUsuario(Usuario usuario);
}

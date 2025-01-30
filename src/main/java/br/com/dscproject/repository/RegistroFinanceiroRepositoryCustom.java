package br.com.dscproject.repository;

import br.com.dscproject.domain.RegistroFinanceiro;
import br.com.dscproject.domain.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface RegistroFinanceiroRepositoryCustom {

    List<RegistroFinanceiro> buscarRegistroFinanceiroPorUsuario(Usuario usuario);

}

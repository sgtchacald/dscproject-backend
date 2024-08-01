package br.com.dscproject.repository;

import br.com.dscproject.domain.RegistroFinanceiro;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistroFinanceiroRepository extends CrudRepository<RegistroFinanceiro, Long> {
}

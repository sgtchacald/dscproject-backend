package br.com.dscproject.repository;

import br.com.dscproject.domain.InstituicaoFinanceira;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InstituicaoFinanceiraRepository extends CrudRepository<InstituicaoFinanceira, Long> {
    InstituicaoFinanceira findByNome(String nome);

    Optional<InstituicaoFinanceira> findById(Long id);

}

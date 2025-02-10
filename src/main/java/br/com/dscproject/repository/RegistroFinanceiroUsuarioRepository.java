package br.com.dscproject.repository;

import br.com.dscproject.domain.InstituicaoFinanceira;
import br.com.dscproject.domain.RegistroFinanceiro;
import br.com.dscproject.domain.RegistroFinanceiroUsuario;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistroFinanceiroUsuarioRepository extends CrudRepository<RegistroFinanceiroUsuario, Long> {

    List<RegistroFinanceiroUsuario> findByRegistroFinanceiro(RegistroFinanceiro registroFinanceiro);

}

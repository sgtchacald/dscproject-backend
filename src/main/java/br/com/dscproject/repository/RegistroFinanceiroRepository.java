package br.com.dscproject.repository;

import br.com.dscproject.domain.RegistroFinanceiro;
import br.com.dscproject.domain.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface RegistroFinanceiroRepository extends CrudRepository<RegistroFinanceiro, Long> {

    @Query
    (
        value = "SELECT u.usu_id FROM usuarios u " +
                "JOIN registro_financeiro_usuario rfu ON u.usu_id = rfu.usu_id " +
                "WHERE rfu.refi_id = :registroFinanceiroId",
        nativeQuery = true
    )
    Set<Long> findUsuariosByRegistroFinanceiroId(@Param("registroFinanceiroId") Long registroFinanceiroId);

}

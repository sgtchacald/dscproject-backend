package br.com.dscproject.repository;

import br.com.dscproject.domain.RegistroFinanceiro;
import br.com.dscproject.domain.Usuario;
import br.com.dscproject.dto.UsuarioResponsavelDTO;
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
        value = "SELECT u.usu_id, u.usu_nome, u.usu_login, u.usu_email FROM usuarios u " +
                "JOIN registro_financeiro_usuario rfu ON u.usu_id = rfu.usu_id " +
                "WHERE rfu.refi_id = :registroFinanceiroId",
        nativeQuery = true
    )
    Set<UsuarioResponsavelDTO> findUsuariosByRegistroFinanceiroIdSQL(@Param("registroFinanceiroId") Long registroFinanceiroId);

    @Query("SELECT new br.com.dscproject.dto.UsuarioResponsavelDTO(u.id, u.nome, u.login, u.email) " +
            "FROM Usuario u " +
            "JOIN RegistroFinanceiroUsuario rfu ON u.id = rfu.usuario.id " +
            "WHERE rfu.registroFinanceiro.id = :registroFinanceiroId")
    Set<UsuarioResponsavelDTO> findUsuariosByRegistroFinanceiroId(@Param("registroFinanceiroId") Long registroFinanceiroId);

}

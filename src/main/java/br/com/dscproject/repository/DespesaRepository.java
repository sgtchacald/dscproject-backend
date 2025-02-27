package br.com.dscproject.repository;

import br.com.dscproject.domain.Despesa;
import br.com.dscproject.dto.UsuarioResponsavelDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface DespesaRepository extends JpaRepository<Despesa, Long> {

    @Query("SELECT new br.com.dscproject.dto.UsuarioResponsavelDTO(u.id, u.nome, u.login, u.email, u.genero, du.valor)" +
            "FROM Usuario u " +
            "JOIN DespesaUsuario du ON u.id = du.usuario.id " +
            "WHERE du.despesa.id = :despesaId")
    Set<UsuarioResponsavelDTO> findUsuariosByDespesaId(@Param("despesaId") Long despesaId);

}

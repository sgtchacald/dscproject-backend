package br.com.dscproject.repository;

import br.com.dscproject.domain.Despesa;
import br.com.dscproject.dto.UsuarioResponsavelDTO;
import br.com.dscproject.dto.UsuarioResponsavelQueryDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Repository
public interface DespesaRepository extends JpaRepository<Despesa, Long> {

    @Query("SELECT new br.com.dscproject.dto.UsuarioResponsavelQueryDTO(u.id, u.nome, u.login, u.email, u.genero, du.valor, du.statusPagamento)" +
            "FROM Usuario u " +
            "JOIN DespesaUsuario du ON u.id = du.usuario.id " +
            "WHERE du.despesa.id = :despesaId")
    Set<UsuarioResponsavelQueryDTO> findUsuariosByDespesaId(@Param("despesaId") Long despesaId);


    // Consulta JPQL com INNER JOIN entre Despesa, DespesaUsuario e Usuario
    @Query("SELECT d FROM Despesa d " +
            "JOIN DespesaUsuario du ON d.id = du.despesa.id " +
            "WHERE du.usuario.id = :usuarioId ")
    List<Despesa> findDespesasByUsuarioId(@Param("usuarioId") Long usuarioId);

    List<Despesa> findByCompetenciaAndNomeAndDescricaoAndDtLancamentoAndTransacaoIdAndInstituicaoFinanceiraUsuario_Id(String competencia, String nome, String descricao, LocalDate dtLancamento, String transacaoId, Long instituicaoFinanceiraUsuario_id );

    boolean existsByCompetenciaAndNomeAndDescricaoAndDtLancamentoAndTransacaoIdAndInstituicaoFinanceiraUsuario_Id(String competencia, String nome, String descricao, LocalDate dtLancamento, String transacaoId, Long instituicaoFinanceiraUsuario_id );

}

package br.com.dscproject.repository;

import br.com.dscproject.domain.Despesa;
import br.com.dscproject.domain.DespesaUsuario;
import br.com.dscproject.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DespesaUsuarioRepository extends JpaRepository<DespesaUsuario, Long> {

    List<DespesaUsuario> findByDespesa(Optional<Despesa> despesa);

    DespesaUsuario findByUsuarioAndDespesa(Usuario usuario, Despesa despesa);

    List<DespesaUsuario> findByUsuario_IdAndDespesa_Competencia(Long usuarioId, String despesaCompetencia);

}

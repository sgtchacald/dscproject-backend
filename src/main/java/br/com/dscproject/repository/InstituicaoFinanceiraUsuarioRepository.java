package br.com.dscproject.repository;

import br.com.dscproject.domain.InstituicaoFinanceiraUsuario;
import br.com.dscproject.domain.Usuario;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstituicaoFinanceiraUsuarioRepository extends CrudRepository<InstituicaoFinanceiraUsuario, Long> {
    List<InstituicaoFinanceiraUsuario> findByUsuario(Usuario usuario);
}

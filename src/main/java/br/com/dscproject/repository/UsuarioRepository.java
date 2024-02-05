package br.com.dscproject.repository;

import br.com.dscproject.model.Usuario;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends CrudRepository<Usuario, Long> {
    Optional<Usuario> findById(Long id);
    List<Usuario> findByLogin(String login);

}

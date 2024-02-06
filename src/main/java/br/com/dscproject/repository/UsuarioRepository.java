package br.com.dscproject.repository;

import br.com.dscproject.model.Usuario;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends CrudRepository<Usuario, Long> {
    @NonNull
    Optional<Usuario> findById(@NonNull Long id);
}

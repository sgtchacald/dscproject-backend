package br.com.dscproject.repository;

import br.com.dscproject.domain.Usuario;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends RevisionRepository<Usuario, Long, Long>,CrudRepository<Usuario, Long> {
    UserDetails findByLoginOrEmail(String login, String email);

    @NonNull
    Optional<Usuario> findById(@NonNull Long id);

    @NonNull
    UserDetails findByLogin(@NonNull String login);

    @Query("SELECT u FROM Usuario u WHERE u.login = ?1 OR u.email = ?1")
    List<Usuario> findByCredenciaisList(String valor);

    Usuario findByEmail(String email);

}

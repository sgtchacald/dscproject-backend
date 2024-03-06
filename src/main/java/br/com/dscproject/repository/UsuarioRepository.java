package br.com.dscproject.repository;

import br.com.dscproject.domain.Usuario;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends CrudRepository<Usuario, Long> {
    UserDetails findByLogin(String login);

    @NonNull
    Optional<Usuario> findById(@NonNull Long id);

    @Query("SELECT u FROM Usuario u WHERE u.login = ?1 OR u.email = ?1")
    List<Usuario> findByCredenciaisList(String valor);

    Usuario findByEmail(String email);

}

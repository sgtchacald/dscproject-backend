package br.com.dscproject.repository;

import br.com.dscproject.domain.Pagamento;
import br.com.dscproject.domain.Usuario;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PagamentoRepository extends CrudRepository<Pagamento, Long> {
}

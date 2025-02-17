package br.com.dscproject.repository;

import br.com.dscproject.domain.RegistroFinanceiro;
import br.com.dscproject.domain.RegistroFinanceiroUsuario;
import br.com.dscproject.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistroFinanceiroUsuarioRepository extends JpaRepository<RegistroFinanceiroUsuario, Long> {

    List<RegistroFinanceiroUsuario> findByRegistroFinanceiro(Optional<RegistroFinanceiro> registroFinanceiro);
    RegistroFinanceiroUsuario findByUsuarioAndRegistroFinanceiro(Usuario usuario, RegistroFinanceiro registroFinanceiro);

}

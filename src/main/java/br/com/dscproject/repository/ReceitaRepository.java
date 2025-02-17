package br.com.dscproject.repository;

import br.com.dscproject.domain.Despesa;
import br.com.dscproject.domain.Receita;
import br.com.dscproject.dto.UsuarioResponsavelDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface ReceitaRepository extends JpaRepository<Receita, Long> {

}

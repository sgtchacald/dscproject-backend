package br.com.dscproject.repository;

import br.com.dscproject.domain.TransacaoBancaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransacaoBancariaRepository extends JpaRepository<TransacaoBancaria, Long> {

    List<TransacaoBancaria> findByInstituicaoFinanceiraUsuarioUsuario_Id(Long usuarioId);

}

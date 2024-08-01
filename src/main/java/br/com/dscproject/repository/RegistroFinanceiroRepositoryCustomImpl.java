package br.com.dscproject.repository;

import br.com.dscproject.domain.InstituicaoFinanceiraUsuario;
import br.com.dscproject.domain.RegistroFinanceiro;
import br.com.dscproject.domain.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class RegistroFinanceiroRepositoryCustomImpl implements RegistroFinanceiroRepositoryCustom {


    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<RegistroFinanceiro> buscarRegistroFinanceiroPorUsuario(Usuario usuario) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<RegistroFinanceiro> criteriaQuery = cb.createQuery(RegistroFinanceiro.class);
        Root<RegistroFinanceiro> registroFinanceiro = criteriaQuery.from(RegistroFinanceiro.class);

        // Aqui eu configuro os joins
        Join<RegistroFinanceiro, InstituicaoFinanceiraUsuario> instituicaoJoin = registroFinanceiro.join("instituicaoFinanceiraUsuario");
        Join<InstituicaoFinanceiraUsuario, Usuario> usuarioJoin = instituicaoJoin.join("usuario");

        // Aqui eu configuro o where
        criteriaQuery.where(cb.equal(usuarioJoin.get("id"), usuario.getId()));

        TypedQuery<RegistroFinanceiro> query = entityManager.createQuery(criteriaQuery);
        List<RegistroFinanceiro> resultado = query.getResultList();

        return resultado.isEmpty() ? new ArrayList<>() : resultado;
    }
}

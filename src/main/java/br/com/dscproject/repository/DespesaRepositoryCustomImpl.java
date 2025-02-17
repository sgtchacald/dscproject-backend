package br.com.dscproject.repository;

import br.com.dscproject.domain.InstituicaoFinanceiraUsuario;
import br.com.dscproject.domain.Despesa;
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
public class DespesaRepositoryCustomImpl implements DespesaRepositoryCustom {


    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Despesa> buscarDespesaPorUsuario(Usuario usuario) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Despesa> criteriaQuery = cb.createQuery(Despesa.class);
        Root<Despesa> registroFinanceiro = criteriaQuery.from(Despesa.class);

        // Aqui eu configuro os joins
        Join<Despesa, InstituicaoFinanceiraUsuario> instituicaoJoin = registroFinanceiro.join("instituicaoFinanceiraUsuario");
        Join<InstituicaoFinanceiraUsuario, Usuario> usuarioJoin = instituicaoJoin.join("usuario");

        // Aqui eu configuro o where
        criteriaQuery.where(cb.equal(usuarioJoin.get("id"), usuario.getId()));

        TypedQuery<Despesa> query = entityManager.createQuery(criteriaQuery);
        List<Despesa> resultado = query.getResultList();

        return resultado.isEmpty() ? new ArrayList<>() : resultado;
    }
}

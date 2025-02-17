package br.com.dscproject.repository;

import br.com.dscproject.domain.Receita;
import br.com.dscproject.domain.InstituicaoFinanceiraUsuario;
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
public class ReceitaRepositoryCustomImpl implements ReceitaRepositoryCustom {


    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Receita> buscarReceitaPorUsuario(Usuario usuario) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Receita> criteriaQuery = cb.createQuery(Receita.class);
        Root<Receita> registroFinanceiro = criteriaQuery.from(Receita.class);

        // Aqui eu configuro os joins
        Join<Receita, InstituicaoFinanceiraUsuario> instituicaoJoin = registroFinanceiro.join("instituicaoFinanceiraUsuario");
        Join<InstituicaoFinanceiraUsuario, Usuario> usuarioJoin = instituicaoJoin.join("usuario");

        // Aqui eu configuro o where
        criteriaQuery.where(cb.equal(usuarioJoin.get("id"), usuario.getId()));

        TypedQuery<Receita> query = entityManager.createQuery(criteriaQuery);
        List<Receita> resultado = query.getResultList();

        return resultado.isEmpty() ? new ArrayList<>() : resultado;
    }
}

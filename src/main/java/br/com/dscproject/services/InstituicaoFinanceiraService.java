package br.com.dscproject.services;

import br.com.dscproject.domain.InstituicaoFinanceira;
import br.com.dscproject.domain.RegistroFinanceiro;
import br.com.dscproject.domain.Usuario;
import br.com.dscproject.repository.InstituicaoFinanceiraRepository;
import br.com.dscproject.repository.PagamentoRepository;
import br.com.dscproject.repository.RegistroFinanceiroRepository;
import br.com.dscproject.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class InstituicaoFinanceiraService {

    @Autowired
    private InstituicaoFinanceiraRepository instituicaoFinanceiraRepository;

    public List<InstituicaoFinanceira> buscarTodos() {
        return (List<InstituicaoFinanceira>) instituicaoFinanceiraRepository.findAll();
    }

    @Transactional
    public InstituicaoFinanceira inserir(InstituicaoFinanceira instituicaoFinanceira) {
        List<InstituicaoFinanceira> instituicaoFinanceiraBanco = instituicaoFinanceiraRepository.findByNome(instituicaoFinanceira.getNome());

        if(!instituicaoFinanceiraBanco.isEmpty()) {
            throw new RuntimeException("A Instituição Financeira já existe.");
        }

        instituicaoFinanceiraRepository.save(instituicaoFinanceira);
        return instituicaoFinanceira;
    }

}

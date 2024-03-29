package br.com.dscproject.services;

import br.com.dscproject.domain.RegistroFinanceiro;
import br.com.dscproject.domain.Usuario;
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
public class RegistroFinanceiroService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RegistroFinanceiroRepository registroFinanceiroRepository;

    @Transactional
    public RegistroFinanceiro inserir(RegistroFinanceiro registroFinanceiro) {
        Integer qtdParcelas = registroFinanceiro.getQtdParcela() == 0 ? 1 : registroFinanceiro.getQtdParcela();

        registroFinanceiroRepository.save(registroFinanceiro);

        return registroFinanceiro;
    }

}

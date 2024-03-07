package br.com.dscproject.services;

import br.com.dscproject.domain.RegistroFinanceiro;
import br.com.dscproject.domain.Usuario;
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

    @Transactional
    public RegistroFinanceiro inserir(RegistroFinanceiro registroFinanceiro) {
        Integer qtdParcelas = (registroFinanceiro.getQtdParcela() != null &&
        ){
            log.info("Caso exista Validando se existe prestação");
        }

        log.info("Inserindo novo registro financeiro");
        usuarioRepository.save(usuario);
        log.info(usuario.toString());
        log.info("Usuário inserido com sucesso.");
        return usuario;
    }

}
package br.com.dscproject.services;

import br.com.dscproject.model.Usuario;
import br.com.dscproject.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<Usuario> buscarTodos() {
       return (List<Usuario>) usuarioRepository.findAll();
    }

    @Transactional
    public Usuario insert(Usuario usuario) {
        log.info("Inserindo novo usuário");
        usuarioRepository.save(usuario);
        log.info(usuario.toString());
        log.info("Usuário inserido com sucesso.");
        return usuario;
    }

}

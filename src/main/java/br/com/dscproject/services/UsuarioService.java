package br.com.dscproject.services;

import br.com.dscproject.domain.Usuario;
import br.com.dscproject.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<Usuario> buscarTodos() {
       return (List<Usuario>) usuarioRepository.findAll();
    }

    public boolean verificarSeExisteUsuario(String valor) {
        List<Usuario> usuario = new ArrayList<Usuario>();
        usuario = usuarioRepository.findByCredenciaisList(valor);
        return !usuario.isEmpty();
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

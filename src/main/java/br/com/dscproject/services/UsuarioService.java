package br.com.dscproject.services;

import br.com.dscproject.model.Usuario;
import br.com.dscproject.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<Usuario> buscarTodos() {
       return (List<Usuario>) usuarioRepository.findAll();
    }

}

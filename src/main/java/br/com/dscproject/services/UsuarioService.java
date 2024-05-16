package br.com.dscproject.services;

import br.com.dscproject.domain.Usuario;
import br.com.dscproject.dto.UsuarioDTO;
import br.com.dscproject.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.history.Revision;
import org.springframework.data.history.RevisionSort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<Usuario> buscarTodos() {
       return (List<Usuario>) usuarioRepository.findAll();
    }

    public boolean verificarSeExisteUsuario(String valor) {
        List<Usuario> usuario = usuarioRepository.findByCredenciaisList(valor);
        return !usuario.isEmpty();
    }

    @Transactional
    public Usuario insert(Usuario usuario) {
        usuarioRepository.save(usuario);
        return usuario;
    }

    public List<Usuario> buscarHistoricoUsuarioPorId(Long id, Pageable pageRequest) throws Exception {

        if (usuarioRepository.findById(id).isEmpty()) {
            throw new Exception("Usuario não encontrado");
        }

        List<Usuario> historicoUsuarioList = new ArrayList<Usuario>();

        try {

            Pageable pageable = PageRequest.of(pageRequest.getPageNumber(), pageRequest.getPageSize(), RevisionSort.desc());

            Page<Revision<Long, Usuario>> historicoUsuarioRevision = usuarioRepository.findRevisions(id, pageable);

            for (Revision<Long, Usuario> revision : historicoUsuarioRevision.getContent()) {
                Usuario usuario = new Usuario();
                usuario = revision.getEntity();
                usuario.setSenha(null);
                historicoUsuarioList.add(usuario);
            }

        } catch (DataAccessException ex) {
            ex.printStackTrace();
        }
        return historicoUsuarioList;
    }
}

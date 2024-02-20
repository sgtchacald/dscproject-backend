package br.com.dscproject.controller;

import br.com.dscproject.dto.UsuarioDTO;
import br.com.dscproject.dto.UsuarioNovoDTO;
import br.com.dscproject.model.Usuario;
import br.com.dscproject.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<UsuarioDTO>> buscarTodos(){
        List<Usuario> listaUsuario = usuarioService.buscarTodos();
        List <UsuarioDTO> listaUsuarioDTO = listaUsuario.stream().map(UsuarioDTO::new).collect(Collectors.toList());
        return ResponseEntity.ok().body(listaUsuarioDTO);
    }

    @RequestMapping(value="/inserir-usuario-site", method = RequestMethod.POST)
    public ResponseEntity<Usuario> insert(@Valid @RequestBody UsuarioNovoDTO usuarioNovoDTO){
        Usuario usuario  = new Usuario();

        BeanUtils.copyProperties(usuarioNovoDTO, usuario);

        usuario = usuarioService.insert(usuario);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(usuario.getId()).toUri();

        return ResponseEntity.created(uri).build();
    }

}

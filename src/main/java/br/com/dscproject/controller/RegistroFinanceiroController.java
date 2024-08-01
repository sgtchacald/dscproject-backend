package br.com.dscproject.controller;

import br.com.dscproject.domain.InstituicaoFinanceiraUsuario;
import br.com.dscproject.domain.RegistroFinanceiro;
import br.com.dscproject.domain.Usuario;
import br.com.dscproject.dto.InstituicaoFinanceiraUsuarioDTO;
import br.com.dscproject.dto.RegistroFinanceiroDTO;
import br.com.dscproject.dto.UsuarioDTO;
import br.com.dscproject.services.RegistroFinanceiroService;
import br.com.dscproject.services.UsuarioService;
import br.com.dscproject.services.exceptions.ObjectNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/registros-financeiros")
public class RegistroFinanceiroController {

    @Autowired
    RegistroFinanceiroService registroFinanceiroService;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<RegistroFinanceiro>> buscarTodos(){
        return ResponseEntity.ok().body(registroFinanceiroService.buscarTodosPorUsuario());
    }

    @RequestMapping(value="/buscar-por-id/{id}", method=RequestMethod.GET)
    public ResponseEntity<RegistroFinanceiro> buscarPorId(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok().body(registroFinanceiroService.buscarPorId(id));
    }

    @RequestMapping(value="/inserir", method = RequestMethod.POST)
    public ResponseEntity<RegistroFinanceiro> inserirUsuarioSite(@Valid @RequestBody RegistroFinanceiroDTO data){
        RegistroFinanceiro registroFinanceiro = registroFinanceiroService.inserir(data);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(registroFinanceiro.getId()).toUri();
        return ResponseEntity.created(uri).build();
    }

    @RequestMapping(value="/editar/{id}", method = RequestMethod.PUT)
    public ResponseEntity<RegistroFinanceiro> editar(@Valid @RequestBody RegistroFinanceiroDTO data, @PathVariable Long id) throws ObjectNotFoundException {
        data.setId(id);
        registroFinanceiroService.editar(data);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value="/excluir/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> excluir(@PathVariable Long id) throws ObjectNotFoundException{
        registroFinanceiroService.excluir(id);
        return ResponseEntity.noContent().build();
    }

}

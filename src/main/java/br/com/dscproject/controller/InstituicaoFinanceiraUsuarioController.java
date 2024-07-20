package br.com.dscproject.controller;

import br.com.dscproject.domain.InstituicaoFinanceiraUsuario;
import br.com.dscproject.dto.InstituicaoFinanceiraUsuarioDTO;
import br.com.dscproject.services.InstituicaoFinanceiraUsuarioService;
import br.com.dscproject.services.exceptions.ObjectNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping(value = "/instituicoes-financeiras-usuario")
public class InstituicaoFinanceiraUsuarioController {

    @Autowired
    private InstituicaoFinanceiraUsuarioService instituicaoFinanceiraUsuarioService;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<InstituicaoFinanceiraUsuario>> buscarTodos(){
        return ResponseEntity.ok().body(instituicaoFinanceiraUsuarioService.buscarTodosPorUsuario());
    }

    @RequestMapping(value="/inserir", method = RequestMethod.POST)
    public ResponseEntity<InstituicaoFinanceiraUsuario> inserir(@Valid @RequestBody InstituicaoFinanceiraUsuarioDTO data){

        InstituicaoFinanceiraUsuario instituicaoFinanceiraUsuario  = new InstituicaoFinanceiraUsuario();
        instituicaoFinanceiraUsuario = instituicaoFinanceiraUsuarioService.inserir(data);

        return ResponseEntity
                .created
                    (ServletUriComponentsBuilder
                        .fromCurrentRequest().path("/{id}")
                        .buildAndExpand(instituicaoFinanceiraUsuario.getId())
                        .toUri()
                    )
                    .build();
    }

    @RequestMapping(value="/editar/{id}", method = RequestMethod.PUT)
    public ResponseEntity<InstituicaoFinanceiraUsuario> editar(@Valid @RequestBody InstituicaoFinanceiraUsuarioDTO data, @PathVariable Long id) throws ObjectNotFoundException {
        data.setId(id);
        instituicaoFinanceiraUsuarioService.editar(data);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value="/excluir/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> excluir(@PathVariable Long id) throws ObjectNotFoundException{
        instituicaoFinanceiraUsuarioService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}

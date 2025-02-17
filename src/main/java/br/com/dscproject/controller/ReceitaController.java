package br.com.dscproject.controller;

import br.com.dscproject.domain.Receita;
import br.com.dscproject.dto.ReceitaDTO;
import br.com.dscproject.services.ReceitaService;
import br.com.dscproject.services.exceptions.ObjectNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "/receitas")
public class ReceitaController {

    @Autowired
    ReceitaService receitaService;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<ReceitaDTO>> buscarTodos(){
        return ResponseEntity.ok().body(receitaService.buscarTodosPorUsuario());
    }

    @RequestMapping(value="/buscar-por-id/{id}", method=RequestMethod.GET)
    public ResponseEntity<Receita> buscarPorId(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok().body(receitaService.buscarPorId(id));
    }

    @RequestMapping(value="/inserir", method = RequestMethod.POST)
    public ResponseEntity<Receita> inserir(@Valid @RequestBody ReceitaDTO data){
        Receita receita = receitaService.inserir(data);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(receita.getId()).toUri();
        return ResponseEntity.created(uri).build();
    }

    @RequestMapping(value="/editar/{id}", method = RequestMethod.PUT)
    public ResponseEntity<Receita> editar(@Valid @RequestBody ReceitaDTO data, @PathVariable Long id) throws ObjectNotFoundException {
        data.setId(id);
        receitaService.editar(data);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value="/excluir/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> excluir(@PathVariable Long id) throws ObjectNotFoundException{
        receitaService.excluir(id);
        return ResponseEntity.noContent().build();
    }

}

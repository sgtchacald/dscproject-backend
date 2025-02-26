package br.com.dscproject.controller;

import br.com.dscproject.domain.Despesa;
import br.com.dscproject.dto.DespesaDTO;
import br.com.dscproject.services.DespesaService;
import br.com.dscproject.services.exceptions.ObjectNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "/despesas")
public class DespesaController {

    @Autowired
    DespesaService despesaService;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<DespesaDTO>> buscarTodos(){
        return ResponseEntity.ok().body(despesaService.buscarTodosPorUsuario());
    }

    @RequestMapping(value="/buscar-por-id/{id}", method=RequestMethod.GET)
    public ResponseEntity<Despesa> buscarPorId(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok().body(despesaService.buscarPorId(id));
    }

    @RequestMapping(value="/inserir", method = RequestMethod.POST)
    public ResponseEntity<Despesa> inserir(@Valid @RequestBody DespesaDTO data){
        Despesa despesa = despesaService.inserir(data);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(despesa.getId()).toUri();
        return ResponseEntity.created(uri).build();
    }

    @RequestMapping(value="/editar/{id}", method = RequestMethod.PUT)
    public ResponseEntity<Despesa> editar(@Valid @RequestBody DespesaDTO data, @PathVariable Long id) throws ObjectNotFoundException {
        data.setId(id);
        despesaService.editar(data);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value="/excluir/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> excluir(@PathVariable Long id) throws ObjectNotFoundException{
        despesaService.excluir(id);
        return ResponseEntity.noContent().build();
    }

}

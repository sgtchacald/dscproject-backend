package br.com.dscproject.controller;

import br.com.dscproject.domain.TransacaoBancaria;
import br.com.dscproject.dto.TransacaoBancariaDTO;
import br.com.dscproject.services.TransacaoBancariaService;
import br.com.dscproject.services.exceptions.ObjectNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "/transacao-bancaria")
public class TransacaoBancariaController {

    @Autowired
    TransacaoBancariaService transacaoBancariaService;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<TransacaoBancariaDTO>> buscarTodos(){
        return ResponseEntity.ok().body(transacaoBancariaService.buscarTodosPorUsuario());
    }

    @RequestMapping(value="/buscar-por-id/{id}", method=RequestMethod.GET)
    public ResponseEntity<TransacaoBancaria> buscarPorId(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok().body(transacaoBancariaService.buscarPorId(id));
    }

    @RequestMapping(value="/inserir", method = RequestMethod.POST)
    public ResponseEntity<TransacaoBancaria> inserir(@Valid @RequestBody TransacaoBancariaDTO data){
        TransacaoBancaria transacaoBancaria = transacaoBancariaService.inserir(data);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(transacaoBancaria.getId()).toUri();
        return ResponseEntity.created(uri).build();
    }

    @RequestMapping(value="/editar/{id}", method = RequestMethod.PUT)
    public ResponseEntity<TransacaoBancaria> editar(@Valid @RequestBody TransacaoBancariaDTO data, @PathVariable Long id) throws ObjectNotFoundException {
        data.setId(id);
        transacaoBancariaService.editar(data);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value="/excluir/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> excluir(@PathVariable Long id) throws ObjectNotFoundException{
        transacaoBancariaService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value="/importar-dados-bancarios", method = RequestMethod.POST)
    public ResponseEntity<String> importarDadosBancariosOfx(@RequestParam("file") MultipartFile file, @RequestParam("bancoCodigo") String bancoCodigo) throws ObjectNotFoundException{
        return ResponseEntity.ok().body(transacaoBancariaService.importarDadosBancariosOfx(file, bancoCodigo));
    }

}

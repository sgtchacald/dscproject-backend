package br.com.dscproject.controller;

import br.com.dscproject.domain.InstituicaoFinanceira;
import br.com.dscproject.dto.InstituicaoFinanceiraDTO;
import br.com.dscproject.services.InstituicaoFinanceiraService;
import br.com.dscproject.services.exceptions.ObjectNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "/instituicoes-financeiras")
public class InstituicaoFinanceiraController {

    @Autowired
    private InstituicaoFinanceiraService instituicaoFinanceiraService;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<InstituicaoFinanceira>> buscarTodos(){
        return ResponseEntity.ok().body(instituicaoFinanceiraService.buscarTodos());
    }

    @RequestMapping(value="/inserir", method = RequestMethod.POST)
    public ResponseEntity<InstituicaoFinanceira> inserir(@Valid @RequestBody InstituicaoFinanceiraDTO data){
        InstituicaoFinanceira instituicaoFinanceira  = new InstituicaoFinanceira();
        BeanUtils.copyProperties(data, instituicaoFinanceira);
        instituicaoFinanceira = instituicaoFinanceiraService.inserir(instituicaoFinanceira);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(instituicaoFinanceira.getId()).toUri();
        return ResponseEntity.created(uri).build();
    }

    @RequestMapping(value="/editar/{id}", method = RequestMethod.PUT)
    public ResponseEntity<InstituicaoFinanceira> editar(@Valid @RequestBody InstituicaoFinanceiraDTO data, @PathVariable Long id) throws ObjectNotFoundException {
        InstituicaoFinanceira instituicaoFinanceira = new InstituicaoFinanceira();
        BeanUtils.copyProperties(data, instituicaoFinanceira);
        instituicaoFinanceira.setId(id);
        instituicaoFinanceira = instituicaoFinanceiraService.editar(instituicaoFinanceira);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value="/excluir/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> excluir(@PathVariable Long id) throws ObjectNotFoundException{
        instituicaoFinanceiraService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}

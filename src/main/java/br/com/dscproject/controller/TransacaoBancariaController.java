package br.com.dscproject.controller;

import br.com.dscproject.domain.TransacaoBancaria;
import br.com.dscproject.dto.DashboardCardSaldoDTO;
import br.com.dscproject.dto.TransacaoBancariaDTO;
import br.com.dscproject.services.TransacaoBancariaService;
import br.com.dscproject.services.exceptions.ObjectNotFoundException;
import com.webcohesion.ofx4j.io.OFXParseException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<Map<String, String>> importarDadosBancariosOfx(
            @RequestParam("file") MultipartFile file,
            @RequestParam("competencia") String competencia,
            @RequestParam("bancoCodigo") String bancoCodigo) throws IOException, OFXParseException {

        String mensagem = transacaoBancariaService.importarDadosBancariosOfx(file, competencia, bancoCodigo);

        Map<String, String> response = new HashMap<>();
        response.put("message", mensagem);
        return ResponseEntity.ok(response);
    }

    @RequestMapping(value="/buscar-saldo-competencia/{competencia}", method=RequestMethod.GET)
    public ResponseEntity<DashboardCardSaldoDTO> buscarTotalPorCompetencia(@PathVariable String competencia) throws Exception {
        return ResponseEntity.ok().body(transacaoBancariaService.buscarSaldoPorCompetencia(competencia));
    }

}

package br.com.dscproject.controller;

import br.com.dscproject.domain.RegistroFinanceiro;
import br.com.dscproject.domain.Usuario;
import br.com.dscproject.dto.RegistroFinanceiroDTO;
import br.com.dscproject.dto.UsuarioDTO;
import br.com.dscproject.services.RegistroFinanceiroService;
import br.com.dscproject.services.UsuarioService;
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

    @RequestMapping(value="/inserir", method = RequestMethod.POST)
    public ResponseEntity<RegistroFinanceiro> inserirUsuarioSite(@Valid @RequestBody RegistroFinanceiroDTO data){
        RegistroFinanceiro registroFinanceiro = registroFinanceiroService.inserir(data);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(registroFinanceiro.getId()).toUri();
        return ResponseEntity.created(uri).build();
    }

}

package br.com.dscproject.controller;

import br.com.dscproject.dto.CredenciaisDTO;
import br.com.dscproject.dto.LoginResponseDTO;
import br.com.dscproject.model.Usuario;
import br.com.dscproject.repository.UsuarioRepository;
import br.com.dscproject.services.TokenService;
import br.com.dscproject.services.exceptions.AuthorizationException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "auth")
public class AuthenticationController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    UsuarioRepository usuarioRepository;
    @Autowired
    TokenService tokenService;
    @PostMapping(value = "/login")
    public ResponseEntity login(@RequestBody @Valid CredenciaisDTO data){
        try {
            var userPassAuthToken = new UsernamePasswordAuthenticationToken(data.getLogin(), data.getSenha());
            var auth = this.authenticationManager.authenticate(userPassAuthToken);
            var token = tokenService.gerarToken((Usuario) auth.getPrincipal());
            return ResponseEntity.ok(new LoginResponseDTO(token));
        }catch (Exception e){
            throw new AuthorizationException("Erro, autenticação inválida. Revise os dados digitados.");
        }
    }

}

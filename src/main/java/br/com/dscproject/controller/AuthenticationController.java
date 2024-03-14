package br.com.dscproject.controller;

import br.com.dscproject.dto.CredenciaisDTO;
import br.com.dscproject.dto.EmailDTO;
import br.com.dscproject.dto.LoginResponseDTO;
import br.com.dscproject.domain.Usuario;
import br.com.dscproject.repository.UsuarioRepository;
import br.com.dscproject.services.AutorizationService;
import br.com.dscproject.services.TokenService;
import br.com.dscproject.services.exceptions.AuthorizationException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "auth")
public class AuthenticationController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    UsuarioRepository usuarioRepository;
    @Autowired
    TokenService tokenService;
    @Autowired
    AutorizationService autorizationService;
    @PostMapping(value = "/login")
    public ResponseEntity efetuarLogin(@RequestBody @Valid CredenciaisDTO data, HttpServletResponse response){
        try {
            var userPassAuthToken = new UsernamePasswordAuthenticationToken(data.getLogin(), data.getSenha());
            var auth = this.authenticationManager.authenticate(userPassAuthToken);
            var token = tokenService.gerarToken((Usuario) auth.getPrincipal());

            response.addHeader("Authorization", "Bearer " + token);
            response.addHeader("access-control-expose-headers", "Authorization");

            return ResponseEntity.ok(new LoginResponseDTO(token));
        }catch (Exception e){
            throw new AuthorizationException("Autenticação inválida. Revise os dados digitados.");
        }
    }

    @PostMapping(value = "/atualizar-token")
    public ResponseEntity<Void> refreshToken(HttpServletResponse response) {
        Usuario usuario = AutorizationService.authenticated();
        String token = tokenService.gerarToken(usuario);
        response.addHeader("Authorization", "Bearer " + token);
        response.addHeader("access-control-expose-headers", "Authorization");
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/recuperar-senha")
    public ResponseEntity<Void> recuperarSenha(@Valid @RequestBody EmailDTO data) {
        autorizationService.recuperarSenha(data.getEmail());
        return ResponseEntity.noContent().build();
    }

}

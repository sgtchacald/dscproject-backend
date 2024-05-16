package br.com.dscproject.services;

import br.com.dscproject.domain.Usuario;
import br.com.dscproject.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.lang.NonNullApi;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class AuditoriaService implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
        Usuario usuario = AutorizationService.authenticated();
        String loginUsuario = usuario != null &&  !usuario.getLogin().isBlank() ? usuario.getLogin() : "INSERIDO_PELO_SITE";

        return Optional.of(loginUsuario);
    }

}

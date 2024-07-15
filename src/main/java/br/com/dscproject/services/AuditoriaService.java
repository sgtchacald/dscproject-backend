package br.com.dscproject.services;

import br.com.dscproject.domain.Usuario;
import br.com.dscproject.filter.SecurityFilter;
import br.com.dscproject.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.lang.NonNullApi;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class AuditoriaService implements AuditorAware<String> {

    @Autowired
    SecurityFilter securityFilter;

    @Override
    public Optional<String> getCurrentAuditor() {

        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        String token = securityFilter.recuperarToken(attr.getRequest());

        Usuario usuario = AutorizationService.authenticated();
        String login = usuario != null &&  !usuario.getLogin().isBlank() ? usuario.getLogin() : "INSERIDO_PELO_SITE";

        return Optional.of(login);
    }

}

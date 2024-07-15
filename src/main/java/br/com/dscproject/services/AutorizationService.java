package br.com.dscproject.services;

import br.com.dscproject.domain.Usuario;
import br.com.dscproject.dto.EmailDTO;
import br.com.dscproject.repository.UsuarioRepository;
import br.com.dscproject.services.exceptions.ObjectNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class AutorizationService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /*@Autowired
    private EmailService emailService;*/

    private Random random = new Random();

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        return usuarioRepository.findByLogin(login);
    }

    public static Usuario authenticated() {
        try {
            return (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        }
        catch (Exception e) {
            return null;
        }
    }

    public void recuperarSenha(String email) {

        Usuario usuario = usuarioRepository.findByEmail(email);
        if(usuario == null)
            throw new ObjectNotFoundException("Email não encontrado");

        String novaSenha = gerarNovaSenha();
        usuario.setSenha(passwordEncoder.encode(novaSenha));

        usuarioRepository.save(usuario);

        log.warn(novaSenha);
        //emailService.sendNewPasswordEmail(usuario, novaSenha);
    }

    private String gerarNovaSenha() {
        char[] vet = new char[10];
        for (int i=0; i<10; i++) {
            vet[i] = randomChar();
        }
        return new String(vet);
    }

    /* Utiliza a tabela Unicode */
    private char randomChar() {
        int opt = random.nextInt(3);
        if (opt == 0) { // gera um digito
            return (char) (random.nextInt(10) + 48);
        }
        else if (opt == 1) { // gera letra maiuscula
            return (char) (random.nextInt(26) + 65);
        }
        else { // gera letra minuscula
            return (char) (random.nextInt(26) + 97);
        }
    }
}

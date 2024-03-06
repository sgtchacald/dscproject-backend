package br.com.dscproject.services;

import br.com.dscproject.domain.Usuario;
import br.com.dscproject.enums.Perfis;
import br.com.dscproject.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.Optional;

@Service
@Slf4j
public class DBService {

   @Autowired
    private UsuarioRepository usuarioRepository;

    public void instanciarBancoDeDados(){

        /*usuarioRepository.save(new Usuario(null, "Diego dos Santos Cordeiro", "M", Date.valueOf("1986-07-12"),"sgt.chacal.d@gmail.com", "chacalsgt", new BCryptPasswordEncoder().encode("chacal@01"), Perfis.ADMIN));
        usuarioRepository.save(new Usuario(null, "Mara Cristiane", "F", Date.valueOf("1975-10-13"),"mcristianesilveira@gmail.com", "mcristianesilveira", new BCryptPasswordEncoder().encode("chacal@02"), Perfis.USER));
        usuarioRepository.save(new Usuario(null, "Tenente Comandante Data", "O", Date.valueOf("2045-10-13"),"cmderdata@gmail.com", "cmderdata", new BCryptPasswordEncoder().encode("chacal@03"), Perfis.USER));
        usuarioRepository.save(new Usuario(null, "Primeiro Oficial Cmt Riker", "M", Date.valueOf("2045-10-13"),"cmderriker@gmail.com", "cmderriker", new BCryptPasswordEncoder().encode("chacal@04"), Perfis.USER));

        log.info("--------------------------------");

        // Buscando todos os Usuarios
        log.info("Usuarios encontrados com findAll():");

        usuarioRepository.findAll().forEach(usuario -> log.info("Inserindo" + usuario.toString()));

        log.info("--------------------------------");
        // Buscando usuario por ID
        Optional<Usuario> usuario = usuarioRepository.findById(1L);
        log.info("Usuario encontrado com findById(1L):");
        log.info(usuario.toString());
        log.info("--------------------------------");*/
    }
}

package br.com.dscproject.services;

import br.com.dscproject.model.Usuario;
import br.com.dscproject.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.Optional;

@Service
public class DBService {

    private UsuarioRepository usuarioRepository;
    private static final Logger log = LoggerFactory.getLogger(DBService.class);

    public void setDadosDev(){
        usuarioRepository.save(new Usuario(null, "Diego dos Santos Cordeiro", "M", Date.valueOf("1986-07-12"),"sgt.chacal.d@gmail.com", "chacalsgt", "chacal@01"));
        usuarioRepository.save(new Usuario(null, "Mara Cristiane", "F", Date.valueOf("1975-10-13"),"mcristianesilveira@gmail.com", "mcristianesilveira", "chacal@02"));
        usuarioRepository.save(new Usuario(null, "Tenente Comandante Data", "O", Date.valueOf("2045-10-13"),"cmderdata@gmail.com", "cmderdata", "chacal@03"));
        usuarioRepository.save(new Usuario(null, "Primeiro Oficial Cmt Riker", "M", Date.valueOf("2045-10-13"),"cmderriker@gmail.com", "cmderriker", "chacal@04"));

        // Buscando todos os Usuarios
        log.info("Usuarios encontrados com findAll():");
        log.info("-------------------------------");
        usuarioRepository.findAll().forEach(usuario -> {
            log.info(usuario.toString());
        });
        log.info("");

        // Buscando usuario por ID
        Optional<Usuario> usuario = usuarioRepository.findById(1L);
        log.info("Usuario encontrado com findById(1L):");
        log.info("--------------------------------");
        log.info(usuario.toString());
        log.info("");
    }
}

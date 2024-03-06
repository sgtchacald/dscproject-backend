package br.com.dscproject.services;

import br.com.dscproject.domain.Usuario;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class TokenService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.whith.is.user}")
    private String whithIsUser;

    public String gerarToken(Usuario usuario) throws RuntimeException {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer(whithIsUser)
                    .withSubject(usuario.getLogin())
                    .withExpiresAt(new Date(System.currentTimeMillis() + expiration))
                    .withClaim("nome", usuario.getNome())
                    .withClaim("email", usuario.getEmail())
                    .withClaim("login", usuario.getLogin())
                    .sign(Algorithm.HMAC256(secret));
        }catch (Exception e){
            throw new RuntimeException("Erro ao gerar o token JWT.", e);
        }
    }

    public String validarToken(String token) throws RuntimeException {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer(whithIsUser)
                    .build()
                    .verify(token)
                    .getSubject();
        }catch (Exception e){
            throw new RuntimeException("Não foi possível validar o token JWT, verifique.", e);
        }
    }



}

package br.com.dscproject.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name="usuarios")
public class Usuario {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "nome", length = 100, nullable = false)
    private String nome;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    @Column(name = "dt_nascimento", nullable = false)
    private Date nascimento;

    @Column(name = "email", length = 512, nullable = false, unique = true)
    private String email;

    @Column(name = "login", length = 40, nullable = false, unique = true)
    private String login;

    @Column(name = "senha", length = 60, nullable = false)
    private String senha;

}

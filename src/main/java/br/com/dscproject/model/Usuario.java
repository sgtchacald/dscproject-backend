package br.com.dscproject.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data @NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name="USUARIOS")
public class Usuario implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "NOME", length = 100, nullable = false)
    private String nome;

    @Column(name = "GENERO", length = 1, nullable = true)
    private String genero;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    @Column(name = "DT_NASCIMENTO", nullable = true)
    private Date nascimento;

    @Column(name = "EMAIL", length = 512, nullable = false, unique = true)
    private String email;

    @Column(name = "LOGIN", length = 40, nullable = false, unique = true)
    private String login;

    @Column(name = "SENHA", length = 25, nullable = false)
    private String senha;

}

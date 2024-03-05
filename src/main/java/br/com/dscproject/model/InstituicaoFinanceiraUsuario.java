package br.com.dscproject.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="INSTITUICOES_FINANCEIRAS_USUARIOS")
public class InstituicaoFinanceiraUsuario implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "INFU_ID", nullable = false)
    private Long id;

    @Column(name = "INFU_AGENCIA", nullable = false)
    private String agencia;

    @Column(name = "INFU_CONTA", nullable = false)
    private String conta;

    @Column(name = "INFU_NOM_GERENTE", nullable = false)
    private String gerente;

    @Column(name = "INFU_TEL_GERENTE", nullable = false)
    private String telefoneGerente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "US_ID", nullable = false, updatable = true)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "INFI_ID", nullable = false, updatable = true)
    private InstituicaoFinanceira instituicaoFinanceira;



}

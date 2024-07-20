package br.com.dscproject.domain;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cascade;
import org.hibernate.envers.Audited;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="INSTITUICOES_FINANCEIRAS_USUARIO")
@Audited
@EntityListeners(AuditingEntityListener.class)
public class InstituicaoFinanceiraUsuario  extends AbstractAuditoria implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "INFU_ID", nullable = false)
    private Long id;

    @Column(name = "INFU_AGENCIA", nullable = false, length=30)
    private String agencia;

    @Column(name = "INFU_CONTA", nullable = false, length=30, unique = true)
    private String conta;

    @Column(name = "INFU_NOM_GERENTE", nullable = false, length=100)
    private String nomeGerente;

    @Column(name = "INFU_TEL_GERENTE", nullable = false, length=20)
    private String telefoneGerente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "INFI_ID")
    private InstituicaoFinanceira instituicaoFinanceira;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "USU_ID")
    private Usuario usuario;

}

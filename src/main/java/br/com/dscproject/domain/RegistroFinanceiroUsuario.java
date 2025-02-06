package br.com.dscproject.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="REGISTRO_FINANCEIRO_USUARIO")
@Audited
@EntityListeners(AuditingEntityListener.class)
public class RegistroFinanceiroUsuario extends AbstractAuditoria implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "REFU_ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "REFI_ID", nullable = false)
    private RegistroFinanceiro registroFinanceiro;

    @ManyToOne
    @JoinColumn(name = "USU_ID", nullable = false)
    private Usuario usuario;

}

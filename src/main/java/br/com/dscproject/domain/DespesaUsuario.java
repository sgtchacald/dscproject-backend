package br.com.dscproject.domain;

import br.com.dscproject.enums.StatusPagamento;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="DESPESAS_USUARIO")
@Audited
@EntityListeners(AuditingEntityListener.class)
public class DespesaUsuario extends AbstractAuditoria implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DEPU_ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "DESP_ID", nullable = false)
    private Despesa despesa;

    @ManyToOne
    @JoinColumn(name = "USU_ID", nullable = false)
    private Usuario usuario;

    @Column(name = "DEPU_VALOR", nullable = true)
    private BigDecimal valor;

    @Column(name = "DEPU_IND_STATUS_PAGAMENTO", nullable = false)
    private boolean statusPagamento = false;

}

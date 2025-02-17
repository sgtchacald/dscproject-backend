package br.com.dscproject.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name="PAGAMENTOS")
@Audited
@EntityListeners(AuditingEntityListener.class)
public class Pagamento extends AbstractAuditoria implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "PA_ID", nullable = false)
    private Long id;

    @Column(name = "PA_VALOR", nullable = false)
    private BigDecimal valor;

    @Column(name = "PA_DT_PAGAMENTO", nullable = false)
    private LocalDate dtPagamento;

    @Column(name = "PA_QTD_PARCELA", nullable = false)
    private int numeroParcela;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "REFI_ID")
    private Despesa registroFinanceiro;

}

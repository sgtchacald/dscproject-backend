package br.com.dscproject.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="PAGAMENTOS")
public class Pagamento implements Serializable {
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

    @Column(name = "PA_QTD_PARCELA", columnDefinition = "1", nullable = false)
    private int numeroParcela;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "US_ID")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "REFI_ID")
    private RegistroFinanceiro registroFinanceiro;

}

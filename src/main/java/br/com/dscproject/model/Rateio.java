package br.com.dscproject.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="RATEIO")
public class Rateio implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "VALOR", nullable = false)
    private BigDecimal valor;

    @Column(name = "VENCIMENTO", nullable = false)
    private LocalDate dtVencimento;

    @Column(name = "QTD_PARCELA", nullable = false)
    private int numeroParcela;

    private boolean contaPaga;

    @ManyToOne
    private Usuario usuario;

    @ManyToOne
    private Transacao transacao;

}

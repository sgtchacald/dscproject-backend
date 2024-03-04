package br.com.dscproject.model;

import br.com.dscproject.enums.TipoEntrada;
import br.com.dscproject.enums.TipoTransacao;
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
@Table(name="TRANSACAO")
public class Transacao implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "DESCRICAO", length = 100, nullable = false)
    private String descricao;

    @Column(name = "VALOR", nullable = false)
    private BigDecimal valor;

    @Column(name = "LANCAMENTO", nullable = false)
    private LocalDate dtLancamento;

    @Column(name = "QTD_PARCELA", nullable = false)
    private int qtdParcela;

    @Enumerated(EnumType.STRING)
    private TipoTransacao tipoTransacao;

    @Enumerated(EnumType.STRING)
    private TipoEntrada tipoEntrada;

    @ManyToOne
    private Usuario usuario;

}

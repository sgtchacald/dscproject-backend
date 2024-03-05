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
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="REGISTROS_FINANCEIROS")
public class RegistroFinanceiro implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "REFI_ID", nullable = false)
    private Long id;

    @Column(name = "REFI_DESCRICAO", length = 100, nullable = false)
    private String descricao;

    @Column(name = "REFI_VALOR", nullable = false)
    private BigDecimal valor;

    @Column(name = "REFI_DT_LANCAMENTO", nullable = false)
    private LocalDate dtLancamento;

    @Column(name = "REFI_DT_VENCIMENTO", nullable = false)
    private LocalDate dtVencimento;

    @Column(name = "REFI_QTD_PARCELA", nullable = false)
    private int qtdParcela;

    @Enumerated(EnumType.STRING)
    @Column(name = "REFI_TIPO_TRANSACAO", length = 1, nullable = false)
    private TipoTransacao tipoTransacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "REFI_TIPO_ENTRADA", length = 1, nullable = false)
    private TipoEntrada tipoEntrada;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "US_ID", nullable = false, updatable = true)
    private List<Usuario> usuarios;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "INFI_ID", nullable = false, updatable = true)
    private InstituicaoFinanceira instituicaoFinanceira;
}
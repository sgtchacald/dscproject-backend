package br.com.dscproject.domain;

import br.com.dscproject.enums.CategoriaRegistroFinanceiro;
import br.com.dscproject.enums.StatusPagamento;
import br.com.dscproject.enums.TipoRegistroFinanceiro;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
@Entity
@NamedEntityGraph(
        name = "br.com.dscproject.domain.Usuario",
        attributeNodes = @NamedAttributeNode("usuariosResponsaveis")
)

@Table(name="DESPESAS")
@Audited
@EntityListeners(AuditingEntityListener.class)
public class Despesa extends AbstractAuditoria implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DESP_ID", nullable = false)
    private Long id;

    @Column(name = "DESP_COMPETENCIA", length = 7, nullable = false)
    private String competencia="0000-00";

    @Column(name = "DESP_NOME", length = 100, nullable = false)
    private String nome;

    @Column(name = "DESP_DESCRICAO", length = 512, nullable = false)
    private String descricao;

    @Column(name = "DESP_DT_LANCAMENTO", nullable = false)
    private LocalDate dtLancamento;

    @Column(name = "DESP_DT_VENCIMENTO", nullable = true)
    private LocalDate dtVencimento;

    @Column(name = "DESP_EXISTE_PARCELA", nullable = false)
    private boolean existeParcela = false;

    @Column(name = "DESP_ID_PARCELA_PAI", nullable = true)
    private Long idParcelaPai;

    @Column(name = "DESP_NRO_PARCELA", nullable = true)
    private int nrParcela=0;

    @Column(name = "DESP_QTD_PARCELA", nullable = true)
    private int qtdParcela;

    @Column(name = "DESP_VALOR_PARCELADO", nullable = true)
    private BigDecimal valorParcelado;

    @Column(name = "DESP_VALOR_TOTAL_A_DIVIDIR", nullable = true)
    private BigDecimal valorTotalADividir;

    @Column(name = "DESP_VALOR", nullable = true)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(name = "DESP_TIPO_TRANSACAO", nullable = false)
    private TipoRegistroFinanceiro tipoRegistroFinanceiro;

    @Enumerated(EnumType.STRING)
    @Column(name = "DESP_TIPO_RECEITA_DESPESA", nullable = true)
    private CategoriaRegistroFinanceiro categoriaRegistroFinanceiro;

    @Enumerated(EnumType.STRING)
    @Column(name = "DESP_IND_STATUS_PAGAMENTO", nullable = true)
    private StatusPagamento statusPagamento;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "INFU_ID")
    private InstituicaoFinanceiraUsuario instituicaoFinanceiraUsuario;

    @JsonIgnore
    @ManyToMany(cascade = { CascadeType.MERGE, CascadeType.PERSIST }, fetch = FetchType.LAZY)
    @JoinTable(name = "DESPESAS_USUARIO", joinColumns = @JoinColumn(name = "DESP_ID"), inverseJoinColumns = @JoinColumn(name = "USU_ID"))
    private List<Usuario> usuariosResponsaveis;

}
package br.com.dscproject.domain;

import br.com.dscproject.enums.CategoriaRegistroFinanceiro;
import br.com.dscproject.enums.StatusPagamento;
import br.com.dscproject.enums.TipoRegistroFinanceiro;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
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
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data @NoArgsConstructor @AllArgsConstructor
@Entity
@NamedEntityGraph(
        name = "br.com.dscproject.domain.Usuario",
        attributeNodes = @NamedAttributeNode("usuariosResponsaveis")
)

@Table(name="REGISTROS_FINANCEIROS")
@Audited
@EntityListeners(AuditingEntityListener.class)
public class RegistroFinanceiro extends AbstractAuditoria implements Serializable {
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
    private Instant dtLancamento;

    @Column(name = "REFI_DT_VENCIMENTO", nullable = true)
    private LocalDate dtVencimento;

    //@Column(name = "REFI_DIA_VENCIMENTO", nullable = true)
    //private int diaVencimento;

    @Column(name = "REFI_QTD_PARCELA", nullable = false)
    private int qtdParcela;

    @Enumerated(EnumType.STRING)
    @Column(name = "REFI_TIPO_TRANSACAO", nullable = false)
    private TipoRegistroFinanceiro tipoRegistroFinanceiro;

    @Enumerated(EnumType.STRING)
    @Column(name = "REFI_TIPO_RECEITA_DESPESA", nullable = true)
    private CategoriaRegistroFinanceiro categoriaRegistroFinanceiro;


    @Enumerated(EnumType.STRING)
    @Column(name = "REFI_IND_STATUS_PAGAMENTO", nullable = true)
    private StatusPagamento statusPagamento;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "INFU_ID")
    private InstituicaoFinanceiraUsuario instituicaoFinanceiraUsuario;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "REGISTRO_FINANCEIRO_USUARIO",
            joinColumns = @JoinColumn(name = "REFI_ID"),
            inverseJoinColumns = @JoinColumn(name = "USU_ID")
    )
    @JsonIgnore
    private Set<Usuario> usuariosResponsaveis;



}
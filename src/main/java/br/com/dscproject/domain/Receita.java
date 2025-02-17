package br.com.dscproject.domain;

import br.com.dscproject.enums.CategoriaRegistroFinanceiro;
import br.com.dscproject.enums.TipoRegistroFinanceiro;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Data @NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name="RECEITAS")
@Audited
@EntityListeners(AuditingEntityListener.class)
public class Receita extends AbstractAuditoria implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RECE_ID", nullable = false)
    private Long id;

    @Column(name = "RECE_NOME", length = 100, nullable = false)
    private String nome;

    @Column(name = "RECE_DESCRICAO", length = 512, nullable = false)
    private String descricao;

    @Column(name = "RECE_VALOR", nullable = false)
    private BigDecimal valor;

    @Column(name = "RECE_DT_LANCAMENTO", nullable = false)
    private Instant dtLancamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "RECE_TIPO_TRANSACAO", nullable = false)
    private TipoRegistroFinanceiro tipoRegistroFinanceiro;

    @Enumerated(EnumType.STRING)
    @Column(name = "RECE_TIPO_RECEITA_DESPESA", nullable = true)
    private CategoriaRegistroFinanceiro categoriaRegistroFinanceiro;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "INFU_ID")
    private InstituicaoFinanceiraUsuario instituicaoFinanceiraUsuario;

}
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
import java.util.Date;

@Data @NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name="TRANSACOES_BANCARIAS")
@Audited
@EntityListeners(AuditingEntityListener.class)
public class TransacaoBancaria extends AbstractAuditoria implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TRBA_ID", nullable = false)
    private Long id;

    @Column(name = "TRBA_DESCRICAO", length = 512, nullable = false)
    private String descricao;

    @Column(name = "TRBA_VALOR", nullable = false)
    private BigDecimal valor;

    @Column(name = "TRBA_DT_LANCAMENTO", nullable = false)
    private Date dtLancamento;

    @Column(name = "TRBA_OFX_TRANSACAO_ID", length = 512, nullable = true, unique = true)
    private String ofxTransacaoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "TRBA_TIPO_TRANSACAO", nullable = false)
    private TipoRegistroFinanceiro tipoRegistroFinanceiro;

    @Enumerated(EnumType.STRING)
    @Column(name = "TRBA_CATEGORIA_REGISTRO_FINANCEIRO", nullable = true)
    private CategoriaRegistroFinanceiro categoriaRegistroFinanceiro;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "INFU_ID")
    private InstituicaoFinanceiraUsuario instituicaoFinanceiraUsuario;

}
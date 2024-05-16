package br.com.dscproject.domain;

import br.com.dscproject.enums.TipoInstituicaoFinanceira;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="INSTITUICOES_FINANCEIRAS")
@Audited
@EntityListeners(AuditingEntityListener.class)
public class InstituicaoFinanceira extends AbstractAuditoria implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "INFI_ID", nullable = false)
    private Long id;

    @Column(name = "INFI_NOME", length = 100, nullable = false, unique = true)
    private String nome;

    @Column(name = "INFI_TIPO_INSTITUICAO", length = 1, nullable = false)
    private String tipoInstituicao;

    public TipoInstituicaoFinanceira getTipoInstituicao() {
        return TipoInstituicaoFinanceira.toEnum(tipoInstituicao);
    }

    public void setTipoInstituicao (TipoInstituicaoFinanceira tipoInstituicaoFinanceira) {
        this.tipoInstituicao = tipoInstituicaoFinanceira.getCodigo();
    }

}

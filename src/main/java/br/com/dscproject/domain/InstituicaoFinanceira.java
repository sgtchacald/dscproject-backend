package br.com.dscproject.domain;

import br.com.dscproject.enums.TipoInstituicaoFinanceira;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="INSTITUICOES_FINANCEIRAS")
public class InstituicaoFinanceira implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "INFI_ID", nullable = false)
    private Long id;

    @Column(name = "INFI_NOME", length = 100, nullable = false)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(name = "INFI_TIPO_INSTITUICAO", nullable = false)
    private TipoInstituicaoFinanceira tipoInstituicao;

}

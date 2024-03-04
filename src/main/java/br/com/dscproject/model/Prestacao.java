package br.com.dscproject.model;

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
@Table(name="PRESTACAO")
public class Prestacao implements Serializable {
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

    @ManyToOne
    private Usuario usuarioQuePagou;

    @ManyToOne
    private Transacao transacao;

    @ManyToMany
    private List<Usuario> listaUsuariosQueCompartilharam;

}

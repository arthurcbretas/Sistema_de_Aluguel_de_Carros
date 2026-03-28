package br.com.pucminas.aluguelcarros.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Fonte de renda do Cliente.
 * Um cliente pode ter até 3 rendimentos (regra validada no service).
 */
@Entity
@Table(name = "rendimento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Rendimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rendimento")
    private Long idRendimento;

    @Column(name = "descricao", nullable = false, length = 200)
    private String descricao;

    @Column(name = "valor_mensal", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorMensal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;
}

package br.com.pucminas.aluguelcarros.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Contrato de crédito concedido por um Banco a um aluguel aprovado.
 * Associado opcionalmente a um Contrato.
 */
@Entity
@Table(name = "contrato_credito")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"contrato", "banco"})
public class ContratoCredito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contrato_credito")
    private Long idContratoCredito;

    @Column(name = "valor_credito", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorCredito;

    @Column(name = "parcelas", nullable = false)
    private Integer parcelas;

    @Column(name = "taxa_juros_mensal", precision = 8, scale = 4)
    private BigDecimal taxaJurosMensal;

    // ── Relacionamentos ───────────────────────────────────────────────────

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_contrato", nullable = false, unique = true)
    private Contrato contrato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_banco")
    private Banco banco;
}

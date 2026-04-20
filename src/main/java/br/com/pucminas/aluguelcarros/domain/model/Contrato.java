package br.com.pucminas.aluguelcarros.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Contrato de aluguel gerado automaticamente pelo sistema
 * após a aprovação do pedido pelo agente.
 */
@Entity
@Table(name = "contrato")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"pedido", "contratoCredito"})
public class Contrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contrato")
    private Long idContrato;

    @Column(name = "data_assinatura")
    private LocalDate dataAssinatura;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim;

    @Column(name = "assinado", nullable = false)
    private Boolean assinado = false;

    @Column(name = "valor_final", precision = 15, scale = 2)
    private BigDecimal valorFinal;

    /** Flag propagada do Pedido: indica se o cliente solicitou financiamento bancário. */
    @Column(name = "necessita_credito", nullable = false)
    private Boolean necessitaCredito = false;

    // ── Relacionamentos ───────────────────────────────────────────────────

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pedido", nullable = false, unique = true)
    private Pedido pedido;

    @OneToOne(mappedBy = "contrato", cascade = CascadeType.ALL, orphanRemoval = true)
    private ContratoCredito contratoCredito;
}

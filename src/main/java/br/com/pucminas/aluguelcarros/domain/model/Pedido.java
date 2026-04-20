package br.com.pucminas.aluguelcarros.domain.model;

import br.com.pucminas.aluguelcarros.domain.enums.StatusPedido;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Pedido de aluguel criado por um Cliente.
 * Ciclo de vida: PENDENTE → APROVADO/REJEITADO → (contrato gerado) | CANCELADO.
 * Somente pedidos com status PENDENTE podem ser modificados ou cancelados pelo cliente.
 */
@Entity
@Table(name = "pedido")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private Long idPedido;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_inicio_aluguel", nullable = false)
    private LocalDate dataInicioAluguel;

    @Column(name = "data_fim_aluguel", nullable = false)
    private LocalDate dataFimAluguel;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusPedido status = StatusPedido.PENDENTE;

    @Column(name = "valor_estimado", precision = 15, scale = 2)
    private BigDecimal valorEstimado;

    /** Flag indicando se o cliente necessita de financiamento bancário. Imutável após criação. */
    @Column(name = "necessita_credito", nullable = false)
    private Boolean necessitaCredito = false;

    // ── Relacionamentos ───────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_automovel", nullable = false)
    private Automovel automovel;

    @OneToOne(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private Contrato contrato;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Avaliacao> avaliacoes = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.dataCriacao = LocalDateTime.now();
    }
}

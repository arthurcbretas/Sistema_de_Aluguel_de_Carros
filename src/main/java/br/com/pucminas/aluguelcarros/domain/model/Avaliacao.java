package br.com.pucminas.aluguelcarros.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Avaliação de um Pedido realizada por um Agente (Empresa ou Banco).
 *
 * <p>Nota de implementação: o campo {@code agente} usa
 * {@code targetEntity = Agente.class} explicitamente para garantir
 * compatibilidade com Micronaut Data em hierarquias JOINED.</p>
 */
@Entity
@Table(name = "avaliacao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_avaliacao")
    private Long idAvaliacao;

    @Column(name = "data_avaliacao", nullable = false)
    private LocalDateTime dataAvaliacao;

    @Column(name = "parecer", nullable = false, length = 20)
    private String parecer;  // "APROVADO" | "REJEITADO"

    @Column(name = "justificativa", length = 500)
    private String justificativa;

    // ── Relacionamentos ───────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY, optional = false, targetEntity = Agente.class)
    @JoinColumn(name = "id_agente", nullable = false)
    private Agente agente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pedido", nullable = false)
    private Pedido pedido;

    @PrePersist
    protected void onCreate() {
        this.dataAvaliacao = LocalDateTime.now();
    }
}

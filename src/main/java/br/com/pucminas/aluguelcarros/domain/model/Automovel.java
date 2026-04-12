package br.com.pucminas.aluguelcarros.domain.model;

import jakarta.persistence.*;
import lombok.*;
import io.micronaut.serde.annotation.Serdeable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

/**
 * Automóvel disponível para aluguel.
 * Pode ser de propriedade de um Cliente, uma Empresa ou um Banco.
 * Regra de negócio: exatamente uma das três FKs deve ser não-nula
 * (validado em AutomovelService antes da persistência).
 */
@Entity
@Table(name = "automovel")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Serdeable
@JsonIgnoreProperties({"clienteProprietario", "empresaProprietaria", "bancoProprietario"})
public class Automovel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_automovel")
    private Long idAutomovel;

    @Column(name = "matricula", nullable = false, unique = true, length = 50)
    private String matricula;

    @Column(name = "placa", nullable = false, unique = true, length = 10)
    private String placa;

    @Column(name = "marca", nullable = false, length = 80)
    private String marca;

    @Column(name = "modelo", nullable = false, length = 100)
    private String modelo;

    @Column(name = "ano", nullable = false)
    private Integer ano;

    @Column(name = "disponivel", nullable = false)
    private Boolean disponivel = true;

    @Column(name = "preco_diaria", precision = 10, scale = 2)
    private BigDecimal precoDiaria;

    // ── Proprietário (três FKs nullable — apenas uma preenchida) ──────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente_proprietario")
    private Cliente clienteProprietario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empresa_proprietaria")
    private Empresa empresaProprietaria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_banco_proprietario")
    private Banco bancoProprietario;
}

package br.com.pucminas.aluguelcarros.domain.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Empregador do Cliente.
 * Um cliente pode ter até 3 empregadores (regra validada no service).
 */
@Entity
@Table(name = "empregador")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Empregador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empregador")
    private Long idEmpregador;

    @Column(name = "nome", nullable = false, length = 200)
    private String nome;

    @Column(name = "cnpj", length = 18)
    private String cnpj;

    @Column(name = "cargo", length = 100)
    private String cargo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;
}

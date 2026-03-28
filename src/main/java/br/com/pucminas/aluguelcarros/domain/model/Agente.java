package br.com.pucminas.aluguelcarros.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidade-raiz da hierarquia de agentes (Empresa / Banco).
 * Estratégia JOINED mantém integridade referencial por subtipo e
 * permite {@code @ManyToOne(targetEntity = Agente.class)} em Avaliacao.
 */
@Entity
@Table(name = "agente")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "tipo_agente", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class Agente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_agente")
    private Long idAgente;

    @Column(name = "cnpj", nullable = false, unique = true, length = 18)
    private String cnpj;

    @Column(name = "nome", nullable = false, length = 200)
    private String nome;

    @Column(name = "login", nullable = false, unique = true, length = 100)
    private String login;

    @Column(name = "senha", nullable = false, length = 255)
    private String senha;

    @OneToMany(mappedBy = "agente", cascade = CascadeType.ALL)
    private List<Avaliacao> avaliacoes = new ArrayList<>();
}

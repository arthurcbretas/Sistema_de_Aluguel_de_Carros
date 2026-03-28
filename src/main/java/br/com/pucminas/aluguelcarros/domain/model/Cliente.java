package br.com.pucminas.aluguelcarros.domain.model;

import br.com.pucminas.aluguelcarros.domain.interfaces.Proprietario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Pessoa física que realiza pedidos de aluguel.
 * Pode ser proprietário de automóveis.
 * Limites de negócio: até 3 rendimentos e 3 empregadores (validados no service).
 */
@Entity
@Table(name = "cliente")
@DiscriminatorValue("CLIENTE")
@PrimaryKeyJoinColumn(name = "id_usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cliente extends Usuario implements Proprietario {

    @Column(name = "cpf", nullable = false, unique = true, length = 14)
    private String cpf;

    @Column(name = "rg", length = 20)
    private String rg;

    @Column(name = "profissao", length = 100)
    private String profissao;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Rendimento> rendimentos = new ArrayList<>();

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Empregador> empregadores = new ArrayList<>();

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.PERSIST)
    private List<Pedido> pedidos = new ArrayList<>();

    // ── Proprietario ─────────────────────────────────────────────────────

    @Override
    public String getTipoProprietario() {
        return "CLIENTE";
    }

    @Override
    public String getDocumento() {
        return cpf;
    }
}

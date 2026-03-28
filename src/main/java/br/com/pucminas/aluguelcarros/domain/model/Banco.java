package br.com.pucminas.aluguelcarros.domain.model;

import br.com.pucminas.aluguelcarros.domain.interfaces.Proprietario;
import jakarta.persistence.*;
import lombok.*;

/**
 * Agente do tipo instituição bancária.
 * Pode associar contratos de crédito a aluguéis aprovados.
 */
@Entity
@Table(name = "banco")
@DiscriminatorValue("BANCO")
@PrimaryKeyJoinColumn(name = "id_agente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Banco extends Agente implements Proprietario {

    @Column(name = "nome_banco", nullable = false, length = 150)
    private String nomeBanco;

    @Column(name = "codigo_bancario", nullable = false, length = 10)
    private String codigoBancario;

    @Override
    public String getTipoProprietario() {
        return "BANCO";
    }

    @Override
    public String getDocumento() {
        return getCnpj();
    }
}

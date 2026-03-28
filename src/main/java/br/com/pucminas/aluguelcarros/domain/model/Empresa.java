package br.com.pucminas.aluguelcarros.domain.model;

import br.com.pucminas.aluguelcarros.domain.interfaces.Proprietario;
import jakarta.persistence.*;
import lombok.*;

/**
 * Agente do tipo empresa locadora de automóveis.
 */
@Entity
@Table(name = "empresa")
@DiscriminatorValue("EMPRESA")
@PrimaryKeyJoinColumn(name = "id_agente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Empresa extends Agente implements Proprietario {

    @Column(name = "razao_social", nullable = false, length = 200)
    private String razaoSocial;

    @Override
    public String getTipoProprietario() {
        return "EMPRESA";
    }

    @Override
    public String getDocumento() {
        return getCnpj();
    }
}

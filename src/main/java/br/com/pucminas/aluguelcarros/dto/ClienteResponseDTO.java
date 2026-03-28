package br.com.pucminas.aluguelcarros.dto;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;

/**
 * DTO de resposta para Cliente.
 * Nunca expõe o campo {@code senha}.
 */
@Serdeable
@Data
public class ClienteResponseDTO {

    private Long idUsuario;
    private String nome;
    private String login;
    private String endereco;
    private String cpf;
    private String rg;
    private String profissao;
}

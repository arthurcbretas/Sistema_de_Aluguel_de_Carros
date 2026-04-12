package br.com.pucminas.aluguelcarros.dto;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Serdeable
@Data
public class ClienteRequestDTO {

    @NotBlank
    @Size(max = 150)
    private String nome;

    @NotBlank
    @Email
    @Size(max = 100)
    private String login;

    @NotBlank
    @Size(min = 6, max = 100)
    private String senha;

    @Size(max = 255)
    private String endereco;

    @NotBlank
    private String cpf; // normalizado pelo frontend (somente dígitos)

    @Size(max = 20)
    private String rg;

    @Size(max = 100)
    private String profissao;
}

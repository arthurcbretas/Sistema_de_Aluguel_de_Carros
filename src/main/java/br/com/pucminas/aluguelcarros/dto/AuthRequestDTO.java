package br.com.pucminas.aluguelcarros.dto;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Serdeable
@Data
public class AuthRequestDTO {

    @NotBlank
    private String login;

    @NotBlank
    private String senha;
}

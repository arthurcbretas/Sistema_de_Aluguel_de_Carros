package br.com.pucminas.aluguelcarros.dto;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Serdeable
@Data
public class AvaliacaoRequestDTO {

    @NotBlank
    @Pattern(regexp = "APROVADO|REJEITADO")
    private String parecer;

    private String justificativa;
}

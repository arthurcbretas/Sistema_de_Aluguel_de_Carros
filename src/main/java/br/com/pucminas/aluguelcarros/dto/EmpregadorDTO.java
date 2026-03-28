package br.com.pucminas.aluguelcarros.dto;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Serdeable
@Data
public class EmpregadorDTO {

    @NotBlank
    private String nome;

    private String cnpj;

    private String cargo;
}

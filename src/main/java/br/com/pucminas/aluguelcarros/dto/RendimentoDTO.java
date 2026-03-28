package br.com.pucminas.aluguelcarros.dto;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Serdeable
@Data
public class RendimentoDTO {

    @NotBlank
    private String descricao;

    @NotNull
    @Positive
    private BigDecimal valorMensal;
}

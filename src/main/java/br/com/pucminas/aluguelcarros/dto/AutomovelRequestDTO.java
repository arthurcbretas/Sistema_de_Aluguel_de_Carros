package br.com.pucminas.aluguelcarros.dto;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Serdeable
@Data
public class AutomovelRequestDTO {
    @NotBlank
    private String matricula;
    @NotBlank
    private String placa;
    @NotBlank
    private String marca;
    @NotBlank
    private String modelo;
    @NotNull
    private Integer ano;
    @NotNull
    private java.math.BigDecimal precoDiaria;

    /** URL da imagem do veículo (opcional) */
    private String imagemUrl;
}

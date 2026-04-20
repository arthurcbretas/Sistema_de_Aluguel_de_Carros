package br.com.pucminas.aluguelcarros.dto;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Serdeable
@Data
public class PedidoRequestDTO {

    @NotNull
    @Future
    private LocalDate dataInicioAluguel;

    @NotNull
    @Future
    private LocalDate dataFimAluguel;

    @NotNull
    private Long idAutomovel;

    private BigDecimal valorEstimado;

    private Boolean necessitaCredito;
}

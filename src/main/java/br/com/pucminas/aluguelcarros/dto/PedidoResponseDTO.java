package br.com.pucminas.aluguelcarros.dto;

import br.com.pucminas.aluguelcarros.domain.enums.StatusPedido;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Serdeable
@Data
public class PedidoResponseDTO {

    private Long idPedido;
    private LocalDateTime dataCriacao;
    private LocalDate dataInicioAluguel;
    private LocalDate dataFimAluguel;
    private StatusPedido status;
    private BigDecimal valorEstimado;
    private Long idCliente;
    private Long idAutomovel;
    private Long idContrato;
}

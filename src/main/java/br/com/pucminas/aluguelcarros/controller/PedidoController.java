package br.com.pucminas.aluguelcarros.controller;

import br.com.pucminas.aluguelcarros.dto.PedidoRequestDTO;
import br.com.pucminas.aluguelcarros.dto.PedidoResponseDTO;
import br.com.pucminas.aluguelcarros.domain.model.Pedido;
import br.com.pucminas.aluguelcarros.service.PedidoService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.validation.Valid;
import br.com.pucminas.aluguelcarros.exception.RegraDeNegocioException;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Controller("/pedidos")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class PedidoController {

    private final PedidoService pedidoService;
    private final br.com.pucminas.aluguelcarros.repository.ClienteRepository clienteRepository;

    public PedidoController(PedidoService pedidoService, br.com.pucminas.aluguelcarros.repository.ClienteRepository clienteRepository) {
        this.pedidoService = pedidoService;
        this.clienteRepository = clienteRepository;
    }

    @Post
    public HttpResponse<PedidoResponseDTO> criar(@Body @Valid PedidoRequestDTO dto,
            Principal principal) {
        Long idCliente = clienteRepository.findByLogin(principal.getName())
            .orElseThrow(() -> new RegraDeNegocioException("Cliente com login " + principal.getName() + " não encontrado"))
            .getIdUsuario();
        return HttpResponse.created(toResponse(pedidoService.criar(idCliente, dto)));
    }

    @Get
    public List<PedidoResponseDTO> listar(Principal principal) {
        Long idCliente = clienteRepository.findByLogin(principal.getName())
            .orElseThrow(() -> new RegraDeNegocioException("Cliente com login " + principal.getName() + " não encontrado"))
            .getIdUsuario();
        return pedidoService.listarPorCliente(idCliente).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Get("/{id}")
    public PedidoResponseDTO buscar(@PathVariable Long id) {
        return toResponse(pedidoService.buscarPorId(id));
    }

    @Put("/{id}")
    public PedidoResponseDTO modificar(@PathVariable Long id,
            @Body @Valid PedidoRequestDTO dto) {
        return toResponse(pedidoService.modificar(id, dto));
    }

    @Delete("/{id}")
    public HttpResponse<Void> cancelar(@PathVariable Long id) {
        pedidoService.cancelar(id);
        return HttpResponse.noContent();
    }

    @Get("/pendentes")
    @Secured("ROLE_AGENTE")
    public List<PedidoResponseDTO> listarPendentes() {
        return pedidoService.listarPendentes().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private PedidoResponseDTO toResponse(Pedido p) {
        PedidoResponseDTO dto = new PedidoResponseDTO();
        dto.setIdPedido(p.getIdPedido());
        dto.setDataCriacao(p.getDataCriacao());
        dto.setDataInicioAluguel(p.getDataInicioAluguel());
        dto.setDataFimAluguel(p.getDataFimAluguel());
        dto.setStatus(p.getStatus());
        dto.setValorEstimado(p.getValorEstimado());
        dto.setIdCliente(p.getCliente().getIdUsuario());
        dto.setIdAutomovel(p.getAutomovel().getIdAutomovel());
        if (p.getContrato() != null)
            dto.setIdContrato(p.getContrato().getIdContrato());
        return dto;
    }
}

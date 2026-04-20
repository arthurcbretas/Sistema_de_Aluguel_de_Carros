package br.com.pucminas.aluguelcarros.service;

import br.com.pucminas.aluguelcarros.domain.enums.StatusPedido;
import br.com.pucminas.aluguelcarros.domain.model.*;
import br.com.pucminas.aluguelcarros.dto.PedidoRequestDTO;
import br.com.pucminas.aluguelcarros.exception.RecursoNaoEncontradoException;
import br.com.pucminas.aluguelcarros.exception.RegraDeNegocioException;
import br.com.pucminas.aluguelcarros.repository.AutomovelRepository;
import br.com.pucminas.aluguelcarros.repository.PedidoRepository;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Singleton
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteService clienteService;
    private final AutomovelRepository automovelRepository;

    public PedidoService(PedidoRepository pedidoRepository,
                         ClienteService clienteService,
                         AutomovelRepository automovelRepository) {
        this.pedidoRepository = pedidoRepository;
        this.clienteService = clienteService;
        this.automovelRepository = automovelRepository;
    }

    @Transactional
    public Pedido criar(Long idCliente, PedidoRequestDTO dto) {
        Cliente cliente = clienteService.buscarPorId(idCliente);
        Automovel automovel = automovelRepository.findById(dto.getIdAutomovel())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Automóvel", dto.getIdAutomovel()));

        if (!automovel.getDisponivel()) {
            throw new RegraDeNegocioException("Automóvel não disponível para aluguel.");
        }
        if (dto.getDataInicioAluguel().isAfter(dto.getDataFimAluguel())) {
            throw new RegraDeNegocioException("Data de início deve ser anterior à data de fim.");
        }

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setAutomovel(automovel);
        pedido.setDataInicioAluguel(dto.getDataInicioAluguel());
        pedido.setDataFimAluguel(dto.getDataFimAluguel());
        pedido.setStatus(StatusPedido.PENDENTE);
        pedido.setNecessitaCredito(
                dto.getNecessitaCredito() != null ? dto.getNecessitaCredito() : false);

        // ── Cálculo automático do valor estimado ──────────────────────────
        long dias = ChronoUnit.DAYS.between(dto.getDataInicioAluguel(), dto.getDataFimAluguel());
        if (dias <= 0) dias = 1;
        BigDecimal precoDiaria = automovel.getPrecoDiaria() != null
                ? automovel.getPrecoDiaria()
                : new BigDecimal("150.00"); // fallback padrão
        pedido.setValorEstimado(precoDiaria.multiply(BigDecimal.valueOf(dias)));

        return pedidoRepository.save(pedido);
    }

    public List<Pedido> listarPorCliente(Long idCliente) {
        return pedidoRepository.findByClienteIdUsuario(idCliente);
    }

    public List<Pedido> listarPendentes() {
        return pedidoRepository.findByStatus(StatusPedido.PENDENTE);
    }

    public Pedido buscarPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido", id));
    }

    @Transactional
    public Pedido modificar(Long id, PedidoRequestDTO dto) {
        Pedido pedido = buscarPorId(id);
        validarPendente(pedido);
        pedido.setDataInicioAluguel(dto.getDataInicioAluguel());
        pedido.setDataFimAluguel(dto.getDataFimAluguel());
        pedido.setValorEstimado(dto.getValorEstimado());
        return pedidoRepository.update(pedido);
    }

    @Transactional
    public void cancelar(Long id) {
        Pedido pedido = buscarPorId(id);
        validarPendente(pedido);
        pedido.setStatus(StatusPedido.CANCELADO);
        pedidoRepository.update(pedido);
    }

    private void validarPendente(Pedido pedido) {
        if (pedido.getStatus() != StatusPedido.PENDENTE) {
            throw new RegraDeNegocioException(
                    "Somente pedidos PENDENTES podem ser modificados ou cancelados.");
        }
    }
}

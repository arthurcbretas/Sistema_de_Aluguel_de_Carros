package br.com.pucminas.aluguelcarros.service;

import br.com.pucminas.aluguelcarros.domain.enums.StatusPedido;
import br.com.pucminas.aluguelcarros.domain.model.*;
import br.com.pucminas.aluguelcarros.dto.AvaliacaoRequestDTO;
import br.com.pucminas.aluguelcarros.exception.RegraDeNegocioException;
import br.com.pucminas.aluguelcarros.repository.AvaliacaoRepository;
import br.com.pucminas.aluguelcarros.repository.PedidoRepository;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

@Singleton
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final PedidoRepository pedidoRepository;
    private final PedidoService pedidoService;
    private final ContratoService contratoService;

    public AvaliacaoService(AvaliacaoRepository avaliacaoRepository,
                            PedidoRepository pedidoRepository,
                            PedidoService pedidoService,
                            ContratoService contratoService) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.pedidoRepository = pedidoRepository;
        this.pedidoService = pedidoService;
        this.contratoService = contratoService;
    }

    @Transactional
    public Avaliacao avaliar(Long idPedido, Agente agente, AvaliacaoRequestDTO dto) {
        Pedido pedido = pedidoService.buscarPorId(idPedido);

        if (pedido.getStatus() != StatusPedido.PENDENTE) {
            throw new RegraDeNegocioException("Somente pedidos PENDENTES podem ser avaliados.");
        }

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setPedido(pedido);
        avaliacao.setAgente(agente);
        avaliacao.setParecer(dto.getParecer());
        avaliacao.setJustificativa(dto.getJustificativa());
        avaliacaoRepository.save(avaliacao);

        // Atualiza status do pedido e gera contrato automaticamente se aprovado
        if ("APROVADO".equals(dto.getParecer())) {
            pedido.setStatus(StatusPedido.APROVADO);
            pedidoRepository.update(pedido);
            contratoService.gerarContrato(pedido);
        } else {
            pedido.setStatus(StatusPedido.REJEITADO);
            pedidoRepository.update(pedido);
        }

        return avaliacao;
    }
}

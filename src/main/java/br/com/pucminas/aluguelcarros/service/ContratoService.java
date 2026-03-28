package br.com.pucminas.aluguelcarros.service;

import br.com.pucminas.aluguelcarros.domain.model.*;
import br.com.pucminas.aluguelcarros.exception.RecursoNaoEncontradoException;
import br.com.pucminas.aluguelcarros.exception.RegraDeNegocioException;
import br.com.pucminas.aluguelcarros.repository.ContratoCreditoRepository;
import br.com.pucminas.aluguelcarros.repository.ContratoRepository;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;

@Singleton
public class ContratoService {

    private final ContratoRepository contratoRepository;
    private final ContratoCreditoRepository contratoCreditoRepository;

    public ContratoService(ContratoRepository contratoRepository,
                           ContratoCreditoRepository contratoCreditoRepository) {
        this.contratoRepository = contratoRepository;
        this.contratoCreditoRepository = contratoCreditoRepository;
    }

    /**
     * Geração automática de contrato após aprovação do pedido.
     * Chamado pelo AvaliacaoService — não exposto diretamente via controller.
     */
    @Transactional
    public Contrato gerarContrato(Pedido pedido) {
        Contrato contrato = new Contrato();
        contrato.setPedido(pedido);
        contrato.setDataInicio(pedido.getDataInicioAluguel());
        contrato.setDataFim(pedido.getDataFimAluguel());
        contrato.setValorFinal(pedido.getValorEstimado());
        contrato.setAssinado(false);
        return contratoRepository.save(contrato);
    }

    public Contrato buscarPorId(Long id) {
        return contratoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Contrato", id));
    }

    /**
     * Associa crédito bancário a um contrato existente (ação do Agente Banco).
     */
    @Transactional
    public ContratoCredito associarCredito(Long idContrato, Banco banco,
                                           BigDecimal valor, int parcelas) {
        Contrato contrato = buscarPorId(idContrato);
        if (contrato.getContratoCredito() != null) {
            throw new RegraDeNegocioException("Este contrato já possui crédito associado.");
        }
        ContratoCredito credito = new ContratoCredito();
        credito.setContrato(contrato);
        credito.setBanco(banco);
        credito.setValorCredito(valor);
        credito.setParcelas(parcelas);
        return contratoCreditoRepository.save(credito);
    }
}

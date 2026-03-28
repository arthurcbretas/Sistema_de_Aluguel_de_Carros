package br.com.pucminas.aluguelcarros.domain.enums;

/**
 * Ciclo de vida de um pedido de aluguel.
 */
public enum StatusPedido {

    /** Aguardando avaliação do agente. */
    PENDENTE,

    /** Aprovado — contrato sendo gerado automaticamente. */
    APROVADO,

    /** Reprovado na análise financeira pelo agente. */
    REJEITADO,

    /** Cancelado pelo próprio cliente enquanto PENDENTE. */
    CANCELADO
}

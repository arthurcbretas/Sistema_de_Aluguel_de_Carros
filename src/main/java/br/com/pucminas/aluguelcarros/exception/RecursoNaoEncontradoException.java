package br.com.pucminas.aluguelcarros.exception;

/**
 * Lançada quando um recurso solicitado não é encontrado no banco de dados.
 * Mapeada para HTTP 404.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }

    public RecursoNaoEncontradoException(String entidade, Long id) {
        super(entidade + " com id " + id + " não encontrado(a).");
    }
}

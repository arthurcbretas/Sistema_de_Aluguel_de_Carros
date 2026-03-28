package br.com.pucminas.aluguelcarros.exception;

/**
 * Lançada quando uma operação viola uma regra de negócio do sistema.
 * Mapeada para HTTP 422 (Unprocessable Entity).
 */
public class RegraDeNegocioException extends RuntimeException {

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}

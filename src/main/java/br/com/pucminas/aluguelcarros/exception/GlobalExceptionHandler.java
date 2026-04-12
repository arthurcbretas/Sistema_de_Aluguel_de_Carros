package br.com.pucminas.aluguelcarros.exception;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;

import java.util.Map;

/**
 * Handler global que converte RegraDeNegocioException em HTTP 409 Conflict
 * com corpo JSON { "message": "..." } em vez de 500 Internal Server Error.
 */
@Produces
@Singleton
public class GlobalExceptionHandler
        implements ExceptionHandler<RegraDeNegocioException, HttpResponse<?>> {

    @Override
    public HttpResponse<?> handle(HttpRequest request, RegraDeNegocioException exception) {
        return HttpResponse.<Map<String, String>>status(
                io.micronaut.http.HttpStatus.CONFLICT
        ).body(Map.of("message", exception.getMessage()));
    }
}

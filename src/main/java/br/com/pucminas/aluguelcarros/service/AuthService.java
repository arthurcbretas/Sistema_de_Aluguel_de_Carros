package br.com.pucminas.aluguelcarros.service;

import br.com.pucminas.aluguelcarros.domain.model.Cliente;
import br.com.pucminas.aluguelcarros.repository.ClienteRepository;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.AuthenticationProvider;
import io.micronaut.security.authentication.AuthenticationRequest;
import io.micronaut.security.authentication.AuthenticationResponse;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * Autenticação via login/senha. Implementa a interface genérica
 * {@code AuthenticationProvider<HttpRequest<?>>} do Micronaut Security 4.x.
 *
 * <p>TODO: substituir comparação de senha plain-text por BCrypt hash.</p>
 */
@Singleton
public class AuthService implements AuthenticationProvider<HttpRequest<?>> {

    private final ClienteRepository clienteRepository;

    public AuthService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public Publisher<AuthenticationResponse> authenticate(
            HttpRequest<?> httpRequest,
            AuthenticationRequest<?, ?> authenticationRequest) {

        String login = authenticationRequest.getIdentity().toString();
        String senha = authenticationRequest.getSecret().toString();

        Optional<Cliente> clienteOpt = clienteRepository.findByLogin(login);

        if (clienteOpt.isPresent() && clienteOpt.get().getSenha().equals(senha)) {
            return Mono.just(AuthenticationResponse.success(login));
        }
        return Mono.just(AuthenticationResponse.failure("Credenciais inválidas."));
    }
}

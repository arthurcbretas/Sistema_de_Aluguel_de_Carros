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
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Autenticação via login/senha. Implementa a interface genérica
 * {@code AuthenticationProvider<HttpRequest<?>>} do Micronaut Security 4.x.
 *
 * <p>Validada usando BCrypt hash.</p>
 */
@Singleton
public class AuthService implements AuthenticationProvider<HttpRequest<?>> {

    private final ClienteRepository clienteRepository;
    private final br.com.pucminas.aluguelcarros.repository.EmpresaRepository empresaRepository;

    public AuthService(ClienteRepository clienteRepository,
                       br.com.pucminas.aluguelcarros.repository.EmpresaRepository empresaRepository) {
        this.clienteRepository = clienteRepository;
        this.empresaRepository = empresaRepository;
    }

    @Override
    public Publisher<AuthenticationResponse> authenticate(
            HttpRequest<?> httpRequest,
            AuthenticationRequest<?, ?> authenticationRequest) {

        String login = authenticationRequest.getIdentity().toString();
        String senha = authenticationRequest.getSecret().toString();

        Optional<Cliente> clienteOpt = clienteRepository.findByLogin(login);
        if (clienteOpt.isPresent() && BCrypt.checkpw(senha, clienteOpt.get().getSenha())) {
            return Mono.just(AuthenticationResponse.success(login, List.of("ROLE_CLIENTE")));
        }

        Optional<br.com.pucminas.aluguelcarros.domain.model.Empresa> empresaOpt = empresaRepository.findByLogin(login);
        // Note: Em ambiente de produção senhas de agente deveriam usar bcrypt também. 
        // Para a PoC e o Dummy Initializer, estamos em texto puro ou checando equals direto para facilitar
        if (empresaOpt.isPresent() && senha.equals(empresaOpt.get().getSenha())) {
            return Mono.just(AuthenticationResponse.success(login, List.of("ROLE_AGENTE")));
        }

        return Mono.just(AuthenticationResponse.failure("Credenciais inválidas."));
    }
}

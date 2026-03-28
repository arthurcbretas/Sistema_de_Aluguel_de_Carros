package br.com.pucminas.aluguelcarros.controller;

import br.com.pucminas.aluguelcarros.dto.ClienteRequestDTO;
import br.com.pucminas.aluguelcarros.dto.ClienteResponseDTO;
import br.com.pucminas.aluguelcarros.domain.model.Cliente;
import br.com.pucminas.aluguelcarros.service.ClienteService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.validation.Valid;

/**
 * Endpoints públicos de autenticação.
 * Rotas liberadas via `micronaut.security.intercept-url-map` no application.yml.
 */
@Controller("/auth")
@Secured(SecurityRule.IS_ANONYMOUS)
public class AuthController {

    private final ClienteService clienteService;

    public AuthController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    /**
     * Cadastro de novo cliente.
     * O login (JWT) é gerenciado automaticamente pelo Micronaut Security
     * via POST /login — este endpoint apenas cria o usuário.
     */
    @Post("/register")
    public HttpResponse<ClienteResponseDTO> register(@Body @Valid ClienteRequestDTO dto) {
        Cliente cliente = clienteService.criar(dto);
        ClienteResponseDTO response = new ClienteResponseDTO();
        response.setIdUsuario(cliente.getIdUsuario());
        response.setNome(cliente.getNome());
        response.setLogin(cliente.getLogin());
        response.setCpf(cliente.getCpf());
        return HttpResponse.created(response);
    }
}

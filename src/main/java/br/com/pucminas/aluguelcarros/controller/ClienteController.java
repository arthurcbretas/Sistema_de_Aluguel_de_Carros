package br.com.pucminas.aluguelcarros.controller;

import br.com.pucminas.aluguelcarros.domain.model.Cliente;
import br.com.pucminas.aluguelcarros.dto.*;
import br.com.pucminas.aluguelcarros.service.ClienteService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.validation.Valid;

import java.util.List;

@Controller("/clientes")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @Get("/{id}")
    public ClienteResponseDTO buscar(@PathVariable Long id) {
        Cliente c = clienteService.buscarPorId(id);
        return toResponse(c);
    }

    @Put("/{id}")
    public ClienteResponseDTO atualizar(@PathVariable Long id,
                                         @Body @Valid ClienteRequestDTO dto) {
        return toResponse(clienteService.atualizar(id, dto));
    }

    @Delete("/{id}")
    public HttpResponse<Void> deletar(@PathVariable Long id) {
        clienteService.deletar(id);
        return HttpResponse.noContent();
    }

    // ── Rendimentos ───────────────────────────────────────────────────────

    @Get("/{id}/rendimentos")
    public List<?> listarRendimentos(@PathVariable Long id) {
        return clienteService.listarRendimentos(id);
    }

    @Post("/{id}/rendimentos")
    public HttpResponse<?> adicionarRendimento(@PathVariable Long id,
                                                @Body @Valid RendimentoDTO dto) {
        return HttpResponse.created(clienteService.adicionarRendimento(id, dto));
    }

    @Delete("/{id}/rendimentos/{rid}")
    public HttpResponse<Void> removerRendimento(@PathVariable Long id,
                                                 @PathVariable Long rid) {
        clienteService.removerRendimento(id, rid);
        return HttpResponse.noContent();
    }

    // ── Empregadores ──────────────────────────────────────────────────────

    @Get("/{id}/empregadores")
    public List<?> listarEmpregadores(@PathVariable Long id) {
        return clienteService.listarEmpregadores(id);
    }

    @Post("/{id}/empregadores")
    public HttpResponse<?> adicionarEmpregador(@PathVariable Long id,
                                                @Body @Valid EmpregadorDTO dto) {
        return HttpResponse.created(clienteService.adicionarEmpregador(id, dto));
    }

    // ── Mapeamento interno ────────────────────────────────────────────────

    private ClienteResponseDTO toResponse(Cliente c) {
        ClienteResponseDTO dto = new ClienteResponseDTO();
        dto.setIdUsuario(c.getIdUsuario());
        dto.setNome(c.getNome());
        dto.setLogin(c.getLogin());
        dto.setEndereco(c.getEndereco());
        dto.setCpf(c.getCpf());
        dto.setRg(c.getRg());
        dto.setProfissao(c.getProfissao());
        return dto;
    }
}

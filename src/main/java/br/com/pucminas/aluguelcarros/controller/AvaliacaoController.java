package br.com.pucminas.aluguelcarros.controller;

import br.com.pucminas.aluguelcarros.domain.model.Agente;
import br.com.pucminas.aluguelcarros.dto.AvaliacaoRequestDTO;
import br.com.pucminas.aluguelcarros.service.AvaliacaoService;
import br.com.pucminas.aluguelcarros.repository.EmpresaRepository;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import jakarta.validation.Valid;

import java.security.Principal;
import java.util.Map;

@Controller("/avaliacoes")
@Secured("ROLE_AGENTE")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;
    private final EmpresaRepository empresaRepository;

    public AvaliacaoController(AvaliacaoService avaliacaoService, EmpresaRepository empresaRepository) {
        this.avaliacaoService = avaliacaoService;
        this.empresaRepository = empresaRepository;
    }

    @Post("/{idPedido}")
    public HttpResponse<?> avaliar(@PathVariable Long idPedido, @Body @Valid AvaliacaoRequestDTO dto, Principal principal) {
        Agente agente = empresaRepository.findByLogin(principal.getName()).orElseThrow();
        avaliacaoService.avaliar(idPedido, agente, dto);
        return HttpResponse.ok(Map.of("message", "Pedido avaliado com sucesso"));
    }
}

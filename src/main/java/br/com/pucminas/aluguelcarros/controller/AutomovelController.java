package br.com.pucminas.aluguelcarros.controller;

import br.com.pucminas.aluguelcarros.domain.model.Automovel;
import br.com.pucminas.aluguelcarros.domain.model.Empresa;
import br.com.pucminas.aluguelcarros.repository.AutomovelRepository;
import br.com.pucminas.aluguelcarros.repository.EmpresaRepository;
import br.com.pucminas.aluguelcarros.exception.RecursoNaoEncontradoException;
import br.com.pucminas.aluguelcarros.dto.AutomovelRequestDTO;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;

@Controller("/automoveis")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class AutomovelController {

    private final AutomovelRepository automovelRepository;
    private final EmpresaRepository empresaRepository;

    public AutomovelController(AutomovelRepository automovelRepository, EmpresaRepository empresaRepository) {
        this.automovelRepository = automovelRepository;
        this.empresaRepository = empresaRepository;
    }

    @Get
    public List<Automovel> listarDisponiveis() {
        return automovelRepository.findByDisponivelTrue();
    }

    @Get("/{id}")
    public Automovel buscar(@PathVariable Long id) {
        return automovelRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Automóvel", id));
    }

    // ── ROTAS EXCLUSIVAS DE GESTÃO DA EMPRESA ─────────────────────────────

    @Get("/meus")
    @Secured("ROLE_AGENTE")
    public List<Automovel> listarMeusAutomoveis(Principal principal) {
        // Para simplificar, estamos pegando a empresa atual
        Empresa empresa = empresaRepository.findByLogin(principal.getName()).orElseThrow();
        // findAll() e filter (melhor seria um método no Repository, mas com collection em memória ou stream resolve para H2 small data)
        return automovelRepository.findAll().stream()
                .filter(a -> a.getEmpresaProprietaria() != null && 
                             a.getEmpresaProprietaria().getIdAgente().equals(empresa.getIdAgente()))
                .toList();
    }

    @Post
    @Secured("ROLE_AGENTE")
    public HttpResponse<Automovel> criarAutomovel(@Body @Valid AutomovelRequestDTO dto, Principal principal) {
        Empresa empresa = empresaRepository.findByLogin(principal.getName()).orElseThrow();
        
        Automovel a = new Automovel();
        a.setMarca(dto.getMarca());
        a.setModelo(dto.getModelo());
        a.setAno(dto.getAno());
        a.setMatricula(dto.getMatricula());
        a.setPlaca(dto.getPlaca());
        a.setDisponivel(true);
        a.setPrecoDiaria(dto.getPrecoDiaria());
        a.setImagemUrl(dto.getImagemUrl());
        a.setEmpresaProprietaria(empresa);

        return HttpResponse.created(automovelRepository.save(a));
    }

    @Put("/{id}/preco")
    @Secured("ROLE_AGENTE")
    public HttpResponse<Automovel> atualizarPreco(@PathVariable Long id, @Body java.util.Map<String, java.math.BigDecimal> body, Principal principal) {
        Empresa empresa = empresaRepository.findByLogin(principal.getName()).orElseThrow();
        Automovel a = buscar(id);
        
        if (a.getEmpresaProprietaria() == null || !a.getEmpresaProprietaria().getIdAgente().equals(empresa.getIdAgente())) {
             return HttpResponse.unauthorized();
        }
        
        if (body == null || !body.containsKey("precoDiaria") || body.get("precoDiaria") == null) {
            return HttpResponse.badRequest();
        }
        
        a.setPrecoDiaria(body.get("precoDiaria"));
        return HttpResponse.ok(automovelRepository.update(a));
    }

    @Put("/{id}/disponibilidade")
    @Secured("ROLE_AGENTE")
    public HttpResponse<Automovel> alternarDisponibilidade(@PathVariable Long id, Principal principal) {
        Empresa empresa = empresaRepository.findByLogin(principal.getName()).orElseThrow();
        Automovel a = buscar(id);
        
        if (a.getEmpresaProprietaria() == null || !a.getEmpresaProprietaria().getIdAgente().equals(empresa.getIdAgente())) {
             return HttpResponse.unauthorized();
        }
        
        a.setDisponivel(!a.getDisponivel());
        return HttpResponse.ok(automovelRepository.update(a));
    }

    @Delete("/{id}")
    @Secured("ROLE_AGENTE")
    public HttpResponse<?> deletarAutomovel(@PathVariable Long id, Principal principal) {
        Empresa empresa = empresaRepository.findByLogin(principal.getName()).orElseThrow();
        Automovel a = buscar(id);
        
        if (a.getEmpresaProprietaria() == null || !a.getEmpresaProprietaria().getIdAgente().equals(empresa.getIdAgente())) {
             return HttpResponse.unauthorized();
        }
        
        try {
            automovelRepository.delete(a);
            return HttpResponse.noContent();
        } catch (Exception e) {
            // Em caso de Foreign Key violation (já atrelado a um Pedido/Contrato)
            return HttpResponse.badRequest("Não é possível excluir um veículo com histórico de pedidos associado. Tente inativá-lo (Indisponível).");
        }
    }
}

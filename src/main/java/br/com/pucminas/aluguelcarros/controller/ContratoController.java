package br.com.pucminas.aluguelcarros.controller;

import br.com.pucminas.aluguelcarros.domain.model.Banco;
import br.com.pucminas.aluguelcarros.domain.model.Contrato;
import br.com.pucminas.aluguelcarros.domain.model.ContratoCredito;
import br.com.pucminas.aluguelcarros.exception.RegraDeNegocioException;
import br.com.pucminas.aluguelcarros.repository.BancoRepository;
import br.com.pucminas.aluguelcarros.service.ContratoService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller("/contratos")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class ContratoController {

    private final ContratoService contratoService;
    private final BancoRepository bancoRepository;

    public ContratoController(ContratoService contratoService, BancoRepository bancoRepository) {
        this.contratoService = contratoService;
        this.bancoRepository = bancoRepository;
    }

    @Get("/{id}")
    public Contrato buscar(@PathVariable Long id) {
        return contratoService.buscarPorId(id);
    }

    /**
     * Lista contratos aprovados que ainda não possuem crédito associado
     * E cujo cliente solicitou financiamento (necessitaCredito = true).
     * Fila de trabalho exclusiva do Banco.
     */
    @Secured("ROLE_BANCO")
    @Get("/pendentes-credito")
    public List<Contrato> listarPendentesCredito() {
        return contratoService.listarSemCredito();
    }

    /**
     * Lista créditos já associados pelo banco logado.
     * Histórico para a aba "Créditos Associados".
     */
    @Secured("ROLE_BANCO")
    @Get("/creditos-banco")
    public List<Map<String, Object>> listarCreditosBanco(Principal principal) {
        String login = principal.getName();
        List<ContratoCredito> creditos = contratoService.listarCreditosPorBanco(login);
        return creditos.stream().map(this::creditoToMap).collect(Collectors.toList());
    }

    /**
     * Associa um Contrato de Crédito — ação exclusiva do Banco.
     * Parâmetros via query string (não body JSON).
     * Retorna Map serializável (evita lazy-load de entidades JPA).
     */
    @Secured("ROLE_BANCO")
    @Post("/{id}/credito")
    public HttpResponse<Map<String, Object>> associarCredito(@PathVariable Long id,
                                            @QueryValue BigDecimal valor,
                                            @QueryValue int parcelas,
                                            @QueryValue(defaultValue = "0") BigDecimal taxaJuros,
                                            Principal principal) {
        Banco banco = bancoRepository.findByLogin(principal.getName())
                .orElseThrow(() -> new RegraDeNegocioException("Banco não encontrado"));
        ContratoCredito credito = contratoService.associarCredito(id, banco, valor, parcelas, taxaJuros);
        return HttpResponse.created(creditoToMap(credito));
    }

    /** Converte ContratoCredito para Map seguro (sem lazy-load). */
    private Map<String, Object> creditoToMap(ContratoCredito c) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("idContratoCredito", c.getIdContratoCredito());
        map.put("idContrato", c.getContrato().getIdContrato());
        map.put("valorCredito", c.getValorCredito());
        map.put("parcelas", c.getParcelas());
        map.put("taxaJurosMensal", c.getTaxaJurosMensal());
        return map;
    }
}

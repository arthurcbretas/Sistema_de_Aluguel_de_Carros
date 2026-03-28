package br.com.pucminas.aluguelcarros.controller;

import br.com.pucminas.aluguelcarros.domain.model.Contrato;
import br.com.pucminas.aluguelcarros.service.ContratoService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;

@Controller("/contratos")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class ContratoController {

    private final ContratoService contratoService;

    public ContratoController(ContratoService contratoService) {
        this.contratoService = contratoService;
    }

    @Get("/{id}")
    public Contrato buscar(@PathVariable Long id) {
        return contratoService.buscarPorId(id);
    }

    /**
     * Associa um Contrato de Crédito — ação exclusiva de agente Banco.
     * TODO: validar que o principal autenticado é um Banco e extrair a entidade.
     */
    @Post("/{id}/credito")
    public HttpResponse<?> associarCredito(@PathVariable Long id,
                                            @QueryValue java.math.BigDecimal valor,
                                            @QueryValue int parcelas) {
        // TODO: resolver Banco a partir do principal autenticado
        return HttpResponse.created(
                contratoService.associarCredito(id, null, valor, parcelas));
    }
}

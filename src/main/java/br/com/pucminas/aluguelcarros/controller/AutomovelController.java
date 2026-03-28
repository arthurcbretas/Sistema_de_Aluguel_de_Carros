package br.com.pucminas.aluguelcarros.controller;

import br.com.pucminas.aluguelcarros.domain.model.Automovel;
import br.com.pucminas.aluguelcarros.repository.AutomovelRepository;
import br.com.pucminas.aluguelcarros.exception.RecursoNaoEncontradoException;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;

import java.util.List;

@Controller("/automoveis")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class AutomovelController {

    private final AutomovelRepository automovelRepository;

    public AutomovelController(AutomovelRepository automovelRepository) {
        this.automovelRepository = automovelRepository;
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
}

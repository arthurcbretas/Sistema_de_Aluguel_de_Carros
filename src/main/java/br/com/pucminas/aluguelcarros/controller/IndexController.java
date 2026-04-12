package br.com.pucminas.aluguelcarros.controller;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;

import java.net.URI;

@Controller("/")
@Secured(SecurityRule.IS_ANONYMOUS)
public class IndexController {

    @Get
    public HttpResponse<?> index() {
        return HttpResponse.redirect(URI.create("/index.html"));
    }
}

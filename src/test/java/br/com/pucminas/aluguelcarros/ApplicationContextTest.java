package br.com.pucminas.aluguelcarros;

import io.micronaut.context.ApplicationContext;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Teste de contexto — verifica que o contexto do Micronaut inicializa
 * corretamente com H2 + Hibernate JOINED + todas as injeções de dependência.
 *
 * Se este teste passar, erros de mapeamento JPA (hierarquia JOINED,
 * relações nullable de Proprietario, etc.) serão detectados antes do runtime.
 */
@MicronautTest(environments = "dev")
class ApplicationContextTest {

    @Inject
    ApplicationContext applicationContext;

    @Inject
    EmbeddedServer embeddedServer;

    @Test
    void contextCarregaComSucesso() {
        assertNotNull(applicationContext);
        assertTrue(applicationContext.isRunning(),
                "O contexto do Micronaut deve estar em execução");
    }

    @Test
    void servidorEstaAtivo() {
        assertNotNull(embeddedServer);
        assertTrue(embeddedServer.isRunning(),
                "O servidor embedded deve estar ativo");
    }
}

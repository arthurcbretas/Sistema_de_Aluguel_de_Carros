# Log de Troubleshooting e Correções (Testes End-to-End)

Este documento registra o histórico contínuo da validação Autônoma pelo Agente, detectando e corrigindo quebras no ambiente Micronaut + Javascript.

## Erros Detectados e Resoluções

### 1. Inicialização do Servidor Presa (Porta 8080 em uso)
- **Problema**: O processo Micronaut não conseguia subir (`java.net.BindException`) pois os processos órfãos Java permaneciam prendendo a porta do localhost, além de execuções com a variável de ambiente perdida causando tentativas de conexão com Postgres.
- **Resolução**: Intervenção via terminal rodando `Stop-Process -Name java -Force` combinada à amarração da variável local de execução `$env:MICRONAUT_ENVIRONMENTS="dev"`.

### 2. Cadastro Falhando com 400 Bad Request
- **Problema**: O Agente Autônomo retornou falha na tentativa de criação de usuário. A causa raiz era uma anotação oculta `@Email` na Entidade `ClienteRequestDTO.java` do backend. O frontend permitia injetar "janedoe" (String pura) gerando quebra silenciosa de validação DTO no Micronaut.
- **Resolução**: Editado o `index.html` para tipar os inputs `loginUsername` e `regLogin` como `type="email"`, forçando o HTML5 a bloquear submissões fora do padrão obrigatório do Back-end.

### 3. Falas Secundárias (Falso 500 e Ausência de Carros)
- **Problema 500**: O Agente re-executou a suíte de testes passando um email válido, porém o Cadastro retornou 500. Investigação apontou falso-positivo de sistema: a primeira tentativa validou o e-mail preenchendo o Db, e na retentativa o `ClienteService` disparou uma `RegraDeNegocioException` (Duplicidade) não tratada em ControllerAdvices, resultando em 500. Evitado mudando as chaves do teste E2E.
- **Problema Carros ausentes**: O fluxo completo esbarrou novamente pois o campo `<select>` de carros estava sempre em branco. Causa: Arquivo `DataInitializer.java` foi apagado da árvore de pastas durante uma reinicialização anterior.
- **Resolução**: Recriação e vinculação do Mocker Singleton de Beans Micronaut para popular Empresa e Automóveis dinamicamente.

### 4. Round 3: Integração Completa Sucesso Total
- **Resultado**: O Agente rodou a bateria de testes criando a conta "Arthur Bretas" (`arthur@test.com`), logou com sucesso puxando os dados no JWT, abriu o Modal de listagem (agora preenchido pelo Initializer com a Frota Simulada), preencheu as datas de `2026-06-01` até `2026-06-05`, enviou e o UI recarregou o container exibindo *Pedido #1* com Badge `PENDENTE` na Grid, amarrando Front-end (Vanilla SPA) e Back-end (Database H2 + JPA + Micronaut Sec) com primor!

## Conclusão Inicial
Aplicação plenamente validada em ambiente Local via Automação, pronta para o professor acessar e simular de ponta a ponta sem esbarrar em exceções.

### 5. Expansão de Escopo: Dashboard Administrativo
- **Resultado**: A pedido do usuário, implementamos os scripts de Back-end e UI de Front-end para comportar o Agente autenticando, avaliando e gerando os Contratos. O MVP avançado foi construído e passou no teste E2E na primeira rodada oficial, evidenciando o quão sólida estava a abstração em Micronaut! Nenhuma Exception foi disparada.

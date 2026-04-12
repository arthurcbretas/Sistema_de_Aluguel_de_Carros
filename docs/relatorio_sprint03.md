# Relatório da Sprint 3 (Lab02S03) — Sistema de Aluguel de Carros

## 1. Visão Geral da Sprint

A Sprint 3 consolidou o sistema como um **protótipo funcional completo**, cobrindo todos os requisitos funcionais. Além da interface do cliente implementada em sessões anteriores, a sprint adicionou: diferenciação de perfis com JWT, painel administrativo completo para avaliação de pedidos, CRUD de frota por parte da Empresa e cálculo automático de valor estimado baseado em dias x preço diário.

---

## 2. Entregas Técnicas

### 2.1 Interface de Usuário (SPA Multi-Role)

Foram mantidos e expandidos os arquivos estáticos em `src/main/resources/public/`:

- **`style.css`**: Design system Premium (Glassmorphism, gradientes, animações).
- **`index.html`**: Login e Cadastro de Conta com validação de e-mail.
- **`dashboard.html`**: Painel do **Cliente** — histórico de pedidos, criação de novo pedido com dropdown de veículos disponíveis, detalhes e cancelamento.
- **`admin-dashboard.html`**: Painel do **Agente/Empresa** com duas abas:
  - **Avaliações Pendentes**: Listagem de pedidos aguardando aprovação com modal de avaliação (Aprovar / Rejeitar + Justificativa).
  - **Minha Frota Automotiva**: CRUD completo de veículos — listagem, criação com formulário (incluindo preço por diária), suspensão/ativação e exclusão.
- **`app.js`**: Roteamento dinâmico baseado na Role do JWT. Ao realizar login, o token é decodificado para extrair `ROLE_CLIENTE` ou `ROLE_AGENTE` e o usuário é redirecionado à tela correta automaticamente.

### 2.2 Autenticação Multi-Role (JWT)

- **`AuthService`** refatorado para consultar **duas** bases: `ClienteRepository` (emite `ROLE_CLIENTE` com BCrypt) e `EmpresaRepository` (emite `ROLE_AGENTE`).
- Endpoints administrativos protegidos com `@Secured("ROLE_AGENTE")`.
- Endpoints de cliente protegidos com `@Secured(SecurityRule.IS_AUTHENTICATED)`.

### 2.3 CRUD de Automóveis (Agente)

Novos endpoints em `AutomovelController`, exclusivos para `ROLE_AGENTE`:

| Método | Endpoint | Ação |
|---|---|---|
| `GET` | `/automoveis/meus` | Lista veículos da empresa autenticada |
| `POST` | `/automoveis` | Cadastra veículo (vinculado à empresa via JWT) |
| `PUT` | `/automoveis/{id}/disponibilidade` | Toggle disponível/indisponível |
| `DELETE` | `/automoveis/{id}` | Remove veículo (sem histórico de pedidos) |

### 2.4 Cálculo Automático de Valor Estimado

- Campo `precoDiaria` adicionado à entidade `Automovel` (BigDecimal).
- `PedidoService.criar()` calcula automaticamente `valorEstimado = dias × precoDiaria` ao criar um pedido, sem entrada manual do usuário.
- Fallback de R$ 150,00/dia para veículos sem preço configurado.

### 2.5 Avaliação de Pedidos (`AvaliacaoController`)

- Endpoint `POST /avaliacoes/{idPedido}` protegido por `ROLE_AGENTE`.
- Ao aprovar, o `AvaliacaoService` gera automaticamente um `Contrato` via `ContratoService`.

### 2.6 DataInitializer

Mock de inicialização do banco H2 atualizado: cria a Empresa DriveLux com login `drivelux@admin.com` e popula 3 automóveis com preços diários reais (Mercedes R$350, BMW R$420, Audi R$390).

---

## 3. Diagrama de Implantação

O `diagrama-implantacao.puml` representa a arquitetura de 3 nós: **Navegador** (SPA HTML/JS/CSS) → **Servidor Micronaut** (JVM, porta 8080, REST JSON + JWT) → **Banco de Dados** (H2 em dev, PostgreSQL em produção).

---

## 4. Testes

Suíte unitária com **JUnit 5 + Mockito** cobrindo:
- `ClienteServiceTest`: validação de duplicidade, criação com BCrypt.
- `PedidoServiceTest`: validação de datas, disponibilidade, cancelamento.
- `AvaliacaoServiceTest`: restrição de avaliação apenas a pedidos PENDENTE.

---

## 5. Resultado Final

O sistema foi validado com testes End-to-End automatizados (browser headless) cobrindo o fluxo completo: registro de cliente → criação de pedido → cálculo de valor → login do agente → aprovação → atualização de status → gestão de frota (adicionar, suspender, excluir veículo). Zero erros 4xx/5xx durante toda a bateria de testes.

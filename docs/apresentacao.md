---
marp: true
theme: default
paginate: true
backgroundColor: #1a1a2e
color: #eaeaea
style: |
  section {
    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  }
  h1, h2, h3 {
    color: #00d2ff;
  }
  strong {
    color: #00d2ff;
  }
  code {
    background: #16213e;
    color: #e94560;
  }
  table {
    font-size: 0.8em;
  }
  th {
    background: #16213e;
    color: #00d2ff;
  }
  td {
    background: #0f3460;
  }
  blockquote {
    border-left: 4px solid #e94560;
    background: #16213e;
    padding: 0.5em 1em;
  }
  a {
    color: #00d2ff;
  }
  .emoji { font-size: 1.3em; }
---

<!-- _class: lead -->
<!-- _backgroundColor: #0f3460 -->

# 🚗 Sistema de Aluguel de Carros

### **DriveLux** — Gestão de Aluguéis de Automóveis

**LAB02 — Laboratório de Desenvolvimento de Software**
PUC Minas · Engenharia de Software
Prof. João Paulo Carneiro Aramuni

---

# 📋 Agenda

1. **Visão Geral do Projeto**
2. **Stack Tecnológica**
3. **Arquitetura — Camadas MVC**
4. **Modelo de Domínio**
5. **Segurança — JWT Multi-Role**
6. **Camada de Dados — JPA + Dual Database**
7. **Frontend — SPA sem Framework**
8. **Facilidades do Micronaut**
9. **Desafios e Dificuldades**
10. **Lições Aprendidas**

---

# 🎯 Visão Geral do Projeto

Sistema web para apoio à **gestão de aluguéis de automóveis**, permitindo efetuar, cancelar e modificar pedidos através da Internet.

### Atores do Sistema

| Ator | Papel |
|------|-------|
| **Cliente** | Cadastro, login, criar/consultar/cancelar pedidos, solicitar financiamento |
| **Agente (Empresa)** | Avaliar pedidos, gerenciar frota (CRUD com fotos) |
| **Agente (Banco)** | Visualizar contratos com crédito solicitado, associar termos |
| **Sistema** | Cálculo automático de valores, geração de contratos |

---

# 🎯 Fluxo de Negócio

```
 Cliente             Empresa (Admin)          Banco
   │                      │                     │
   ├─ Cria Pedido ───────►│                     │
   │  [✓ Crédito?]        │                     │
   │                      ├─ Avalia ──────────►  │
   │                      │  (APROVADO)          │
   │                      │  → Gera Contrato     │
   │                      │                      │
   │                      │    Se crédito:       │
   │                      │    ──────────────►   ├─ Associa Crédito
   │                      │                      │  (valor, parcelas,
   │◄─── Contrato Final ──┤◄─────────────────── │   taxa de juros)
```

> `valorEstimado = dias × precoDiaria` — calculado automaticamente pelo servidor

---

# 🛠️ Stack Tecnológica

| Camada | Tecnologia | Versão |
|--------|-----------|--------|
| **Linguagem** | Java (OpenJDK) | 17 |
| **Framework** | Micronaut | 4.x |
| **Build** | Gradle + Shadow JAR | 8.x |
| **ORM** | Micronaut Data + Hibernate | 6 |
| **Segurança** | Micronaut Security JWT + BCrypt | — |
| **DB Produção** | PostgreSQL | 15 |
| **DB Dev/Testes** | H2 In-Memory | 2.2.224 |
| **Servidor HTTP** | Netty (embedded) | — |
| **Reatividade** | Project Reactor | — |
| **Testes** | JUnit 5 + Mockito | — |
| **Frontend** | Vanilla HTML/JS/CSS | — |

---

# 🏗️ Arquitetura — Camadas MVC

```
┌──────────────────────────────────────────────────────┐
│                    FRONTEND (SPA)                     │
│        dashboard.html · admin · bank-dashboard        │
│              Vanilla JS + Fetch API + JWT             │
└──────────────────────┬───────────────────────────────┘
                       │ HTTP/JSON (Bearer Token)
┌──────────────────────▼───────────────────────────────┐
│              CONTROLLER LAYER                         │
│  AuthController · PedidoController · ContratoController│
│  ClienteController · AutomovelController · ...         │
│              @Secured + JWT Guards                     │
├───────────────────────────────────────────────────────┤
│              SERVICE LAYER                             │
│  AuthService · PedidoService · ContratoService         │
│  ClienteService · AvaliacaoService                     │
│              Regras de Negócio                         │
├───────────────────────────────────────────────────────┤
│              REPOSITORY LAYER                          │
│  Micronaut Data JPA — queries em compile-time          │
│  ClienteRepo · PedidoRepo · ContratoRepo · ...         │
├───────────────────────────────────────────────────────┤
│              DOMAIN MODEL (JPA Entities)                │
│  Usuario · Cliente · Empresa · Banco · Pedido · ...    │
└──────────────────────┬───────────────────────────────┘
                       │ JDBC / Hibernate
            ┌──────────▼──────────┐
            │  PostgreSQL (prod)  │
            │  H2 Memory (dev)    │
            └─────────────────────┘
```

> **Dependências unidirecionais:** Controller → Service → Repository → Domain

---

# 📦 Modelo de Domínio — Hierarquia de Herança

```
                    ┌──────────┐
                    │ Usuario  │ (abstract)
                    │──────────│
                    │ login    │
                    │ senha    │
                    └────┬─────┘
                         │
              ┌──────────┴──────────┐
              │                     │
        ┌─────▼─────┐        ┌─────▼─────┐
        │  Cliente   │        │  Agente   │ (abstract)
        │───────────│        │───────────│
        │ cpf       │        └────┬──────┘
        │ profissao │             │
        └───────────┘    ┌────────┴────────┐
                         │                 │
                   ┌─────▼─────┐    ┌──────▼─────┐
                   │  Empresa  │    │   Banco    │
                   │───────────│    │────────────│
                   │ cnpj      │    │ cnpj       │
                   │ qtdRecursos│    │ qtdRecursos│
                   └───────────┘    └────────────┘
```

- **Estratégia JPA:** `@Inheritance(strategy = JOINED)` — cada subclasse tem sua tabela
- **Interface** `Proprietario` — `Automovel` pode pertencer a Cliente, Empresa ou Banco

---

# 📦 Entidades do Domínio

```
 Pedido ──────────► Contrato ──────────► ContratoCredito
   │                  │                       │
   │ idAutomovel      │ valorFinal            │ valorCredito
   │ dataInicio       │ necessitaCredito      │ parcelas
   │ dataFim          │                       │ taxaJurosMensal
   │ valorEstimado    │                       │
   │ necessitaCredito │                       │
   │ status           │                       │
   │                  │                       │
   ▼                  ▼                       ▼
 Automovel          Pedido (ref)            Banco (ref)
   │
   │ marca, modelo, ano
   │ placa, cor, precoDiaria
   │ imagemUrl, disponivel
```

| Entidade | Relacionamento |
|----------|---------------|
| `Cliente` → `Pedido` | 1:N |
| `Pedido` → `Contrato` | 1:1 |
| `Contrato` → `ContratoCredito` | 1:1 |
| `Banco` → `ContratoCredito` | 1:N |
| `Cliente` → `Rendimento` | 1:N (máx 3) |
| `Cliente` → `Empregador` | 1:N (máx 3) |

---

# 🔐 Segurança — JWT Multi-Role

### Arquitetura de Autenticação

```
  POST /auth/login { login, senha }
            │
            ▼
    ┌───────────────────┐
    │   AuthService      │  implements AuthenticationProvider<HttpRequest<?>>
    │                    │
    │  1. ClienteRepo    │──► BCrypt.checkpw() ──► ROLE_CLIENTE
    │  2. EmpresaRepo    │──► plaintext match  ──► ROLE_AGENTE
    │  3. BancoRepo      │──► plaintext match  ──► ROLE_BANCO
    │                    │
    │  Nenhum match:     │──► "Credenciais inválidas"
    └────────┬───────────┘
             │
             ▼
    JWT Token { sub: login, roles: ["ROLE_XXX"] }
```

### Controle de Acesso nos Endpoints

```java
@Secured("ROLE_CLIENTE")    // Apenas clientes
@Secured("ROLE_AGENTE")     // Apenas empresa (admin)
@Secured("ROLE_BANCO")      // Apenas banco
@Secured(SecurityRule.IS_AUTHENTICATED)  // Qualquer logado
```

---

# 🔐 Segurança — Intercept URL Map

Configuração declarativa no `application.yml`:

```yaml
micronaut:
  security:
    authentication: bearer
    intercept-url-map:
      - pattern: /*.html          # Páginas estáticas
        access: [isAnonymous()]
      - pattern: /css/**          # CSS
        access: [isAnonymous()]
      - pattern: /js/**           # JavaScript
        access: [isAnonymous()]
      - pattern: /auth/**         # Login e registro
        access: [isAnonymous()]
      - pattern: /**              # Todo o resto
        access: [isAuthenticated()]
```

> **Facilidade Micronaut:** Intercept URL Map configurado via YAML — sem necessidade de classes `SecurityFilterChain` ou `WebSecurityConfigurerAdapter` como no Spring.

---

# 💾 Camada de Dados — Dual Database

### Perfil de Produção (`application.yml`)

```yaml
datasources:
  default:
    url: "jdbc:postgresql://localhost:5432/aluguelcarros"
    driver-class-name: org.postgresql.Driver
jpa:
  default:
    properties:
      hibernate:
        hbm2ddl.auto: validate    # Nunca auto-migra em produção
```

### Perfil de Desenvolvimento (`application-dev.yml`)

```yaml
datasources:
  default:
    url: "jdbc:h2:mem:devDb;DB_CLOSE_DELAY=-1"
    driver-class-name: org.h2.Driver
jpa:
  default:
    properties:
      hibernate:
        hbm2ddl.auto: create-drop  # Recria schema a cada restart
        show_sql: true
```

> Ativação: `./gradlew run -Dmicronaut.environments=dev`

---

# 💾 Micronaut Data JPA — Repositories

```java
@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByClienteLogin(String login);

    List<Pedido> findByStatus(StatusPedido status);
}
```

```java
@Repository
public interface ContratoRepository extends JpaRepository<Contrato, Long> {

    @Query("SELECT c FROM Contrato c " +
           "WHERE c.contratoCredito IS NULL " +
           "AND c.necessitaCredito = true")
    List<Contrato> findSemCredito();
}
```

> **Vantagem Micronaut Data:** Queries derivadas do nome do método são **resolvidas em tempo de compilação** — erros detectados antes do runtime, sem reflection.

---

# 🌐 Frontend — SPA sem Framework

### Por que Vanilla JS?

- **Zero dependências** externas (sem npm, webpack, node_modules)
- **Fetch API** nativa para chamadas REST com JWT
- **Simplicidade:** 3 HTMLs + 1 CSS + 1 JS

### Roteamento por Role (no `app.js`)

```javascript
function checkAuthAndRoute() {
    const token = localStorage.getItem('token');
    const role = parseJwt(token)?.roles?.[0];

    if (!token) return;  // Sem token na index → não redireciona

    if (role === 'ROLE_BANCO')   → bank-dashboard.html
    if (role === 'ROLE_AGENTE')  → admin-dashboard.html
    if (role === 'ROLE_CLIENTE') → dashboard.html
}
```

| Página | Ator | Funcionalidades |
|--------|------|----------------|
| `dashboard.html` | Cliente | Catálogo, pedidos, perfil |
| `admin-dashboard.html` | Empresa | Avaliar pedidos, gerenciar frota |
| `bank-dashboard.html` | Banco | Contratos pendentes, associar crédito |

---

# ✅ Facilidades do Micronaut (1/3)

## 🚀 Startup Ultrarrápido

- **AOT (Ahead-of-Time) Compilation** — anotações processadas em **compile-time**
- Sem scanning de classpath em runtime como o Spring
- Startup em ~2 segundos (vs ~5-8s do Spring Boot equivalente)

```groovy
micronaut {
    aot {
        precomputeOperations.set(true)
        cacheEnvironment.set(true)
        optimizeClassLoading.set(true)
        optimizeNetty.set(true)
    }
}
```

## 📦 Injeção de Dependência sem Reflection

```java
@Singleton
public class AuthService implements AuthenticationProvider<HttpRequest<?>> {
    // Construtor injetado em compile-time — sem proxies, sem CGLIB
    public AuthService(ClienteRepository repo, ...) { ... }
}
```

---

# ✅ Facilidades do Micronaut (2/3)

## 🔑 Security JWT — Configuração Mínima

Autenticação completa com **apenas 1 dependência + YAML**:

```groovy
implementation("io.micronaut.security:micronaut-security-jwt")
```

```yaml
micronaut:
  security:
    authentication: bearer
    token:
      jwt:
        signatures:
          secret:
            generator:
              secret: "minha-chave-secreta"
```

> Sem `SecurityFilterChain`, sem `@EnableWebSecurity`, sem `WebSecurityConfigurerAdapter`. Basta implementar `AuthenticationProvider` e o framework cuida do resto.

## 📝 Intercept URL Map via YAML

Controle de acesso declarativo — padrões glob aplicados **sem código Java**.

---

# ✅ Facilidades do Micronaut (3/3)

## 🗄️ Micronaut Data — Queries em Compile-Time

```java
// Query gerada automaticamente — erro de compilação se campo não existir
List<Pedido> findByClienteLogin(String login);
```

- Erros de query detectados em **tempo de compilação**
- Sem reflection para gerar SQL
- Suporte completo a `@Query` JPQL customizado

## 🌐 Servidor Netty Embutido

- Non-blocking por padrão
- Sem necessidade de Tomcat/Jetty externo
- Configuração: `micronaut { runtime("netty") }`

## 📂 Recursos Estáticos — 3 Linhas de YAML

```yaml
micronaut:
  router:
    static-resources:
      default:
        paths: classpath:public
        mapping: "/**"
```

---

# ⚠️ Desafios e Dificuldades (1/3)

## 🔌 Conflito de Porta e Processos Órfãos

**Problema:** Processos Java órfãos mantinham a porta 8080 ocupada. Além disso, a variável de ambiente `MICRONAUT_ENVIRONMENTS` se perdia entre sessões do terminal — o servidor tentava conectar no PostgreSQL (produção) em vez do H2 (dev).

**Solução:**
```powershell
Stop-Process -Name java -Force
$env:MICRONAUT_ENVIRONMENTS = "dev"
```

**Lição:** Micronaut diferencia ambientes por **propriedade de sistema** (`-Dmicronaut.environments=dev`), não por perfil Maven/Spring. Necessário atenção ao gerenciamento de variáveis de ambiente.

---

# ⚠️ Desafios e Dificuldades (2/3)

## 📄 Validação Silenciosa no DTO (HTTP 400 oculto)

**Problema:** `@Email` no `ClienteRequestDTO` rejeitava usernames que não eram e-mails. O erro 400 retornava sem mensagem clara, dificultando o debug.

**Solução:** Alinhar validação backend com HTML `type="email"`.

## 🔥 LazyInitializationException na Serialização (HTTP 500)

**Problema:** Entidades JPA com relacionamentos `@ManyToOne` LAZY eram serializadas por Jackson **fora da transação Hibernate** — causando `LazyInitializationException` silenciosa.

**Solução:**
```java
@JsonIgnoreProperties({"pedido", "contratoCredito"})
public class Contrato { ... }

@JsonIgnoreProperties({"contrato", "banco"})
public class ContratoCredito { ... }
```

> Em vez de retornar entidades diretamente, controllers sensíveis retornam `Map<String, Object>` montado manualmente.

---

# ⚠️ Desafios e Dificuldades (3/3)

## 📝 Encoding UTF-8 no Windows

**Problema:** Caracteres acentuados compilavam como `LocaÃ§Ãµes` — o Gradle usava `Cp1252` (Windows default) em vez de `UTF-8`.

**Solução:**
```groovy
tasks.withType(JavaCompile).configureEach {
    options.encoding = 'UTF-8'
}
```

## 🔄 Redirect Loop no Frontend (SPA)

**Problema:** Sem token, o `parseJwt()` retornava role default `ROLE_CLIENTE`, causando redirecionamento infinito entre `index.html` ↔ `dashboard.html`.

**Solução:**
```javascript
if (isIndex && !token) return;   // Sem token na index → não redireciona
if (token) { /* guards de role */ }
```

## 📚 Documentação Micronaut Menor que Spring

Comunidade menor → menos exemplos no StackOverflow. Necessário recorrer mais à **documentação oficial** e ao código-fonte do framework.

---

# 🔬 Micronaut vs Spring Boot — Comparativo

| Aspecto | Micronaut | Spring Boot |
|---------|-----------|-------------|
| **DI** | Compile-time (sem reflection) | Runtime (reflection + proxies) |
| **Startup** | ~2s | ~5-8s |
| **Memória** | ~70-100 MB | ~150-300 MB |
| **Queries DB** | Compile-time validation | Runtime validation |
| **Security Config** | YAML declarativo | Classes Java (`@Configuration`) |
| **Curva de Aprendizado** | Moderada (docs menores) | Baixa (comunidade vasta) |
| **Comunidade** | Crescente | Madura e extensa |
| **GraalVM Native** | Suporte nativo (1ª classe) | Suporte via Spring Native |
| **Anotações** | Similares ao Spring (facilita migração) | Padrão de mercado |

> Micronaut usa anotações **similares** ao Spring (`@Singleton` ≈ `@Component`, `@Controller`, `@Inject`) — reduz a curva para quem já conhece Spring.

---

# 🏗️ Diagrama de Componentes

```
┌─────────────────────────────────────────────────────────────────┐
│                  Dispositivo do Cliente (Browser)                │
│  ┌──────────────┐  ┌──────────────────┐  ┌────────────────┐    │
│  │ SPA Cliente  │  │  SPA Admin       │  │  SPA Banco     │    │
│  │ dashboard    │  │  admin-dashboard │  │  bank-dashboard│    │
│  └──────┬───────┘  └────────┬─────────┘  └───────┬────────┘    │
└─────────┼──────────────────┼─────────────────────┼──────────────┘
          │    HTTP/JSON     │    (Bearer JWT)     │
┌─────────▼──────────────────▼─────────────────────▼──────────────┐
│              Micronaut Web Server (Netty :8080)                   │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  Security: JWT Token Validation + @Secured Guards       │    │
│  ├─────────────────────────────────────────────────────────┤    │
│  │  Controllers: Auth · Cliente · Pedido · Contrato · Auto │    │
│  ├─────────────────────────────────────────────────────────┤    │
│  │  Services: Auth · Cliente · Pedido · Avaliação · Contrato│   │
│  ├─────────────────────────────────────────────────────────┤    │
│  │  Repositories: Micronaut Data JPA (compile-time queries)│    │
│  ├─────────────────────────────────────────────────────────┤    │
│  │  Domain: JPA Entities (JOINED Inheritance)              │    │
│  └──────────────────────────┬──────────────────────────────┘    │
└─────────────────────────────┼───────────────────────────────────┘
                              │ JDBC / Hibernate
                  ┌───────────▼───────────┐
                  │  PostgreSQL (prod)     │
                  │  H2 In-Memory (dev)    │
                  └───────────────────────┘
```

---

# 📊 Números do Projeto

| Métrica | Valor |
|---------|-------|
| **Entidades JPA** | 11 (Usuario, Cliente, Agente, Empresa, Banco, Pedido, Contrato, ContratoCredito, Automovel, Rendimento, Empregador) |
| **Repositories** | 10 |
| **Services** | 5 |
| **Controllers** | 7 |
| **DTOs** | 10 |
| **Endpoints REST** | 20+ |
| **Roles de acesso** | 3 (CLIENTE, AGENTE, BANCO) |
| **Beans gerenciados** | 46 |
| **Páginas HTML** | 4 (index, dashboard, admin, bank) |
| **Sprints executadas** | 5 + hotfix 05.1 |
| **Diagramas UML** | 6 (Casos de Uso, Classes, Componentes, Pacotes, Implantação, Histórias) |

---

# 📊 Cobertura de Testes

### Testes Implementados

| Tipo | Ferramenta | Cobertura |
|------|-----------|-----------|
| **Unitários** | JUnit 5 + Mockito | `ClienteService`, `PedidoService`, `AvaliacaoService` |
| **Contexto** | Micronaut Test | `ApplicationContextTest` — valida que todos os 46 beans carregam |
| **E2E Manual** | Browser + DevTools | Fluxo completo: registro → login → pedido → aprovação → contrato → crédito |

### Resultado

```
BUILD SUCCESSFUL — 46 bean classes loaded
0 erros de compilação
Testes E2E: zero 4xx/5xx em fluxo completo
```

---

# 🗓️ Evolução por Sprints

| Sprint | Entregas Principais |
|--------|-------------------|
| **01** | Diagramas UML (Casos de Uso, Classes, Pacotes) |
| **02** | Arquitetura MVC, CRUD Cliente, JWT Auth, BCrypt, Testes unitários |
| **03** | Frontend SPA, Multi-role auth, CRUD Automóveis, Avaliação de pedidos, Contratos automáticos |
| **04** | Feedback do professor, refinamentos |
| **05** | Infraestrutura ROLE_BANCO completa (repository, auth, seed, dashboard, guards) |
| **05.1** | Fluxo de crédito iniciado pelo cliente (`necessitaCredito`), correção de serialização, aba de histórico |

---

# 🎯 Lições Aprendidas

### O que o Micronaut facilitou

- ⚡ **Startup rápido** — ideal para ciclos curtos de dev/test
- 🔧 **Configuração declarativa** — segurança, DB, static resources via YAML
- 🛡️ **Compile-time safety** — erros de query e DI detectados antes do runtime
- 📦 **Anotações familiares** — quem conhece Spring se adapta rapidamente
- 🌐 **Netty embutido** — zero configuração de servidor

### O que exigiu atenção extra

- 📚 **Documentação menor** — menos exemplos que Spring Boot
- 🔄 **Lazy-loading + Jackson** — exige `@JsonIgnoreProperties` ou DTOs explícitos
- 🖥️ **Encoding Windows** — Gradle precisa de `options.encoding = 'UTF-8'` explícito
- 🧩 **Ambientes** — `MICRONAUT_ENVIRONMENTS` em vez de `spring.profiles.active`
- 🔐 **AuthenticationProvider** — API reativa (Reactor) exige familiaridade com `Publisher`

---

<!-- _class: lead -->
<!-- _backgroundColor: #0f3460 -->

# 🙏 Obrigado!

### Sistema de Aluguel de Carros — DriveLux

**Stack:** Java 17 · Micronaut 4.x · Micronaut Data JPA · JWT · H2/PostgreSQL · Vanilla JS

**Repositório:** Sistema_de_Aluguel_de_Carros

---

<!-- _class: lead -->
<!-- _backgroundColor: #16213e -->

# ❓ Perguntas?


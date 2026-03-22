# Sistema de Aluguel de Carros

> **LAB02 — Laboratório de Desenvolvimento de Software**  
> PUC Minas · Engenharia de Software · Prof. João Paulo Carneiro Aramuni

[![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Micronaut](https://img.shields.io/badge/Micronaut-4.x-1F8ACB?style=for-the-badge&logo=micronaut&logoColor=white)](https://micronaut.io/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-336791?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Gradle](https://img.shields.io/badge/Gradle-8.x-02303A?style=for-the-badge&logo=gradle&logoColor=white)](https://gradle.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](LICENSE)

---

## Sumário

- [Sobre o Projeto](#-sobre-o-projeto)
- [Arquitetura](#-arquitetura)
- [Modelo de Domínio](#-modelo-de-domínio)
- [Estrutura do Projeto](#-estrutura-do-projeto)
- [Pré-requisitos](#-pré-requisitos)
- [Instalação e Execução](#-instalação-e-execução)
- [Endpoints da API](#-endpoints-da-api)
- [Sprints e Entregas](#-sprints-e-entregas)
- [Modelos UML](#-modelos-uml)
- [Tecnologias](#-tecnologias)
- [Equipe](#-equipe)

---

## Sobre o Projeto

Sistema web para apoio à **gestão de aluguéis de automóveis**, permitindo efetuar, cancelar e modificar pedidos através da Internet.

### Funcionalidades Principais

| Ator | Funcionalidades |
|------|----------------|
| **Cliente** | Cadastro, login, criar/consultar/modificar/cancelar pedidos, gerenciar empregadores e rendimentos |
| **Agente (Empresa)** | Login, avaliar pedidos (aprovar/rejeitar), modificar pedidos |
| **Agente (Banco)** | Login, avaliar pedidos, associar contratos de crédito |
| **Sistema** | Geração automática de contratos, registro de propriedade de automóveis |

### Regras de Negócio

- O sistema só pode ser utilizado após cadastro prévio
- Clientes podem ter até **3 fontes de rendimento** e **3 empregadores** cadastrados
- Pedidos seguem o ciclo: `PENDENTE → APROVADO/REJEITADO → (contrato gerado)`
- Automóveis podem ser propriedade de **Clientes, Empresas ou Bancos**
- Um aluguel pode estar associado a um **contrato de crédito** concedido por banco agente

---

## Arquitetura

O sistema segue a arquitetura **MVC (Model-View-Controller)** com o framework **Micronaut**, organizado em camadas com dependências unidirecionais:

```
┌─────────────────────────────────────────────────────────┐
│                    CLIENT (Browser / REST)                │
└──────────────────────────┬──────────────────────────────┘
                           │ HTTP
┌──────────────────────────▼──────────────────────────────┐
│                    CONTROLLER LAYER                       │
│         @Controller  (Micronaut HTTP Server)              │
│   ClienteController │ PedidoController │ ContratoCtrl    │
└──────────────────────────┬──────────────────────────────┘
                           │ invoca
┌──────────────────────────▼──────────────────────────────┐
│                     SERVICE LAYER                         │
│              @Singleton (Injeção de Dependência)          │
│    ClienteService │ PedidoService │ AvaliacaoService      │
└──────────────────────────┬──────────────────────────────┘
                           │ persiste via
┌──────────────────────────▼──────────────────────────────┐
│                   REPOSITORY LAYER                        │
│           Micronaut Data (JPA / Hibernate)                │
│  ClienteRepository │ PedidoRepository │ ContratoRepo      │
└──────────────────────────┬──────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────┐
│                     DOMAIN MODEL                          │
│   Usuario │ Cliente │ Agente │ Pedido │ Contrato │ ...   │
└──────────────────────────┬──────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────┐
│                      DATABASE                             │
│              PostgreSQL (prod) │ H2 (dev/test)           │
└─────────────────────────────────────────────────────────┘
```

---

## Modelo de Domínio

### Hierarquia de Classes

```
Usuario (abstract)
└── Cliente  implements Proprietario

Agente (abstract)
├── Empresa  implements Proprietario
└── Banco    implements Proprietario

<<interface>> Proprietario
  + getTipoProprietario(): String
  + getDocumento(): String
```

### Entidades Principais

```
Pedido                Contrato              ContratoCredito
├── idPedido          ├── idContrato        ├── idContratoCredito
├── dataCriacao       ├── dataAssinatura    ├── valorCredito
├── dataInicio        ├── dataInicio        └── parcelas
├── dataFim           ├── dataFim
├── status            ├── assinado
└── valorEstimado     └── valorFinal

Automovel             Avaliacao
├── idAutomovel       ├── idAvaliacao
├── matricula         ├── dataAvaliacao
├── ano               ├── parecer
├── marca             └── justificativa
├── modelo
└── placa
```

### Enum `StatusPedido`

```java
public enum StatusPedido {
    PENDENTE,    // Aguardando avaliação do agente
    APROVADO,    // Aprovado — contrato sendo gerado
    REJEITADO,   // Reprovado na análise financeira
    CANCELADO    // Cancelado pelo cliente
}
```

---

## Estrutura do Projeto

```
aluguel-carros/
├── src/
│   ├── main/
│   │   ├── java/br/com/pucminas/aluguelcarros/
│   │   │   ├── domain/
│   │   │   │   ├── model/           # Entidades JPA
│   │   │   │   │   ├── Usuario.java
│   │   │   │   │   ├── Cliente.java
│   │   │   │   │   ├── Agente.java
│   │   │   │   │   ├── Empresa.java
│   │   │   │   │   ├── Banco.java
│   │   │   │   │   ├── Pedido.java
│   │   │   │   │   ├── Contrato.java
│   │   │   │   │   ├── ContratoCredito.java
│   │   │   │   │   ├── Automovel.java
│   │   │   │   │   ├── Rendimento.java
│   │   │   │   │   ├── Empregador.java
│   │   │   │   │   └── Avaliacao.java
│   │   │   │   ├── enums/
│   │   │   │   │   └── StatusPedido.java
│   │   │   │   └── interfaces/
│   │   │   │       └── Proprietario.java
│   │   │   ├── repository/          # Micronaut Data repositories
│   │   │   ├── service/             # Regras de negócio (@Singleton)
│   │   │   ├── controller/          # Endpoints REST (@Controller)
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   ├── config/              # Configurações Micronaut
│   │   │   └── exception/           # Exceções customizadas
│   │   └── resources/
│   │       └── application.yml      # Configuração da aplicação
│   └── test/
│       └── java/br/com/pucminas/aluguelcarros/
│           ├── service/             # Testes unitários
│           └── controller/          # Testes de integração
├── docs/
│   └── uml/                         # Diagramas UML (Sprint 01, 02, 03)
├── build.gradle
└── README.md
```

---

## Pré-requisitos

Certifique-se de ter instalado:

- **Java 17+** — [Download JDK](https://adoptium.net/)
- **Gradle 8+** — incluído via Gradle Wrapper (`./gradlew`)
- **PostgreSQL 15+** — para ambiente de produção
- **Docker** (opcional) — para subir o banco via container

Verifique as versões:

```bash
java -version    # deve ser 17+
./gradlew --version
```

---

## Instalação e Execução

### 1. Clone o repositório

```bash
git clone https://github.com/seu-usuario/aluguel-carros.git
cd aluguel-carros
```

### 2. Configure o banco de dados

**Opção A — Docker (recomendado para dev):**

```bash
docker run --name aluguel-db \
  -e POSTGRES_DB=aluguelcarros \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgres:15
```

**Opção B — PostgreSQL local:**

```sql
CREATE DATABASE aluguelcarros;
CREATE USER postgres WITH PASSWORD 'postgres';
GRANT ALL PRIVILEGES ON DATABASE aluguelcarros TO postgres;
```

### 3. Configure o `application.yml`

```yaml
# src/main/resources/application.yml
datasources:
  default:
    url: jdbc:postgresql://localhost:5432/aluguelcarros
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver

jpa:
  default:
    properties:
      hibernate:
        hbm2ddl:
          auto: update
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

> Para ambiente de **desenvolvimento**, use H2 em memória:
> ```yaml
> datasources:
>   default:
>     url: jdbc:h2:mem:devDb
>     driver-class-name: org.h2.Driver
> ```

### 4. Execute a aplicação

```bash
# Build e execução
./gradlew run

# Ou com hot reload
./gradlew run --continuous
```

A API estará disponível em: **`http://localhost:8080`**

### 5. Execute os testes

```bash
./gradlew test

# Com relatório de cobertura
./gradlew test jacocoTestReport
```

---

## Endpoints da API

### Autenticação

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/auth/register` | Cadastro de novo usuário |
| `POST` | `/auth/login` | Login e obtenção de token JWT |

### Clientes

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/clientes/{id}` | Buscar cliente por ID |
| `PUT` | `/clientes/{id}` | Atualizar dados do cliente |
| `GET` | `/clientes/{id}/rendimentos` | Listar rendimentos |
| `POST` | `/clientes/{id}/rendimentos` | Adicionar rendimento (máx. 3) |
| `DELETE` | `/clientes/{id}/rendimentos/{rid}` | Remover rendimento |
| `GET` | `/clientes/{id}/empregadores` | Listar empregadores |
| `POST` | `/clientes/{id}/empregadores` | Adicionar empregador (máx. 3) |

### Pedidos

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/pedidos` | Criar novo pedido de aluguel |
| `GET` | `/pedidos` | Listar pedidos do cliente autenticado |
| `GET` | `/pedidos/{id}` | Detalhar pedido |
| `PUT` | `/pedidos/{id}` | Modificar pedido (somente PENDENTE) |
| `DELETE` | `/pedidos/{id}` | Cancelar pedido (somente PENDENTE) |

### Avaliações (Agentes)

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/pedidos/pendentes` | Listar pedidos pendentes (agentes) |
| `POST` | `/pedidos/{id}/avaliar` | Aprovar ou rejeitar pedido |

### Contratos

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/contratos/{id}` | Detalhar contrato |
| `POST` | `/contratos/{id}/credito` | Associar contrato de crédito (Banco) |

### Automóveis

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/automoveis` | Listar automóveis disponíveis |
| `GET` | `/automoveis/{id}` | Detalhar automóvel |

---

## Sprints e Entregas

### Sprint 01 — Modelagem ✅

> **Entregues:**

- [x] Diagrama de Casos de Uso (12 UCs identificados)
- [x] Histórias do Usuário (9 histórias em 4 épicos com critérios de aceitação)
- [x] Diagrama de Classes (com melhorias: `Avaliacao`, atributos temporais, métodos em `Proprietario`)
- [x] Diagrama de Pacotes — Visão Lógica (9 pacotes, arquitetura MVC Micronaut)

Documentação: [`/docs/uml/Sprint01_Modelagem.docx`](docs/uml/)

---

### Sprint 02 — Componentes + CRUD Cliente 🔄

> **A entregar:**

- [ ] Revisão dos diagramas (feedback Sprint 01)
- [ ] Diagrama de Componentes do Sistema
- [ ] Implementação do CRUD completo de Cliente (Micronaut + JPA)
- [ ] Autenticação JWT com Micronaut Security

---

### Sprint 03 — Protótipo Final

> **A entregar:**

- [ ] Revisão dos diagramas (feedback Sprint 02)
- [ ] Diagrama de Implantação
- [ ] Protótipo funcional: criação de usuários e visualização de status de pedidos
- [ ] Apresentação final comparando modelos e implementação

---

## Modelos UML

Os diagramas estão disponíveis na pasta [`/docs/uml/`](docs/uml/) nos formatos `.docx` (documento completo) e código PlantUML (`.puml`) para re-geração.

### Diagrama de Casos de Uso — PlantUML

```plantuml
@startuml
left to right direction
skinparam actorStyle awesome

actor Cliente
actor Agente
actor Empresa extends Agente
actor Banco extends Agente
actor Sistema

rectangle "Sistema de Aluguel de Carros" {
  (UC01) as "Realizar Cadastro"
  (UC02) as "Realizar Login"
  (UC03) as "Criar Pedido"
  (UC04) as "Consultar Pedido"
  (UC05) as "Modificar Pedido"
  (UC06) as "Cancelar Pedido"
  (UC07) as "Avaliar Pedido"
  (UC08) as "Gerar Contrato"
  (UC09) as "Associar Crédito"
}

Cliente --> (UC01)
Cliente --> (UC02)
Cliente --> (UC03)
Cliente --> (UC04)
Agente  --> (UC07)
Banco   --> (UC09)
Sistema --> (UC08)

(UC03) ..> (UC02) : <<include>>
(UC08) ..> (UC07) : <<include>>
@enduml
```

### Diagrama de Classes — PlantUML

```plantuml
@startuml
skinparam classAttributeIconSize 0

enum StatusPedido { PENDENTE; APROVADO; REJEITADO; CANCELADO }

interface Proprietario {
  +getTipoProprietario(): String
  +getDocumento(): String
}

abstract class Usuario {
  -idUsuario: Long
  -nome: String
  -login: String
  -senha: String
  -endereco: String
}

class Cliente {
  -cpf: String
  -rg: String
  -profissao: String
}
Cliente --|> Usuario
Cliente ..|> Proprietario

abstract class Agente { -cnpj: String }
class Empresa { -razaoSocial: String }
class Banco { -nomeBanco: String; -codigoBancario: String }
Empresa --|> Agente
Banco   --|> Agente
Empresa ..|> Proprietario
Banco   ..|> Proprietario

class Pedido {
  -idPedido: Long
  -dataCriacao: Date
  -dataInicioAluguel: Date
  -dataFimAluguel: Date
  -status: StatusPedido
  -valorEstimado: Double
}

class Avaliacao {
  -dataAvaliacao: Date
  -parecer: String
  -justificativa: String
}

Cliente "1" --> "0..*" Pedido
Pedido  "1" --> "0..1" Contrato
Agente  "1" --> "*"    Avaliacao
Pedido  "1" --> "*"    Avaliacao
@enduml
```

---

## Tecnologias

| Tecnologia | Versão | Uso |
|-----------|--------|-----|
| **Java** | 17+ | Linguagem principal |
| **Micronaut** | 4.x | Framework web e IoC |
| **Micronaut Data** | 4.x | ORM / repositórios JPA |
| **Hibernate** | 6.x | Provider JPA |
| **PostgreSQL** | 15 | Banco de dados produção |
| **H2** | 2.x | Banco de dados desenvolvimento/testes |
| **Micronaut Security** | 4.x | Autenticação JWT |
| **Gradle** | 8.x | Build tool |
| **JUnit 5** | 5.x | Testes unitários |
| **Mockito** | 5.x | Mocks para testes |

---

## Equipe

| Nome | Função | GitHub |
|------|--------|--------|
| [Arthur Capanema Bretas] | Desenvolvedor | [arthurcbretas@gmail.com) |

---

## Licença

Este projeto está licenciado sob a [MIT License](LICENSE).

---

<div align="center">

**PUC Minas · Engenharia de Software · 2026**  
Laboratório de Desenvolvimento de Software — LAB02

</div>

# Relatório de Implementação e Entregas - Sprint 2
**Disciplina**: Laboratório de Desenvolvimento de Software  
**Projeto**: Sistema de Aluguel de Carros  

Este documento detalha tecnicamente, passo a passo, todas as implementações e adequações realizadas no código e na documentação estrutural para o escopo e exigências de avaliação da **Sprint 2 (Lab02S02)**.

---

## Passo 1: Adequação e Revisão dos Modelos UML
De acordo com o feedback recebido anteriormente e as exigências da nova sprint, o repositório foi atualizado na subpasta de documentação (`docs/uml/`).

* **Modelos Mantidos e Revisados (Sprint 1)**: O *Diagrama de Casos de Uso*, *Histórias de Usuários*, *Diagrama de Classes* e a visão lógica via *Diagrama de Pacotes* mantêm-se como referência central do projeto e condizem com o fluxo que está sendo codificado.
* **Diagrama de Componentes (Adicionado)**: A modelagem de componentes físicos e camadas lógicas foi construída com rigor (`docs/uml/diagrama-componentes.puml`). A arquitetura documenta com clareza o uso pleno do padrão MVC estipulado, desenhando desde os requests do Cliente até os verbos acionados interagindo com as engrenagens de Controller, Service e Repository.

## Passo 2: Construção da Arquitetura MVC em Java
A fim de atender plenamente à exigência de criar "um sistema Web em Java seguindo a arquitetura MVC", estruturou-se a base corporativa sobre o framework Micronaut. Toda a lógica transacional e mapeamento está em `src/main/java/br/com/pucminas/aluguelcarros`.

A organização reflete perfeitamente as diretrizes da matéria:
* **Entidades (`domain/model`)**: Classes responsáveis por manter o domínio real do projeto: `Cliente`, `Rendimento` e `Empregador`.
* **Repositórios (`repository`)**: Abstrações de Data JPA (`ClienteRepository`) persistindo de forma coerente no PostgreSQL / H2 em memória.
* **Controladores (`controller`)**: Rotas Web REST (`/clientes`) implementando todos os verbos e requisições focadas em DTOs (Data Transfer Objects), isolando a lógica web da do sistema.

## Passo 3: Implementação Completa do CRUD de Clientes
O foco principal da segunda etapa de código era propiciar a completa gerência dos Usuários (Clientes). Para isso, implementaram-se métodos com forte controle nas regras de negócios levantadas na especificação inicial, todas centralizadas no `ClienteService.java`:

- **Create, Read, Update, Delete**: Todo o ciclo de vida da entidade Principal (Cliente) agora reage tanto a requisições de buscar, deletar, persistir ou atualizar seu cadastro de CPF, RG e Informações vitais.
- **Limitações Contratuais Obrigatórias (Rendimentos & Empregadores)**: A validação em código (Service layer) reflete fielmente o limite exigido de **no máximo 3 rendimentos associados**. Tentar exceder a listagem submete ao backend o bloqueio por conta de uma `RegraDeNegocioException()`.

## Passo 4: Agregação de Qualidade e Segurança (Tech Extras)
Para além do CRUD elementar, o projeto buscou uma entrega robusta e coesa para um sistema WEB do escopo atual.

* **Armazenamento de Senha Criptografado (BCrypt)**: A entrada e persistência dos dados garantem sigilo na autenticação, não salvando senhas de texto puro diretamente no banco relacional. Utilizou-se o método de criptografia padronizada em mercado, visando melhores práticas.
* **Autenticação JWT com Micronaut Security**: O protocolo de login (`AuthService`) foi integrado aos módulos de segurança do Micronaut para emissão e validação de tokens JWT (`AuthenticationProvider`). Isso consolida os requisitos em torno da segurança das controllers.
* **Garantia de Fluxos via Testes (QA)**: Implementou-se uma suíte unitária focada no `ClienteService` utilizando bibliotecas consolidadas de mercado (*JUnit e Mockito*), atestando a persistência do repositório, deleções eficientes e as amarrações do Hash.

---
**Conclusão**  
Neste ponto, o sistema detém total capacidade arquitetural, validada com integridade técnica, para ingressar na gestão de Pedidos de Aluguel estipulada pela Sprint 3.

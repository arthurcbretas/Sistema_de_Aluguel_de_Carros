# Histórias de Usuário — Sistema de Aluguel de Carros

> Sprint 01 · LAB02 — PUC Minas · Engenharia de Software

---

## Épico 1: Acesso ao Sistema

### US01 — Realizar Cadastro
**Como** pessoa interessada em alugar um carro,  
**Quero** me cadastrar no sistema informando meus dados pessoais,  
**Para** poder acessar os serviços de aluguel.

**Critérios de Aceitação:**
- [ ] O sistema valida CPF único e login único antes de criar a conta
- [ ] Campos obrigatórios: nome, CPF, login (e-mail), senha, endereço
- [ ] A senha deve ter no mínimo 6 caracteres
- [ ] O sistema retorna HTTP 201 com os dados do cliente criado (sem senha)
- [ ] O sistema retorna HTTP 422 caso CPF ou login já estejam cadastrados

---

### US02 — Realizar Login
**Como** usuário cadastrado,  
**Quero** me autenticar com login e senha,  
**Para** obter um token JWT e acessar as funcionalidades protegidas.

**Critérios de Aceitação:**
- [ ] O sistema autentica via `POST /login` e retorna token JWT
- [ ] Credenciais inválidas retornam HTTP 401
- [ ] O token tem validade configurável e deve ser enviado em todas as requisições protegidas

---

## Épico 2: Gestão de Perfil do Cliente

### US03 — Gerenciar Rendimentos
**Como** cliente autenticado,  
**Quero** cadastrar, consultar e remover minhas fontes de rendimento,  
**Para** que o sistema possa avaliar minha capacidade financeira.

**Critérios de Aceitação:**
- [ ] O cliente pode ter no máximo **3 rendimentos** cadastrados
- [ ] Cada rendimento tem: descrição e valor mensal (> 0)
- [ ] Ao atingir o limite, o sistema retorna HTTP 422 com mensagem explicativa
- [ ] O cliente pode remover qualquer um dos seus rendimentos

---

### US04 — Gerenciar Empregadores
**Como** cliente autenticado,  
**Quero** cadastrar, consultar e remover meus empregadores,  
**Para** complementar meu perfil financeiro no sistema.

**Critérios de Aceitação:**
- [ ] O cliente pode ter no máximo **3 empregadores** cadastrados
- [ ] Cada empregador tem: nome, CNPJ (opcional) e cargo
- [ ] Ao atingir o limite, o sistema retorna HTTP 422 com mensagem explicativa

---

## Épico 3: Pedidos de Aluguel

### US05 — Criar Pedido de Aluguel
**Como** cliente autenticado,  
**Quero** solicitar o aluguel de um automóvel disponível,  
**Para** iniciar o processo de locação.

**Critérios de Aceitação:**
- [ ] O cliente informa: automóvel desejado, data de início e data de fim
- [ ] O sistema valida que o automóvel está disponível (`disponivel = true`)
- [ ] A data de início deve ser anterior à data de fim
- [ ] O pedido é criado com status `PENDENTE`
- [ ] O sistema retorna HTTP 201 com os dados do pedido

---

### US06 — Consultar e Modificar Pedido
**Como** cliente autenticado,  
**Quero** consultar meus pedidos e modificar um pedido pendente,  
**Para** acompanhar o andamento e ajustar datas se necessário.

**Critérios de Aceitação:**
- [ ] O cliente pode listar todos os seus pedidos
- [ ] O cliente pode detalhar um pedido específico com seu status atual
- [ ] Somente pedidos com status `PENDENTE` podem ser modificados
- [ ] Tentativa de modificar pedido não-pendente retorna HTTP 422

---

### US07 — Cancelar Pedido
**Como** cliente autenticado,  
**Quero** cancelar um pedido pendente,  
**Para** desistir de um aluguel antes da avaliação.

**Critérios de Aceitação:**
- [ ] Somente pedidos com status `PENDENTE` podem ser cancelados
- [ ] Após cancelamento, o status muda para `CANCELADO`
- [ ] O automóvel permanece disponível para outros pedidos

---

## Épico 4: Avaliação e Contratos

### US08 — Avaliar Pedido (Agente)
**Como** agente do sistema (empresa ou banco),  
**Quero** avaliar pedidos pendentes, aprovando ou rejeitando,  
**Para** controlar os aluguéis de acordo com critérios financeiros.

**Critérios de Aceitação:**
- [ ] O agente pode listar todos os pedidos com status `PENDENTE`
- [ ] O agente informa parecer (`APROVADO` ou `REJEITADO`) e justificativa
- [ ] Após aprovação, o sistema gera automaticamente um contrato
- [ ] Somente pedidos `PENDENTE` podem ser avaliados
- [ ] Após avaliação, o status não pode mais ser alterado

---

### US09 — Associar Contrato de Crédito (Banco)
**Como** agente bancário,  
**Quero** associar um contrato de crédito a um aluguel aprovado,  
**Para** financiar o aluguel do cliente com condições definidas.

**Critérios de Aceitação:**
- [ ] Somente agentes do tipo `Banco` podem associar crédito
- [ ] O contrato deve existir e estar vinculado a um pedido aprovado
- [ ] Um contrato pode ter no máximo um contrato de crédito associado
- [ ] Tentativa de associar crédito duplicado retorna HTTP 422
- [ ] Os dados do crédito incluem: valor, número de parcelas e taxa de juros mensal

---

*Documento gerado na Sprint 01 · Modelagem*

---

## Épico 5: Pesquisa e Visualização de Frota (Sprint 04)

### US10 — Pesquisar Catálogo de Veículos
**Como** cliente autenticado,  
**Quero** visualizar um catálogo visual dos veículos disponíveis com fotos, preços e filtros,  
**Para** escolher o carro ideal antes de solicitar o aluguel.

**Critérios de Aceitação:**
- [ ] O sistema exibe cards visuais com imagem, marca, modelo, ano, preço diário e proprietário
- [ ] O cliente pode filtrar por texto (marca/modelo), dropdown de marca e dropdown de ano
- [ ] Somente veículos com `disponivel = true` são exibidos
- [ ] O botão "Alugar Agora" pré-seleciona o veículo no formulário de pedido

---

### US11 — Visualizar Proprietário do Veículo
**Como** cliente autenticado,  
**Quero** ver o nome do proprietário (Empresa/Banco) de cada veículo no catálogo,  
**Para** saber de qual locadora estou alugando.

**Critérios de Aceitação:**
- [ ] O card do veículo exibe uma tag com o nome do proprietário (razão social ou nome do banco)
- [ ] O nome é obtido via método `@Transient getNomeProprietario()` na entidade `Automovel`
- [ ] O JSON da API retorna automaticamente `nomeProprietario` e `tipoProprietario`

---

### US12 — Cadastrar Veículo com Imagem
**Como** agente (empresa),  
**Quero** cadastrar veículos com uma URL de imagem,  
**Para** que os clientes visualizem fotos reais no catálogo de frota.

**Critérios de Aceitação:**
- [ ] O formulário de cadastro de veículo possui campo opcional "URL da Imagem"
- [ ] A imagem é exibida nos cards tanto no painel admin quanto no catálogo do cliente
- [ ] Se a URL estiver ausente ou inválida, um placeholder visual (🚗) é exibido
- [ ] O campo `imagemUrl` (String, max 500) é persist ido na entidade `Automovel`

---

## Épico 6: Acesso Bancário (Sprint 05)

### US13 — Login como Banco
**Como** agente bancário,  
**Quero** me autenticar no sistema com minhas credenciais de banco,  
**Para** acessar o painel de gestão de créditos bancários.

**Critérios de Aceitação:**
- [ ] O sistema autentica o Banco via `POST /login` e retorna token JWT com `ROLE_BANCO`
- [ ] O login do Banco é verificado após Cliente e Empresa no `AuthService`
- [ ] Após login, o frontend redireciona para `/bank-dashboard.html`
- [ ] `ROLE_BANCO` é uma role separada de `ROLE_AGENTE` — Banco não acessa funcionalidades de Empresa

---

### US14 — Listar Contratos Pendentes de Crédito
**Como** agente bancário autenticado,  
**Quero** visualizar todos os contratos aprovados que ainda não possuem crédito associado,  
**Para** identificar oportunidades de financiamento.

**Critérios de Aceitação:**
- [ ] O endpoint `GET /contratos/pendentes-credito` retorna apenas contratos sem `ContratoCredito`
- [ ] Somente usuários com `ROLE_BANCO` podem acessar esse endpoint
- [ ] A tela exibe: ID do contrato, período, valor final, status de assinatura
- [ ] Se não houver contratos pendentes, uma mensagem informativa é exibida

---

### US15 — Associar Crédito com Dados Completos
**Como** agente bancário autenticado,  
**Quero** associar um contrato de crédito com valor, parcelas e taxa de juros a um contrato aprovado,  
**Para** financiar o aluguel do cliente com condições definidas pelo banco.

**Critérios de Aceitação:**
- [ ] O endpoint `POST /contratos/{id}/credito` aceita `valor`, `parcelas` e `taxaJuros` via query string
- [ ] Somente usuários com `ROLE_BANCO` podem executar essa ação
- [ ] O Banco autenticado é resolvido automaticamente do JWT (não mais `null`)
- [ ] A taxa de juros mensal é persistida no campo `taxaJurosMensal` de `ContratoCredito`
- [ ] Um contrato pode ter no máximo um crédito associado — tentativa duplicada retorna HTTP 422
- [ ] Após associação, o contrato sai da lista de pendentes

---

## Épico 7: Fluxo de Crédito Iniciado pelo Cliente (Sprint 05.1)

### US16 — Solicitar Financiamento ao Criar Pedido
**Como** cliente,  
**Quero** indicar que necessito de financiamento bancário ao criar um pedido de aluguel,  
**Para** que o banco possa avaliar e associar crédito ao meu contrato.

**Critérios de Aceitação:**
- [ ] O formulário de novo pedido possui checkbox "Necessito de financiamento bancário"
- [ ] O campo `necessitaCredito` (Boolean) é enviado no `POST /pedidos` e persistido no `Pedido`
- [ ] Se não marcado, `necessitaCredito` assume `false` como default
- [ ] O campo é imutável após criação — `PUT /pedidos/{id}` não altera o flag
- [ ] O card do pedido exibe badge "💳 Crédito" quando `necessitaCredito = true`
- [ ] O painel admin também exibe o badge nos pedidos pendentes
- [ ] Ao aprovar o pedido, `necessitaCredito` é propagado do Pedido para o Contrato gerado
- [ ] O endpoint `GET /contratos/pendentes-credito` retorna apenas contratos com `necessitaCredito = true`
- [ ] Contratos sem solicitação de crédito nunca aparecem na fila do Banco

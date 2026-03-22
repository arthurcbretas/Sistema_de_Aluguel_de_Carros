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

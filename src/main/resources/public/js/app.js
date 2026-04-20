const API_BASE = window.location.origin;

// ── Cache de catálogo (para filtros) ──────────────────────────────────
let allCatalogoCars = [];

// ── Authentication & Routing ──────────────────────────────────────────
function getToken() {
  return localStorage.getItem('jwt_token');
}

function setToken(token) {
  localStorage.setItem('jwt_token', token);
}

function getAuthHeaders() {
  const token = getToken();
  return {
    'Content-Type': 'application/json',
    ...(token ? { 'Authorization': `Bearer ${token}` } : {})
  };
}

function checkAuthAndRoute() {
  const path = window.location.pathname;
  const isClientDash = path === '/dashboard.html';
  const isAdminDash = path.includes('admin-dashboard');
  const isBankDash = path.includes('bank-dashboard');
  const isIndex = !isClientDash && !isAdminDash && !isBankDash;
  const token = getToken();
  
  if ((isClientDash || isAdminDash || isBankDash) && !token) {
    window.location.href = '/index.html';
    return;
  }

  let role = 'ROLE_CLIENTE';
  let payload = {};
  if (token) {
    try {
      payload = JSON.parse(atob(token.split('.')[1]));
      // ⚠️ ROLE_BANCO DEVE ser verificado ANTES de ROLE_AGENTE
      if (payload.roles && payload.roles.includes('ROLE_BANCO')) {
        role = 'ROLE_BANCO';
      } else if (payload.roles && payload.roles.includes('ROLE_AGENTE')) {
        role = 'ROLE_AGENTE';
      }
    } catch(e) {}
  }

  // Sem token na index → fica na index (não redireciona)
  if (isIndex && !token) {
    return;
  }

  // Redirect da index para o dashboard correto (com token)
  if (isIndex && token) {
    if (role === 'ROLE_BANCO') window.location.href = '/bank-dashboard.html';
    else if (role === 'ROLE_AGENTE') window.location.href = '/admin-dashboard.html';
    else window.location.href = '/dashboard.html';
    return;
  }

  // Guards: redireciona se o usuário acessar a página errada manualmente
  if (token) {
    if (role === 'ROLE_BANCO' && !isBankDash) {
      window.location.href = '/bank-dashboard.html';
      return;
    }
    if (role === 'ROLE_AGENTE' && !isAdminDash) {
      window.location.href = '/admin-dashboard.html';
      return;
    }
    if (role === 'ROLE_CLIENTE' && !isClientDash) {
      window.location.href = '/dashboard.html';
      return;
    }
  }

  // Carrega dados conforme a página
  if (isClientDash) {
    loadAutomoveis();
    loadPedidos();
  } else if (isAdminDash) {
    loadPendentes();
  } else if (isBankDash) {
    loadContratosPendentes();
  }
    
  try {
    const display = document.getElementById('usernameDisplay');
    if(display) display.textContent = payload.sub || 'Usuário';
  } catch(e) {}
}

function logout() {
  localStorage.removeItem('jwt_token');
  window.location.href = '/index.html';
}

function toggleAuthMode() {
  const loginForm = document.getElementById('loginForm').parentElement;
  const registerModal = document.getElementById('registerModal');
  
  if (registerModal.classList.contains('active')) {
    registerModal.classList.remove('active');
  } else {
    registerModal.classList.add('active');
  }
}

// ── Listeners Baseados na Página ──────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
  checkAuthAndRoute();

  const loginForm = document.getElementById('loginForm');
  if (loginForm) {
    loginForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const login = document.getElementById('loginUsername').value;
      const senha = document.getElementById('loginPassword').value;
      
      try {
        const response = await fetch(`${API_BASE}/login`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ username: login, password: senha })
        });
        
        if (!response.ok) throw new Error('Login falhou');
        
        const data = await response.json();
        setToken(data.access_token);
        checkAuthAndRoute();
      } catch (err) {
        document.getElementById('loginError').classList.remove('hidden');
      }
    });
  }

  const registerForm = document.getElementById('registerForm');
  if (registerForm) {
    registerForm.addEventListener('submit', async (e) => {
      e.preventDefault();

      const cpfRaw = document.getElementById('regCpf').value.replace(/[^\d]/g, '');

      const payload = {
        nome: document.getElementById('regNome').value.trim(),
        cpf: cpfRaw,
        rg: document.getElementById('regRg').value.trim(),
        profissao: document.getElementById('regProfissao').value.trim(),
        login: document.getElementById('regLogin').value.trim(),
        senha: document.getElementById('regSenha').value
      };

      const msgDiv = document.getElementById('registerMsg');
      const btnSubmit = document.getElementById('btnCadastrar');

      // Helper para exibir feedback inline
      const showMsg = (text, isError) => {
        msgDiv.textContent = text;
        msgDiv.style.display = 'block';
        msgDiv.style.background = isError ? 'rgba(220,53,69,0.15)' : 'rgba(40,167,69,0.15)';
        msgDiv.style.color = isError ? '#ff6b6b' : '#51cf66';
        msgDiv.style.border = `1px solid ${isError ? '#ff6b6b' : '#51cf66'}`;
      };

      btnSubmit.textContent = 'Criando...';
      btnSubmit.disabled = true;
      msgDiv.style.display = 'none';

      try {
        const response = await fetch(`${API_BASE}/auth/register`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        });

        if (response.ok) {
          showMsg('✅ Conta criada! Faça login para continuar.', false);
          e.target.reset();
          // Fecha o modal após 2 segundos para o usuário ver a mensagem
          setTimeout(() => {
            msgDiv.style.display = 'none';
            toggleAuthMode();
          }, 2000);
        } else {
          let errorMsg = 'Verifique os dados e tente novamente.';
          try {
            const errData = await response.json();
            if (errData.message) errorMsg = errData.message;
            else if (errData._embedded?.errors?.[0]?.message) errorMsg = errData._embedded.errors[0].message;
            else if (response.status === 409) errorMsg = 'E-mail ou CPF já cadastrado.';
            else if (response.status === 400) errorMsg = 'Campos inválidos. Verifique e tente novamente.';
          } catch (_) {}
          showMsg(`❌ Erro: ${errorMsg}`, true);
        }
      } catch (err) {
        showMsg('❌ Sem conexão com o servidor. Tente novamente.', true);
      } finally {
        btnSubmit.textContent = 'Cadastrar';
        btnSubmit.disabled = false;
      }
    });
  }

  const novoPedidoForm = document.getElementById('novoPedidoForm');
  if (novoPedidoForm) {
    novoPedidoForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const idCarro = document.getElementById('carroSelect').value;
      if(!idCarro) return;

      const payload = {
        idAutomovel: parseInt(idCarro),
        dataInicioAluguel: document.getElementById('dataInicio').value,
        dataFimAluguel: document.getElementById('dataFim').value,
        necessitaCredito: document.getElementById('necessitaCredito').checked
      };

      try {
        const response = await fetch(`${API_BASE}/pedidos`, {
          method: 'POST',
          headers: getAuthHeaders(),
          body: JSON.stringify(payload)
        });

        if (response.ok) {
          closeNovoPedidoModal();
          loadPedidos();
        } else {
          const errData = await response.json();
          const errDiv = document.getElementById('pedidoErrorMsg');
          errDiv.textContent = errData.message || 'Erro ao criar pedido.';
          errDiv.classList.remove('hidden');
        }
      } catch (err) {
        console.error(err);
      }
    });
  }

  const novoCarroForm = document.getElementById('novoCarroForm');
  if (novoCarroForm) {
    novoCarroForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const payload = {
        marca: document.getElementById('carMarca').value,
        modelo: document.getElementById('carModelo').value,
        ano: parseInt(document.getElementById('carAno').value),
        placa: document.getElementById('carPlaca').value,
        matricula: document.getElementById('carMatricula').value,
        precoDiaria: parseFloat(document.getElementById('carPrecoDiaria').value) || 150.00,
        imagemUrl: document.getElementById('carImagemUrl')?.value || null
      };

      try {
        const res = await fetch(`${API_BASE}/automoveis`, {
          method: 'POST',
          headers: getAuthHeaders(),
          body: JSON.stringify(payload)
        });

        if (res.ok) {
          closeNovoCarroModal();
          loadFrota();
        } else {
          alert('Erro ao registrar veículo. Matrícula/Placa podem estar duplicadas.');
        }
      } catch (err) {
        alert('Erro de rede.');
      }
    });
  }
});

// ── Funções de Negócio do Dashboard ───────────────────────────────────
async function loadAutomoveis() {
  const select = document.getElementById('carroSelect');
  if (!select) return;

  try {
    const res = await fetch(`${API_BASE}/automoveis`, { headers: getAuthHeaders() });
    if (!res.ok) throw new Error();
    const carros = await res.json();
    
    select.innerHTML = '<option value="">Selecione...</option>' + 
      carros.filter(c => c.disponivel).map(c => 
        `<option value="${c.idAutomovel}">${c.marca} ${c.modelo} - (${c.matricula})</option>`
      ).join('');
  } catch (err) {
    select.innerHTML = '<option value="">Erro ao carregar veículos</option>';
  }
}

async function loadPedidos() {
  const grid = document.getElementById('pedidosGrid');
  const loading = document.getElementById('loading');
  const empty = document.getElementById('noPedidos');
  if (!grid) return;

  try {
    const res = await fetch(`${API_BASE}/pedidos`, { headers: getAuthHeaders() });
    if (!res.ok) throw new Error();
    const pedidos = await res.json();
    
    loading.classList.add('hidden');
    if (pedidos.length === 0) {
      empty.classList.remove('hidden');
      grid.classList.add('hidden');
    } else {
      empty.classList.add('hidden');
      grid.classList.remove('hidden');
      
      grid.innerHTML = pedidos.map(p => `
        <div class="card" onclick="openDetalhesModal(${p.idPedido})">
          <span class="badge status-${p.status}">${p.status}</span>
          ${p.necessitaCredito ? '<span class="badge" style="background:#3498db;">\uD83D\uDCB3 Crédito</span>' : ''}
          <h4>Pedido #${p.idPedido}</h4>
          <p style="margin-bottom: 0.5rem">Início: ${new Date(p.dataInicioAluguel).toLocaleDateString()}</p>
          <p style="margin-bottom: 0">Fim: ${new Date(p.dataFimAluguel).toLocaleDateString()}</p>
          <strong style="display:block; margin-top: 1rem; color: var(--primary);">
            Total Estimado: R$ ${p.valorEstimado ? p.valorEstimado.toFixed(2) : 'A calcular'}
          </strong>
        </div>
      `).join('');
    }
  } catch (e) {
    loading.textContent = "Erro ao carregar dados.";
  }
}

let activePedidoId = null;

async function openDetalhesModal(id) {
  activePedidoId = id;
  const modal = document.getElementById('detalhesPedidoModal');
  const content = document.getElementById('detalhesContent');
  
  content.innerHTML = '<p>Carregando...</p>';
  modal.classList.add('active');

  const btnCancelar = document.getElementById('btnCancelarPedido');
  
  try {
    const res = await fetch(`${API_BASE}/pedidos/${id}`, { headers: getAuthHeaders() });
    const p = await res.json();
    
    content.innerHTML = `
      <p><strong>Status:</strong> <span class="badge status-${p.status}">${p.status}</span></p>
      <p><strong>Criação:</strong> ${new Date(p.dataCriacao).toLocaleString()}</p>
      <p><strong>Período:</strong> ${p.dataInicioAluguel} até ${p.dataFimAluguel}</p>
      <p><strong>Automóvel ID:</strong> ${p.idAutomovel}</p>
      <p><strong>Custo:</strong> R$ ${p.valorEstimado}</p>
    `;
    
    if (p.status === 'PENDENTE' || p.status === 'EM_ANALISE') {
      btnCancelar.classList.remove('hidden');
    } else {
      btnCancelar.classList.add('hidden');
    }
    
  } catch (e) {
    content.innerHTML = '<p style="color:var(--danger)">Falha ao carregar detalhes.</p>';
  }
}

document.getElementById('btnCancelarPedido')?.addEventListener('click', async () => {
  if (!activePedidoId || !confirm('Tem certeza que deseja cancelar este pedido?')) return;
  
  try {
    const res = await fetch(`${API_BASE}/pedidos/${activePedidoId}`, {
      method: 'DELETE',
      headers: getAuthHeaders()
    });
    if (res.ok || res.status === 204) {
      closeDetalhesModal();
      loadPedidos();
    } else {
      alert("Falha ao cancelar o pedido.");
    }
  } catch(e) {
    alert("Erro na requisição.");
  }
});

function openNovoPedidoModal() {
  document.getElementById('pedidoErrorMsg').classList.add('hidden');
  document.getElementById('novoPedidoModal').classList.add('active');
}

function closeNovoPedidoModal() {
  document.getElementById('novoPedidoModal').classList.remove('active');
  document.getElementById('novoPedidoForm').reset();
}

function closeDetalhesModal() {
  document.getElementById('detalhesPedidoModal').classList.remove('active');
  activePedidoId = null;
}

// ── Admin Functions ─────────────────────────────────────────────────────
window.loadPendentes = async function() {
  const grid = document.getElementById('pedidosGrid');
  const loading = document.getElementById('loading');
  const empty = document.getElementById('noPedidos');
  if (!grid) return;

  try {
    const res = await fetch(`${API_BASE}/pedidos/pendentes`, { headers: getAuthHeaders() });
    if (!res.ok) throw new Error();
    const pedidos = await res.json();
    
    loading.classList.add('hidden');
    if (pedidos.length === 0) {
      empty.classList.remove('hidden');
      grid.classList.add('hidden');
    } else {
      empty.classList.add('hidden');
      grid.classList.remove('hidden');
      
      grid.innerHTML = pedidos.map(p => `
        <div class="card" onclick="openAdminDetalhesModal(${p.idPedido})">
          <span class="badge status-${p.status}">${p.status}</span>
          ${p.necessitaCredito ? '<span class="badge" style="background:#3498db;">\uD83D\uDCB3 Crédito</span>' : ''}
          <h4>Pedido #${p.idPedido}</h4>
          <p>Cliente ID: ${p.idCliente}</p>
          <p>Auto ID: ${p.idAutomovel}</p>
          <strong style="display:block; margin-top: 1rem; color: var(--primary);">Est: R$ ${p.valorEstimado.toFixed(2)}</strong>
        </div>
      `).join('');
    }
  } catch (e) {
    loading.textContent = "Erro ao carregar pendências.";
  }
};

window.openAdminDetalhesModal = async function(id) {
  activePedidoId = id;
  const modal = document.getElementById('detalhesPedidoModal');
  const content = document.getElementById('detalhesContent');
  content.innerHTML = '<p>Carregando...</p>';
  modal.classList.add('active');

  try {
    const res = await fetch(`${API_BASE}/pedidos/${id}`, { headers: getAuthHeaders() });
    const p = await res.json();
    content.innerHTML = `
      <p><strong>Pedido ID:</strong> ${p.idPedido}</p>
      <p><strong>Cliente:</strong> ${p.idCliente}</p>
      <p><strong>Automóvel ID:</strong> ${p.idAutomovel}</p>
      <p><strong>Início:</strong> ${p.dataInicioAluguel}</p>
      <p><strong>Fim:</strong> ${p.dataFimAluguel}</p>
      <p><strong>Valor Estimado:</strong> R$ ${p.valorEstimado.toFixed(2)}</p>
    `;
  } catch (e) {
    content.innerHTML = '<p style="color:var(--danger)">Erro ao carregar detalhes do pedido.</p>';
  }
};

window.avaliarPedido = async function(parecer) {
  if (!activePedidoId) return;
  const justificativaElem = document.getElementById('justificativa');
  const justificativa = justificativaElem ? justificativaElem.value : "";

  try {
    const res = await fetch(`${API_BASE}/avaliacoes/${activePedidoId}`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({ parecer, justificativa })
    });
    if (res.ok) {
      alert(`O pedido foi marcado como ${parecer} com sucesso!`);
      closeDetalhesModal();
      loadPendentes();
    } else {
      const err = await res.json();
      alert(`Erro ao processar: ${err.message || 'Desconhecido'}`);
    }
  } catch (e) {
    alert("Erro crítico na requisição de avaliação.");
  }
};

window.switchAdminTab = function(tab) {
  const pedidosBtn = document.getElementById('tabPedidosBtn');
  const frotaBtn = document.getElementById('tabFrotaBtn');
  const pedidosSec = document.getElementById('pedidosSection');
  const frotaSec = document.getElementById('frotaSection');

  if (tab === 'pedidos') {
    pedidosBtn.classList.replace('secondary', 'active');
    frotaBtn.classList.replace('active', 'secondary');
    pedidosSec.classList.remove('hidden');
    frotaSec.classList.add('hidden');
    loadPendentes();
  } else {
    frotaBtn.classList.replace('secondary', 'active');
    pedidosBtn.classList.replace('active', 'secondary');
    frotaSec.classList.remove('hidden');
    pedidosSec.classList.add('hidden');
    loadFrota();
  }
};

window.loadFrota = async function() {
  const grid = document.getElementById('frotaGrid');
  const loading = document.getElementById('loadingFrota');
  const empty = document.getElementById('noFrota');
  if (!grid) return;

  try {
    const res = await fetch(`${API_BASE}/automoveis/meus`, { headers: getAuthHeaders() });
    if (!res.ok) throw new Error();
    const carros = await res.json();
    
    loading.classList.add('hidden');
    if (carros.length === 0) {
      empty.classList.remove('hidden');
      grid.classList.add('hidden');
    } else {
      empty.classList.add('hidden');
      grid.classList.remove('hidden');
      
      grid.innerHTML = carros.map(c => `
        <div class="car-card" style="${!c.disponivel ? 'opacity: 0.6;' : ''}">
          ${c.imagemUrl 
            ? `<img src="${c.imagemUrl}" alt="${c.marca} ${c.modelo}" class="car-card-img" onerror="this.outerHTML='<div class=\\'car-card-img-placeholder\\'>🚗</div>'">`
            : '<div class="car-card-img-placeholder">🚗</div>'}
          <div class="car-card-body">
            <span class="badge ${c.disponivel ? 'status-APROVADO' : 'status-CANCELADO'}" style="width:fit-content;">
               ${c.disponivel ? 'Disponível' : 'Indisponível'}
            </span>
            <h4>${c.marca} ${c.modelo} (${c.ano})</h4>
            ${c.nomeProprietario ? `<span class="car-card-owner">🏢 ${c.nomeProprietario}</span>` : ''}
            <p class="car-card-meta">Placa: ${c.placa} · Matrícula: ${c.matricula}</p>
            <div class="car-card-price">R$ ${c.precoDiaria ? c.precoDiaria.toFixed(2) : '---'}/dia</div>
          </div>
          <div class="car-card-actions d-flex" style="gap: 0.5rem; justify-content: space-between;">
            <button class="secondary" style="font-size: 0.70rem; padding: 0.4rem;" onclick="toggleAutomovel(${c.idAutomovel})">
              ${c.disponivel ? 'Suspender' : 'Ativar'}
            </button>
            <button class="primary" style="font-size: 0.70rem; padding: 0.4rem; background: var(--primary); border: none; color: #fff; cursor: pointer; border-radius: 4px;" onclick="editarPreco(${c.idAutomovel}, ${c.precoDiaria})">Editar R$</button>
            <button class="danger" style="font-size: 0.70rem; padding: 0.4rem;" onclick="deleteAutomovel(${c.idAutomovel})">Excluir</button>
          </div>
        </div>
      `).join('');
    }
  } catch (e) {
    loading.textContent = "Erro ao carregar frota.";
  }
};

window.editarPreco = async function(id, precoAtual) {
  const novoPreco = prompt(`Entre o novo valor da diária (Atual: R$ ${precoAtual ? precoAtual.toFixed(2) : '---'}):`);
  if (!novoPreco) return;
  const precoNum = parseFloat(novoPreco.replace(',','.'));
  if (isNaN(precoNum) || precoNum <= 0) {
    alert("Valor inválido.");
    return;
  }
  
  try {
    const res = await fetch(`${API_BASE}/automoveis/${id}/preco`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify({ precoDiaria: precoNum })
    });
    if (res.ok) {
      loadFrota(); // Recarrega os cards
    } else {
      alert("Erro ao alterar o preço. Verifique sua conexão ou tente novamente.");
    }
  } catch (e) {
    console.error(e);
    alert("Erro de comunicação com o servidor.");
  }
};


window.openNovoCarroModal = function() {
  document.getElementById('novoCarroModal').classList.add('active');
}

window.closeNovoCarroModal = function() {
  document.getElementById('novoCarroModal').classList.remove('active');
  document.getElementById('novoCarroForm')?.reset();
}

window.toggleAutomovel = async function(id) {
  try {
    const res = await fetch(`${API_BASE}/automoveis/${id}/disponibilidade`, {
      method: 'PUT',
      headers: getAuthHeaders()
    });
    if (res.ok) {
      loadFrota();
    } else {
      alert("Erro ao alterar disponibilidade.");
    }
  } catch (e) {
    console.error(e);
  }
};

window.deleteAutomovel = async function(id) {
  if (!confirm("Tem certeza que deseja excluir permanentemente este veículo da base?")) return;
  
  try {
    const res = await fetch(`${API_BASE}/automoveis/${id}`, {
      method: 'DELETE',
      headers: getAuthHeaders()
    });
    if (res.ok) {
      loadFrota();
    } else {
      const err = await res.text();
      alert(err || "Erro ao deletar o veículo.");
    }
  } catch (e) {
    console.error(e);
  }
};

// ── Client Tab Switching (Pedidos / Catálogo de Frota) ──────────────────

window.switchClientTab = function(tab) {
  const pedidosBtn = document.getElementById('tabMeusPedidosBtn');
  const frotaBtn = document.getElementById('tabFrotaCatalogoBtn');
  const pedidosSec = document.getElementById('meusPedidosSection');
  const frotaSec = document.getElementById('frotaCatalogoSection');

  if (!pedidosBtn || !frotaBtn) return;

  if (tab === 'pedidos') {
    pedidosBtn.className = 'active';
    frotaBtn.className = 'secondary';
    pedidosSec.classList.remove('hidden');
    frotaSec.classList.add('hidden');
    loadPedidos();
  } else {
    frotaBtn.className = 'active';
    pedidosBtn.className = 'secondary';
    frotaSec.classList.remove('hidden');
    pedidosSec.classList.add('hidden');
    loadFrotaCatalogo();
  }
};

// ── Fleet Catalog (Client-Side) ─────────────────────────────────────────

async function loadFrotaCatalogo() {
  const grid = document.getElementById('catalogoGrid');
  const loading = document.getElementById('loadingCatalogo');
  const empty = document.getElementById('noCatalogo');
  if (!grid) return;

  try {
    const res = await fetch(`${API_BASE}/automoveis`, { headers: getAuthHeaders() });
    if (!res.ok) throw new Error();
    const carros = await res.json();
    
    allCatalogoCars = carros.filter(c => c.disponivel);
    loading.classList.add('hidden');
    
    populateFilters(allCatalogoCars);
    renderCatalogo(allCatalogoCars);
  } catch (e) {
    loading.textContent = "Erro ao carregar catálogo de veículos.";
  }
}

function populateFilters(carros) {
  const marcaSelect = document.getElementById('filterMarca');
  const anoSelect = document.getElementById('filterAno');
  if (!marcaSelect || !anoSelect) return;

  const marcas = [...new Set(carros.map(c => c.marca))].sort();
  const anos = [...new Set(carros.map(c => c.ano))].sort((a, b) => b - a);

  marcaSelect.innerHTML = '<option value="">Todas as Marcas</option>' + 
    marcas.map(m => `<option value="${m}">${m}</option>`).join('');
  
  anoSelect.innerHTML = '<option value="">Todos os Anos</option>' + 
    anos.map(a => `<option value="${a}">${a}</option>`).join('');
}

function filterFrotaCatalogo() {
  const search = (document.getElementById('searchFrota')?.value || '').toLowerCase();
  const marca = document.getElementById('filterMarca')?.value || '';
  const ano = document.getElementById('filterAno')?.value || '';

  let filtered = allCatalogoCars;

  if (search) {
    filtered = filtered.filter(c => 
      c.marca.toLowerCase().includes(search) || 
      c.modelo.toLowerCase().includes(search) ||
      c.placa.toLowerCase().includes(search) ||
      (c.nomeProprietario && c.nomeProprietario.toLowerCase().includes(search))
    );
  }
  if (marca) {
    filtered = filtered.filter(c => c.marca === marca);
  }
  if (ano) {
    filtered = filtered.filter(c => c.ano === parseInt(ano));
  }

  renderCatalogo(filtered);
}

function renderCatalogo(carros) {
  const grid = document.getElementById('catalogoGrid');
  const empty = document.getElementById('noCatalogo');
  if (!grid) return;

  if (carros.length === 0) {
    empty.classList.remove('hidden');
    grid.classList.add('hidden');
  } else {
    empty.classList.add('hidden');
    grid.classList.remove('hidden');
    
    grid.innerHTML = carros.map(c => `
      <div class="car-card">
        ${c.imagemUrl 
          ? `<img src="${c.imagemUrl}" alt="${c.marca} ${c.modelo}" class="car-card-img" onerror="this.outerHTML='<div class=\\'car-card-img-placeholder\\'>🚗</div>'">`
          : '<div class="car-card-img-placeholder">🚗</div>'}
        <div class="car-card-body">
          <h4>${c.marca} ${c.modelo} (${c.ano})</h4>
          ${c.nomeProprietario ? `<span class="car-card-owner">🏢 Proprietário: ${c.nomeProprietario}</span>` : ''}
          <p class="car-card-meta">Placa: ${c.placa} · Matrícula: ${c.matricula}</p>
          <div class="car-card-price">R$ ${c.precoDiaria ? c.precoDiaria.toFixed(2) : '---'}/dia</div>
        </div>
        <div class="car-card-actions">
          <button onclick="alugarAgora(${c.idAutomovel}, '${c.marca} ${c.modelo}')">Alugar Agora</button>
        </div>
      </div>
    `).join('');
  }
}

/**
 * Atalho para abrir o modal de pedido com o carro já selecionado.
 */
window.alugarAgora = function(idAutomovel, nomeAutomovel) {
  // Muda para aba de pedidos e abre o modal
  switchClientTab('pedidos');
  
  // Aguarda a select ser populada e seleciona o carro
  setTimeout(() => {
    const select = document.getElementById('carroSelect');
    if (select) {
      // Tenta selecionar o carro pelo ID
      for (let opt of select.options) {
        if (opt.value == idAutomovel) {
          opt.selected = true;
          break;
        }
      }
    }
    openNovoPedidoModal();
  }, 300);
};

// ── Bank Dashboard Functions ────────────────────────────────────────────

let activeCreditoContratoId = null;

window.switchBankTab = function(tab) {
  const pendentesBtn = document.getElementById('tabPendentesBtn');
  const historicoBtn = document.getElementById('tabHistoricoBtn');
  const pendentesSec = document.getElementById('pendentesSection');
  const historicoSec = document.getElementById('historicoSection');

  if (!pendentesBtn || !historicoBtn) return;

  if (tab === 'pendentes') {
    pendentesBtn.classList.replace('secondary', 'active');
    historicoBtn.classList.replace('active', 'secondary');
    pendentesSec.classList.remove('hidden');
    historicoSec.classList.add('hidden');
    loadContratosPendentes();
  } else {
    historicoBtn.classList.replace('secondary', 'active');
    pendentesBtn.classList.replace('active', 'secondary');
    historicoSec.classList.remove('hidden');
    pendentesSec.classList.add('hidden');
    loadCreditosAssociados();
  }
};

/**
 * Carrega contratos aprovados que ainda não possuem crédito associado.
 * Endpoint: GET /contratos/pendentes-credito (ROLE_BANCO)
 */
window.loadContratosPendentes = async function() {
  const grid = document.getElementById('pendentesGrid');
  const loading = document.getElementById('loadingPendentes');
  const empty = document.getElementById('noPendentes');
  if (!grid) return;

  try {
    const res = await fetch(`${API_BASE}/contratos/pendentes-credito`, { headers: getAuthHeaders() });
    if (!res.ok) throw new Error();
    const contratos = await res.json();

    loading.classList.add('hidden');
    if (contratos.length === 0) {
      empty.classList.remove('hidden');
      grid.classList.add('hidden');
    } else {
      empty.classList.add('hidden');
      grid.classList.remove('hidden');

      grid.innerHTML = contratos.map(c => `
        <div class="card">
          <span class="badge status-APROVADO">Aprovado</span>
          <span class="badge" style="background:#3498db;">\uD83D\uDCB3 Crédito Solicitado</span>
          <h4>Contrato #${c.idContrato}</h4>
          <p><strong>Período:</strong> ${c.dataInicio} — ${c.dataFim}</p>
          <p><strong>Valor Final:</strong> R$ ${c.valorFinal ? c.valorFinal.toFixed(2) : '---'}</p>
          <button onclick="openCreditoModal(${c.idContrato}, ${c.valorFinal || 0})" style="margin-top:1rem; width:100%; font-size:0.85rem;">
            💳 Associar Crédito
          </button>
        </div>
      `).join('');
    }
  } catch (e) {
    loading.textContent = "Erro ao carregar contratos pendentes.";
  }
};

/**
 * Abre o modal para associar crédito a um contrato.
 */
window.openCreditoModal = function(idContrato, valorSugerido) {
  activeCreditoContratoId = idContrato;
  const info = document.getElementById('creditoContratoInfo');
  if (info) {
    info.innerHTML = `<p style="margin-bottom:1rem;"><strong>Contrato:</strong> #${idContrato}</p>`;
  }
  document.getElementById('creditoErrorMsg')?.classList.add('hidden');
  document.getElementById('creditoForm')?.reset();
  // Pré-preenche o valor do crédito com o valor final do contrato
  if (valorSugerido && valorSugerido > 0) {
    const creditoValorInput = document.getElementById('creditoValor');
    if (creditoValorInput) creditoValorInput.value = valorSugerido.toFixed(2);
  }
  document.getElementById('creditoModal').classList.add('active');
};

window.closeCreditoModal = function() {
  document.getElementById('creditoModal').classList.remove('active');
  activeCreditoContratoId = null;
};

/**
 * Carrega créditos já associados pelo banco logado.
 * Endpoint: GET /contratos/creditos-banco (ROLE_BANCO)
 */
window.loadCreditosAssociados = async function() {
  const grid = document.getElementById('historicoGrid');
  const loading = document.getElementById('loadingHistorico');
  const empty = document.getElementById('noHistorico');
  if (!grid) return;

  try {
    const res = await fetch(`${API_BASE}/contratos/creditos-banco`, { headers: getAuthHeaders() });
    if (!res.ok) throw new Error();
    const creditos = await res.json();

    loading.classList.add('hidden');
    if (creditos.length === 0) {
      empty.classList.remove('hidden');
      grid.classList.add('hidden');
    } else {
      empty.classList.add('hidden');
      grid.classList.remove('hidden');

      grid.innerHTML = creditos.map(c => `
        <div class="card">
          <span class="badge" style="background:#2ecc71;">\u2705 Associado</span>
          <h4>Contrato #${c.idContrato}</h4>
          <p><strong>Valor Crédito:</strong> R$ ${c.valorCredito ? c.valorCredito.toFixed(2) : '---'}</p>
          <p><strong>Parcelas:</strong> ${c.parcelas}x</p>
          <p><strong>Taxa Juros:</strong> ${c.taxaJurosMensal ? c.taxaJurosMensal.toFixed(2) : '0'}% a.m.</p>
        </div>
      `).join('');
    }
  } catch (e) {
    loading.textContent = "Erro ao carregar créditos.";
  }
};

// Listener do formulário de crédito
document.addEventListener('DOMContentLoaded', () => {
  const creditoForm = document.getElementById('creditoForm');
  if (creditoForm) {
    creditoForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      if (!activeCreditoContratoId) return;

      const valor = document.getElementById('creditoValor').value;
      const parcelas = document.getElementById('creditoParcelas').value;
      const taxaJuros = document.getElementById('creditoTaxa').value;
      const errDiv = document.getElementById('creditoErrorMsg');

      try {
        // ⚠️ Parâmetros via query string, NÃO body JSON (@QueryValue no controller)
        const url = `${API_BASE}/contratos/${activeCreditoContratoId}/credito?valor=${valor}&parcelas=${parcelas}&taxaJuros=${taxaJuros}`;
        const res = await fetch(url, {
          method: 'POST',
          headers: getAuthHeaders()
        });

        if (res.ok || res.status === 201) {
          closeCreditoModal();
          loadContratosPendentes();
          alert('✅ Crédito associado com sucesso!');
        } else {
          const err = await res.json();
          errDiv.textContent = err.message || 'Erro ao associar crédito.';
          errDiv.classList.remove('hidden');
        }
      } catch (err) {
        errDiv.textContent = 'Erro de rede ao associar crédito.';
        errDiv.classList.remove('hidden');
      }
    });
  }
});

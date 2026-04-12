const API_BASE = window.location.origin;

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
  const isDashboard = window.location.pathname.includes('dashboard.html') && !window.location.pathname.includes('admin');
  const isAdmin = window.location.pathname.includes('admin-dashboard.html');
  const isIndex = !isDashboard && !isAdmin;
  const token = getToken();
  
  if ((isDashboard || isAdmin) && !token) {
    window.location.href = '/index.html';
    return;
  }

  let role = 'ROLE_CLIENTE';
  let payload = {};
  if (token) {
    try {
      payload = JSON.parse(atob(token.split('.')[1]));
      if (payload.roles && payload.roles.includes('ROLE_AGENTE')) {
        role = 'ROLE_AGENTE';
      }
    } catch(e) {}
  }

  if (isIndex && token) {
    window.location.href = role === 'ROLE_AGENTE' ? '/admin-dashboard.html' : '/dashboard.html';
    return;
  }

  if (isDashboard && role === 'ROLE_AGENTE') {
     window.location.href = '/admin-dashboard.html';
     return;
  }
  if (isAdmin && role === 'ROLE_CLIENTE') {
     window.location.href = '/dashboard.html';
     return;
  }

  if (isDashboard || isAdmin) {
    if (isDashboard) {
      loadAutomoveis();
      loadPedidos();
    } else {
      loadPendentes();
    }
    
    try {
      const display = document.getElementById('usernameDisplay');
      if(display) display.textContent = payload.sub || 'Usuário';
    } catch(e) {}
  }
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
        status: "PENDENTE"
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
        precoDiaria: parseFloat(document.getElementById('carPrecoDiaria').value) || 150.00
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
        <div class="card" style="${!c.disponivel ? 'opacity: 0.6;' : ''}">
          <span class="badge ${c.disponivel ? 'status-APROVADO' : 'status-CANCELADO'}" style="float:right;">
             ${c.disponivel ? 'Disponível' : 'Indisponível'}
          </span>
          <h4>${c.marca} ${c.modelo} (${c.ano})</h4>
          <p>Placa: ${c.placa}</p>
          <p>Matrícula: ${c.matricula}</p>
          <strong style="display:block; margin: 0.5rem 0; color: var(--primary);">R$ ${c.precoDiaria ? c.precoDiaria.toFixed(2) : '---'}/dia</strong>
          <div class="d-flex" style="margin-top: 1rem; gap: 0.5rem;">
            <button class="secondary" style="font-size: 0.75rem; padding: 0.25rem;" onclick="toggleAutomovel(${c.idAutomovel})">
              ${c.disponivel ? 'Suspender' : 'Ativar'}
            </button>
            <button class="danger" style="font-size: 0.75rem; padding: 0.25rem;" onclick="deleteAutomovel(${c.idAutomovel})">Exc</button>
          </div>
        </div>
      `).join('');
    }
  } catch (e) {
    loading.textContent = "Erro ao carregar frota.";
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

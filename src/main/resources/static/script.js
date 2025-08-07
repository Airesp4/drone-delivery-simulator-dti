let pendingOrders = [];
let allOrders = [];
let stats = {
    routesCreated: 0,
    averageOrdersPerRoute: 0,
    totalDeliveredOrders: 0,
    freeDrones: 5
};

const priorityMap = {
    'baixa': 'LOW',
    'média': 'MEDIUM',
    'alta': 'HIGH'
};

document.addEventListener('DOMContentLoaded', function() {
    init();
});

function init() {
    loadData();
    setupEventListeners();
    updateUI();
    fetchStats();
    fetchAllOrders();
}

function setupEventListeners() {
    document.querySelectorAll('.tab-button').forEach(button => {
        button.addEventListener('click', () => switchTab(button.dataset.tab));
    });

    document.getElementById('single-order-form').addEventListener('submit', handleSingleOrder);
    document.getElementById('batch-order-form').addEventListener('submit', handleAddToBatch);
}

function switchTab(tabName) {
    document.querySelectorAll('.tab-button').forEach(btn => btn.classList.remove('active'));
    document.querySelector(`[data-tab="${tabName}"]`).classList.add('active');
    
    document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));
    document.getElementById(`${tabName}-tab`).classList.add('active');
}

function updateUI() {
    updateStatsUI();
    updatePendingOrdersList();
}

function updateStatsUI() {
    document.getElementById('routes-created').textContent = stats.routesCreated;
    document.getElementById('average-orders').textContent = stats.averageOrdersPerRoute.toFixed(1);
    document.getElementById('total-delivered').textContent = stats.totalDeliveredOrders;
    document.getElementById('free-drones').textContent = stats.freeDrones;
}

function updatePendingOrdersList() {
    const ordersList = document.getElementById('orders-list');
    const ordersCount = document.getElementById('orders-count');
    const sendButton = document.getElementById('send-orders-list');
    const clearButton = document.getElementById('clear-orders');

    ordersCount.textContent = pendingOrders.length;
    sendButton.disabled = pendingOrders.length === 0;
    sendButton.innerHTML = `<i class="fas fa-plane"></i> Enviar Lista (${pendingOrders.length})`;
    clearButton.style.display = pendingOrders.length > 0 ? 'flex' : 'none';

    if (pendingOrders.length === 0) {
        ordersList.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-box"></i>
                <p>Nenhum pedido na lista</p>
                <span>Adicione pedidos para começar</span>
            </div>
        `;
        return;
    }

    const priorityIcons = {
        'baixa': 'fas fa-circle',
        'média': 'fas fa-exclamation-triangle',
        'alta': 'fas fa-exclamation-circle'
    };

    ordersList.innerHTML = pendingOrders.map((order, index) => {
        return `
            <div class="order-item">
                <div class="order-header">
                    <span class="order-number">#${index + 1}</span>
                    <div class="order-actions">
                        <span class="priority-badge ${order.priority}">
                            <i class="${priorityIcons[order.priority]}"></i>
                            ${order.priority}
                        </span>
                        <button class="remove-btn" onclick="removeOrder('${order.id}')">
                            <i class="fas fa-trash"></i>
                        </button>
                    </div>
                </div>
                <div class="order-details">
                    <div class="order-detail">
                        <i class="fas fa-map-marker-alt"></i>
                        <span>X: ${order.locationX}, Y: ${order.locationY}</span>
                    </div>
                    <div class="order-detail">
                        <i class="fas fa-box"></i>
                        <span>${order.weight} kg</span>
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

async function fetchAllOrders() {
    const allOrdersList = document.getElementById('all-orders-list');
    const refreshBtn = document.querySelector('.all-orders-section .refresh-btn');
    
    allOrdersList.innerHTML = `
        <div class="loading-state">
            <i class="fas fa-spinner fa-spin"></i>
            <p>Carregando pedidos...</p>
        </div>
    `;
    
    refreshBtn.classList.add('loading');
    
    try {
        const response = await fetch('/pedidos/all');
        
        if (response.ok) {
            allOrders = await response.json();
            updateAllOrdersList();
            showToast('Pedidos Carregados', 'Lista de pedidos atualizada com sucesso.');
        } else {
            throw new Error(`Erro ${response.status}`);
        }
    } catch (error) {
        console.error('Erro ao buscar pedidos:', error);
        allOrdersList.innerHTML = `
            <div class="empty-orders-state">
                <i class="fas fa-exclamation-triangle"></i>
                <p>Erro ao carregar pedidos</p>
                <span>Verifique a conexão com o servidor</span>
            </div>
        `;
        showToast('Erro de Conexão', 'Não foi possível carregar os pedidos.', 'error');
    } finally {
        refreshBtn.classList.remove('loading');
    }
}

function updateAllOrdersList() {
    const allOrdersList = document.getElementById('all-orders-list');
    
    if (allOrders.length === 0) {
        allOrdersList.innerHTML = `
            <div class="empty-orders-state">
                <i class="fas fa-box-open"></i>
                <p>Nenhum pedido encontrado</p>
                <span>Os pedidos aparecerão aqui quando forem criados</span>
            </div>
        `;
        return;
    }

    const statusIcons = {
        'DELIVERED': 'fas fa-check-circle',
        'PENDING': 'fas fa-clock',
        'IN_TRANSIT': 'fas fa-shipping-fast',
        'CANCELLED': 'fas fa-times-circle',
        'RECUSED': 'fas fa-ban'
    };

    const statusLabels = {
        'DELIVERED': 'Entregue',
        'PENDING': 'Pendente',
        'ON_ROUTE': 'Em Trânsito',
        'RECUSED': 'Recusado'
    };

    const priorityLabels = {
        'LOW': 'Baixa',
        'MEDIUM': 'Média',
        'HIGH': 'Alta'
    };

    allOrdersList.innerHTML = allOrders.map(order => {
        const statusClass = order.state.toLowerCase().replace('_', '-');
        const statusIcon = statusIcons[order.state] || 'fas fa-question-circle';
        const statusLabel = statusLabels[order.state] || order.state;
        const priorityLabel = priorityLabels[order.priority] || order.priority;
        
        return `
            <div class="all-order-item">
                <div class="all-order-header">
                    <span class="all-order-id">Pedido #${order.id}</span>
                    <span class="status-badge ${statusClass}">
                        <i class="${statusIcon}"></i>
                        ${statusLabel}
                    </span>
                </div>
                <div class="all-order-details">
                    <div class="all-order-detail">
                        <i class="fas fa-map-marker-alt"></i>
                        <span>
                            <span class="detail-label">Posição:</span>
                            <span class="detail-value">X: ${order.clientPositionX}, Y: ${order.clientPositionY}</span>
                        </span>
                    </div>
                    <div class="all-order-detail">
                        <i class="fas fa-weight-hanging"></i>
                        <span>
                            <span class="detail-label">Peso:</span>
                            <span class="detail-value">${order.payloadKg} kg</span>
                        </span>
                    </div>
                    <div class="all-order-detail">
                        <i class="fas fa-exclamation-triangle"></i>
                        <span>
                            <span class="detail-label">Prioridade:</span>
                            <span class="detail-value priority-text ${order.priority}">${priorityLabel}</span>
                        </span>
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

function showToast(title, description, type = 'success') {
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `
        <div class="toast-title">${title}</div>
        <div class="toast-description">${description}</div>
    `;
    
    document.getElementById('toast-container').appendChild(toast);
    
    setTimeout(() => {
        toast.remove();
    }, 3000);
}

function generateId() {
    return Date.now().toString(36) + Math.random().toString(36).substr(2, 9);
}

function getFormData(prefix) {
    return {
        locationX: parseFloat(document.getElementById(`${prefix}-x`).value),
        locationY: parseFloat(document.getElementById(`${prefix}-y`).value),
        weight: parseFloat(document.getElementById(`${prefix}-weight`).value),
        priority: document.getElementById(`${prefix}-priority`).value
    };
}

function resetForm(prefix) {
    document.getElementById(`${prefix}-x`).value = '';
    document.getElementById(`${prefix}-y`).value = '';
    document.getElementById(`${prefix}-weight`).value = '';
    document.getElementById(`${prefix}-priority`).value = '';
}

function validateFormData(data) {
    return data.locationX && data.locationY && data.weight && data.priority;
}

async function fetchStats() {
    const refreshBtn = document.querySelector('.refresh-btn');
    refreshBtn.classList.add('loading');
    
    try {
        const [statsResponse, dronesResponse] = await Promise.all([
            fetch('/estatisticas'),
            fetch('/drones/status')
        ]);

        if (statsResponse.ok && dronesResponse.ok) {
            const apiStats = await statsResponse.json();
            const drones = await dronesResponse.json();
            const availableDrones = drones.filter(d => d.status === 'IDLE').length;

            stats = {
                routesCreated: apiStats.totalRoutes || 0,
                averageOrdersPerRoute: apiStats.averageOrdersPerRoute || 0,
                totalDeliveredOrders: apiStats.totalOrdersCompleted || 0,
                freeDrones: availableDrones
            };

            updateStatsUI();
            showToast('Estatísticas Atualizadas', 'Dados sincronizados com sucesso.');
        } else {
            throw new Error('Falha na comunicação com o servidor');
        }
    } catch (error) {
        console.error('Erro ao buscar estatísticas:', error);
        showToast('Erro de Conexão', 'Não foi possível carregar as estatísticas.', 'error');
    } finally {
        refreshBtn.classList.remove('loading');
    }
}

async function handleSingleOrder(e) {
    e.preventDefault();
    
    const formData = getFormData('single');
    if (!validateFormData(formData)) {
        showToast('Campos Obrigatórios', 'Preencha todos os campos.', 'error');
        return;
    }

    const submitBtn = e.target.querySelector('button[type="submit"]');
    const originalText = submitBtn.innerHTML;
    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Enviando...';
    submitBtn.disabled = true;

    const apiPayload = {
        clientPositionX: formData.locationX,
        clientPositionY: formData.locationY,
        payloadKg: formData.weight,
        priority: priorityMap[formData.priority]
    };

    try {
        const response = await fetch('/pedidos', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(apiPayload),
        });

        if (response.ok) {
            const order = await response.json();

            resetForm('single');
            await fetchStats();

            if (order.state === 'RECUSED') {
                showToast('Pedido Recusado', 'Este pedido não pode ser entregue por nenhum drone.', 'error');
            } else {
                showToast('Pedido Enviado', 'Pedido processado com sucesso.');
            }
        } else {
            throw new Error(`Erro ${response.status}`);
        }
    } catch (error) {
        console.error('Erro ao enviar pedido:', error);
        showToast('Falha no Envio', 'Não foi possível processar o pedido.', 'error');
    } finally {
        submitBtn.innerHTML = originalText;
        submitBtn.disabled = false;
    }
}

function handleAddToBatch(e) {
    e.preventDefault();
    
    const formData = getFormData('batch');
    if (!validateFormData(formData)) {
        showToast('Campos Obrigatórios', 'Preencha todos os campos.', 'error');
        return;
    }

    const order = { 
        id: generateId(), 
        ...formData 
    };
    
    pendingOrders.push(order);
    resetForm('batch');
    saveData();
    updateUI();
    showToast('Pedido Adicionado', 'Pedido adicionado à lista.');
}

async function sendAllOrders() {
    if (pendingOrders.length === 0) return;

    const sendBtn = document.getElementById('send-orders-list');
    const originalText = sendBtn.innerHTML;
    sendBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Enviando...';
    sendBtn.disabled = true;

    let successfulCount = 0;
    let refusedCount = 0;
    const ordersToSend = [...pendingOrders];

    for (const order of ordersToSend) {
        const apiPayload = {
            clientPositionX: order.locationX,
            clientPositionY: order.locationY,
            payloadKg: order.weight,
            priority: priorityMap[order.priority]
        };

        try {
            const response = await fetch('/pedidos', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(apiPayload),
            });

            const responseData = await response.json();

            if (response.ok) {
                successfulCount++;
                pendingOrders = pendingOrders.filter(o => o.id !== order.id);
            } else if (responseData.state === 'RECUSED') {
                refusedCount++;
                pendingOrders = pendingOrders.filter(o => o.id !== order.id);
                showToast('Pedido Recusado', `Pedido com peso ${order.weight}kg foi recusado.`, 'warning');
            }
        } catch (error) {
            console.error('Erro ao enviar pedido:', error);
        }
    }

    if (successfulCount > 0) {
        showToast('Lista Enviada', `${successfulCount} pedidos enviados com sucesso.`);
        await fetchStats();
    }

    if (refusedCount === 0 && successfulCount === 0) {
        showToast('Falha no Envio', 'Nenhum pedido foi enviado.', 'error');
    }

    saveData();
    updateUI();
    sendBtn.innerHTML = originalText;
    sendBtn.disabled = false;
}

function removeOrder(orderId) {
    pendingOrders = pendingOrders.filter(order => order.id !== orderId);
    saveData();
    updateUI();
    showToast('Pedido Removido', 'Pedido removido da lista.');
}

function clearOrders() {
    if (pendingOrders.length === 0) return;
    
    pendingOrders = [];
    saveData();
    updateUI();
    showToast('Lista Limpa', 'Todos os pedidos foram removidos.');
}

function refreshStats() {
    fetchStats();
}

function saveData() {
    localStorage.setItem('drone-pending-orders', JSON.stringify(pendingOrders));
}

function loadData() {
    const savedOrders = localStorage.getItem('drone-pending-orders');
    if (savedOrders) {
        try {
            pendingOrders = JSON.parse(savedOrders);
        } catch (error) {
            console.error('Erro ao carregar dados:', error);
            pendingOrders = [];
        }
    }
}
// Astra Web Dashboard Controller
let currentCategory = 'COMBAT';
let allModules = [];

// Initialize bridge with Minecraft or Mock
const Astra = window.AstraAPI || {
  getModules: () => [
    { name: 'KillAura', category: 'COMBAT', description: 'Автоматическая атака по целям', enabled: true },
    { name: 'AimAssist', category: 'COMBAT', description: 'Автодоводка и аим для лука', enabled: true },
    { name: 'Velocity', category: 'COMBAT', description: 'Управление отбрасыванием', enabled: false },
    { name: 'Sprint', category: 'MOVEMENT', description: 'Автоматический спринт', enabled: true },
    { name: 'Fly', category: 'MOVEMENT', description: 'Режим свободного полета', enabled: false },
    { name: 'InstantSneak', category: 'MOVEMENT', description: 'Резкий shift без сглаживания', enabled: true },
    { name: 'ESP', category: 'RENDER', description: '3D боксы, здоровье и трейсеры', enabled: true },
    { name: 'JumpCircles', category: 'RENDER', description: 'Светящиеся кольца при прыжке', enabled: true },
    { name: 'TargetESP', category: 'RENDER', description: '3D кольцо под целью атаки', enabled: true },
    { name: 'CustomCrosshair', category: 'RENDER', description: 'Кастомный прицел 32x32', enabled: true },
    { name: 'MiniMap', category: 'HUD', description: 'Радар и карта местности', enabled: true },
    { name: 'ArmorHUD', category: 'HUD', description: 'Прочность и элементы брони', enabled: true }
  ],
  toggleModule: (name) => {
    console.log('[AstraAPI] Toggled module:', name);
  },
  setTheme: (theme) => {
    console.log('[AstraAPI] Set theme:', theme);
  },
  close: () => {
    console.log('[AstraAPI] Close requested');
  }
};

document.addEventListener('DOMContentLoaded', () => {
  setupTabs();
  setupSearch();
  setupThemes();
  setupClose();
  loadModules();
});

function loadModules() {
  try {
    const raw = Astra.getModules();
    allModules = typeof raw === 'string' ? JSON.parse(raw) : raw;
  } catch (e) {
    console.error('Failed to parse modules:', e);
  }
  renderModules();
}

function renderModules() {
  const grid = document.getElementById('modulesGrid');
  const query = document.getElementById('searchInput').value.toLowerCase().trim();

  const filtered = allModules.filter(m => {
    const matchesCat = (m.category === currentCategory) || (query.length > 0);
    const matchesQuery = !query || m.name.toLowerCase().includes(query) || (m.description && m.description.toLowerCase().includes(query));
    return matchesCat && matchesQuery;
  });

  document.getElementById('currentCatTitle').innerText = query ? `Поиск: "${query}"` : currentCategory;
  document.getElementById('moduleCountBadge').innerText = `${filtered.length} модулей`;

  grid.innerHTML = filtered.map(mod => `
    <div class="module-card ${mod.enabled ? 'enabled' : ''}" id="card-${mod.name}">
      <div class="mod-top">
        <span class="mod-name">${mod.name}</span>
        <label class="switch">
          <input type="checkbox" ${mod.enabled ? 'checked' : ''} onchange="toggleModule('${mod.name}', this.checked)">
          <span class="slider"></span>
        </label>
      </div>
      <p class="mod-desc">${mod.description || 'Нет описания'}</p>
    </div>
  `).join('');
}

window.toggleModule = function(name, state) {
  const mod = allModules.find(m => m.name === name);
  if (mod) mod.enabled = state;

  const card = document.getElementById(`card-${name}`);
  if (card) {
    if (state) card.classList.add('enabled');
    else card.classList.remove('enabled');
  }

  Astra.toggleModule(name);
};

function setupTabs() {
  const tabs = document.querySelectorAll('.tab-btn');
  tabs.forEach(btn => {
    btn.addEventListener('click', () => {
      tabs.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      currentCategory = btn.getAttribute('data-cat');
      document.getElementById('searchInput').value = '';
      renderModules();
    });
  });
}

function setupSearch() {
  const input = document.getElementById('searchInput');
  input.addEventListener('input', () => {
    renderModules();
  });
}

function setupThemes() {
  const dots = document.querySelectorAll('.theme-dot');
  const colors = {
    'Indigo': '#6366F1',
    'Cyan': '#06B6D4',
    'Emerald': '#10B981',
    'Crimson': '#EF4444',
    'Sunset': '#F97316'
  };

  dots.forEach(dot => {
    dot.addEventListener('click', () => {
      dots.forEach(d => d.classList.remove('active'));
      dot.classList.add('active');
      const theme = dot.getAttribute('data-theme');
      document.documentElement.style.setProperty('--accent', colors[theme] || '#6366F1');
      Astra.setTheme(theme);
    });
  });
}

function setupClose() {
  document.getElementById('closeBtn').addEventListener('click', () => {
    Astra.close();
  });
}

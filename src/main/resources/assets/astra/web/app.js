// Astra Web Dashboard Controller - Complete Client Modules & Settings
let currentCategory = 'COMBAT';
let activeFilter = 'all'; // 'all', 'enabled', 'settings'
let bindingModule = null;

// Complete Module Catalog matching Astra Client (Fabric 1.16.5)
const DEFAULT_MODULES = [
  // COMBAT
  {
    name: 'KillAura',
    category: 'COMBAT',
    description: 'Автоматическая атака врагов с умными таймингами комбо и ротацией взгляда',
    enabled: true,
    key: 'R',
    settings: [
      { name: 'Дистанция', type: 'NUMBER', value: 3.8, min: 3.0, max: 6.0, step: 0.1 },
      { name: 'CPS (Кликов/сек)', type: 'NUMBER', value: 12.0, min: 1.0, max: 20.0, step: 1.0 },
      { name: 'Угол обзора (FOV)', type: 'NUMBER', value: 180.0, min: 30.0, max: 360.0, step: 10.0 },
      { name: 'Ротация', type: 'MODE', value: 'Плавная', modes: ['Плавная', 'Резкая', 'Тихая'] },
      { name: 'Только игроки', type: 'BOOLEAN', value: true },
      { name: 'Сквозь стены', type: 'BOOLEAN', value: false }
    ]
  },
  {
    name: 'AimAssist',
    category: 'COMBAT',
    description: 'Интеллектуальная доводка прицела на цель и баллистический расчет траектории лука',
    enabled: true,
    key: 'None',
    settings: [
      { name: 'Дистанция', type: 'NUMBER', value: 4.5, min: 3.0, max: 8.0, step: 0.2 },
      { name: 'Скорость доводки', type: 'NUMBER', value: 4.0, min: 1.0, max: 10.0, step: 0.5 },
      { name: 'Баллистика лука', type: 'BOOLEAN', value: true },
      { name: 'Быстрый снап', type: 'BOOLEAN', value: true }
    ]
  },
  {
    name: 'Velocity',
    category: 'COMBAT',
    description: 'Умное снижение отбрасывания и нокбэка от ударов под античиты',
    enabled: true,
    key: 'None',
    settings: [
      { name: 'Горизонтально (%)', type: 'NUMBER', value: 0.0, min: 0.0, max: 100.0, step: 5.0 },
      { name: 'Вертикально (%)', type: 'NUMBER', value: 100.0, min: 0.0, max: 100.0, step: 5.0 },
      { name: 'Шанс (%)', type: 'NUMBER', value: 100.0, min: 0.0, max: 100.0, step: 5.0 }
    ]
  },
  {
    name: 'Reach',
    category: 'COMBAT',
    description: 'Увеличение максимальной дистанции нанесения ударов и взаимодействия',
    enabled: false,
    key: 'None',
    settings: [
      { name: 'Дистанция', type: 'NUMBER', value: 3.4, min: 3.0, max: 5.0, step: 0.1 }
    ]
  },
  {
    name: 'AutoClicker',
    category: 'COMBAT',
    description: 'Автоматическое закликивание при зажатии левой кнопки мыши',
    enabled: false,
    key: 'None',
    settings: [
      { name: 'Мин. CPS', type: 'NUMBER', value: 9.0, min: 1.0, max: 20.0, step: 1.0 },
      { name: 'Макс. CPS', type: 'NUMBER', value: 14.0, min: 1.0, max: 20.0, step: 1.0 },
      { name: 'Рандомизация', type: 'BOOLEAN', value: true }
    ]
  },
  {
    name: 'EntityLauncher',
    category: 'COMBAT',
    description: 'Подбрасывание окружающих сущностей в воздух мощным импульсом',
    enabled: false,
    key: 'None',
    settings: [
      { name: 'Сила импульса', type: 'NUMBER', value: 2.0, min: 0.5, max: 5.0, step: 0.5 }
    ]
  },

  // MOVEMENT
  {
    name: 'Sprint',
    category: 'MOVEMENT',
    description: 'Постоянный автоматический спринт без двойного нажатия W',
    enabled: true,
    key: 'None',
    settings: [
      { name: 'Всегда бежать', type: 'BOOLEAN', value: true }
    ]
  },
  {
    name: 'InstantSneak',
    category: 'MOVEMENT',
    description: 'Мгновенное переключение приседания без анимационного лага камеры (Old Sneak)',
    enabled: true,
    key: 'None',
    settings: [
      { name: 'Резкий режим', type: 'BOOLEAN', value: true },
      { name: 'Скорость анимации', type: 'NUMBER', value: 1.0, min: 0.1, max: 2.0, step: 0.1 }
    ]
  },
  {
    name: 'Speed',
    category: 'MOVEMENT',
    description: 'Увеличение скорости передвижения игрока на земле и в прыжках',
    enabled: false,
    key: 'None',
    settings: [
      { name: 'Скорость', type: 'NUMBER', value: 1.25, min: 1.0, max: 2.5, step: 0.05 },
      { name: 'Режим', type: 'MODE', value: 'Bhop', modes: ['Bhop', 'Strafe', 'Ground'] }
    ]
  },
  {
    name: 'Fly',
    category: 'MOVEMENT',
    description: 'Режим свободного полета сквозь пространство',
    enabled: false,
    key: 'G',
    settings: [
      { name: 'Скорость полета', type: 'NUMBER', value: 1.5, min: 0.5, max: 5.0, step: 0.2 },
      { name: 'Режим', type: 'MODE', value: 'Creative', modes: ['Creative', 'Glide', 'Packet'] }
    ]
  },
  {
    name: 'FastClimb',
    category: 'MOVEMENT',
    description: 'Увеличенная скорость подъема по лестницам и лозам',
    enabled: false,
    key: 'None',
    settings: [
      { name: 'Скорость подъема', type: 'NUMBER', value: 2.0, min: 1.0, max: 4.0, step: 0.2 }
    ]
  },
  {
    name: 'FreeLook',
    category: 'MOVEMENT',
    description: 'Свободный круговой обзор камерой на 360° без поворота модели тела',
    enabled: false,
    key: 'C',
    settings: [
      { name: 'Инверсия', type: 'BOOLEAN', value: false }
    ]
  },
  {
    name: 'Snaplook',
    category: 'MOVEMENT',
    description: 'Быстрый разворот камеры на 180 градусов за спину',
    enabled: false,
    key: 'V',
    settings: []
  },
  {
    name: 'ToggleSneak',
    category: 'MOVEMENT',
    description: 'Фиксация режима приседания по нажатию клавиши',
    enabled: false,
    key: 'None',
    settings: []
  },
  {
    name: 'ToggleSprint',
    category: 'MOVEMENT',
    description: 'Переключаемый режим бега с отображением статуса на экране',
    enabled: false,
    key: 'None',
    settings: []
  },
  {
    name: 'AntiSwimming',
    category: 'MOVEMENT',
    description: 'Предотвращение автоматического перехода в анимацию плавания',
    enabled: false,
    key: 'None',
    settings: []
  },

  // RENDER
  {
    name: 'ESP',
    category: 'RENDER',
    description: '3D боксы игроков, индикатор здоровья, дистанция и имена через стены',
    enabled: true,
    key: 'None',
    settings: [
      { name: '3D Боксы', type: 'BOOLEAN', value: true },
      { name: 'Полоса здоровья', type: 'BOOLEAN', value: true },
      { name: 'Трейсеры (Линии)', type: 'BOOLEAN', value: false },
      { name: 'Имена и броня', type: 'BOOLEAN', value: true },
      { name: 'Цвет', type: 'MODE', value: 'Акцентный', modes: ['Акцентный', 'Здоровье', 'Белый'] }
    ]
  },
  {
    name: 'JumpCircles',
    category: 'RENDER',
    description: 'Неоновые расходящиеся волновые кольца на земле при каждом прыжке',
    enabled: true,
    key: 'None',
    settings: [
      { name: 'Радиус волны', type: 'NUMBER', value: 1.5, min: 0.5, max: 3.5, step: 0.1 },
      { name: 'Время жизни', type: 'NUMBER', value: 25.0, min: 10.0, max: 60.0, step: 5.0 },
      { name: 'Стиль колец', type: 'MODE', value: 'Неон', modes: ['Неон', 'Радуга', 'Градиент'] }
    ]
  },
  {
    name: 'TargetESP',
    category: 'RENDER',
    description: 'Светящийся 3D ореол и вращающееся кольцо вокруг атакуемой цели',
    enabled: true,
    key: 'None',
    settings: [
      { name: 'Радиус', type: 'NUMBER', value: 0.8, min: 0.4, max: 2.0, step: 0.1 },
      { name: 'Скорость вращения', type: 'NUMBER', value: 3.0, min: 1.0, max: 10.0, step: 0.5 }
    ]
  },
  {
    name: 'CustomCrosshair',
    category: 'RENDER',
    description: 'Полная кастомизация формы, цвета, точки и толщины игрового прицела',
    enabled: true,
    key: 'None',
    settings: [
      { name: 'Размер', type: 'NUMBER', value: 8.0, min: 2.0, max: 20.0, step: 1.0 },
      { name: 'Толщина', type: 'NUMBER', value: 2.0, min: 1.0, max: 6.0, step: 1.0 },
      { name: 'Центральная точка', type: 'BOOLEAN', value: true },
      { name: 'Динамический разброс', type: 'BOOLEAN', value: false }
    ]
  },
  {
    name: 'Fullbright',
    category: 'RENDER',
    description: 'Максимальная яркость в пещерах, шахтах и ночное время суток',
    enabled: true,
    key: 'B',
    settings: []
  },
  {
    name: 'Trails',
    category: 'RENDER',
    description: 'Красивый светящийся шлейф из частиц за спиной игрока при движении',
    enabled: true,
    key: 'None',
    settings: [
      { name: 'Длина шлейфа', type: 'NUMBER', value: 20.0, min: 5.0, max: 50.0, step: 5.0 },
      { name: 'Ширина линии', type: 'NUMBER', value: 2.0, min: 1.0, max: 5.0, step: 0.5 }
    ]
  },
  {
    name: 'XRay',
    category: 'RENDER',
    description: 'Сквозной просмотр ценных руд (алмазы, незерит, золото) в толще камня',
    enabled: false,
    key: 'X',
    settings: [
      { name: 'Алмазы', type: 'BOOLEAN', value: true },
      { name: 'Древние обломки', type: 'BOOLEAN', value: true },
      { name: 'Железо', type: 'BOOLEAN', value: false },
      { name: 'Золото', type: 'BOOLEAN', value: false }
    ]
  },
  {
    name: 'Zoom',
    category: 'RENDER',
    description: 'Плавное оптическое приближение камеры (аналог OptiFine Zoom)',
    enabled: true,
    key: 'C',
    settings: [
      { name: 'Кратность зума', type: 'NUMBER', value: 4.0, min: 2.0, max: 10.0, step: 0.5 },
      { name: 'Плавность', type: 'BOOLEAN', value: true }
    ]
  },
  {
    name: 'FastRender',
    category: 'RENDER',
    description: 'Отсечение невидимых сущностей и оптимизация фризов видеокарты',
    enabled: true,
    key: 'None',
    settings: []
  },
  {
    name: 'DamageTint',
    category: 'RENDER',
    description: 'Эффектная подсветка краев экрана при получении урона',
    enabled: true,
    key: 'None',
    settings: [
      { name: 'Интенсивность', type: 'NUMBER', value: 0.6, min: 0.1, max: 1.0, step: 0.1 }
    ]
  },
  {
    name: 'FogCustomizer',
    category: 'RENDER',
    description: 'Настройка дистанции и цвета тумана в мире',
    enabled: false,
    key: 'None',
    settings: [
      { name: 'Дистанция тумана', type: 'NUMBER', value: 1.0, min: 0.2, max: 2.0, step: 0.1 }
    ]
  },
  {
    name: 'FOVChanger',
    category: 'RENDER',
    description: 'Расширенная настройка угла обзора без ограничений ваниллы',
    enabled: false,
    key: 'None',
    settings: [
      { name: 'Угол FOV', type: 'NUMBER', value: 90.0, min: 30.0, max: 150.0, step: 1.0 }
    ]
  },
  {
    name: 'ItemPhysics',
    category: 'RENDER',
    description: 'Реалистичная 3D физика падения выброшенных предметов на землю',
    enabled: true,
    key: 'None',
    settings: []
  },
  {
    name: 'NoHurtCam',
    category: 'RENDER',
    description: 'Отключение тряски экрана при получении урона персонажем',
    enabled: true,
    key: 'None',
    settings: []
  },
  {
    name: 'NoRender',
    category: 'RENDER',
    description: 'Отключение ненужных частиц (взрывы, огонь, дождь) для повышения FPS',
    enabled: false,
    key: 'None',
    settings: [
      { name: 'Без дождя', type: 'BOOLEAN', value: true },
      { name: 'Без слепоты', type: 'BOOLEAN', value: true },
      { name: 'Без огня на экране', type: 'BOOLEAN', value: true }
    ]
  },
  {
    name: 'TimeChanger',
    category: 'RENDER',
    description: 'Кастомное локальное время суток в игре (день, ночь, закат)',
    enabled: false,
    key: 'None',
    settings: [
      { name: 'Время', type: 'MODE', value: 'День', modes: ['День', 'Закат', 'Ночь', 'Полночь'] }
    ]
  },
  {
    name: 'WeatherChanger',
    category: 'RENDER',
    description: 'Принудительное переключение ясной погоды или дождя',
    enabled: false,
    key: 'None',
    settings: [
      { name: 'Погода', type: 'MODE', value: 'Ясно', modes: ['Ясно', 'Дождь', 'Гроза'] }
    ]
  },

  // PLAYER
  {
    name: 'AutoTotem',
    category: 'PLAYER',
    description: 'Мгновенная автоматическая перекладка тотема бессмертия в левую руку',
    enabled: true,
    key: 'None',
    settings: [
      { name: 'Задержка (мс)', type: 'NUMBER', value: 0.0, min: 0.0, max: 200.0, step: 20.0 }
    ]
  },
  {
    name: 'FastPlace',
    category: 'PLAYER',
    description: 'Снятие задержки клика ПКМ для скоростной установки блоков',
    enabled: true,
    key: 'None',
    settings: [
      { name: 'Задержка клика', type: 'NUMBER', value: 0.0, min: 0.0, max: 4.0, step: 1.0 }
    ]
  },
  {
    name: 'NoFall',
    category: 'PLAYER',
    description: 'Предотвращение получения урона при падении с любой высоты',
    enabled: false,
    key: 'None',
    settings: [
      { name: 'Режим', type: 'MODE', value: 'Packet', modes: ['Packet', 'Catch', 'Ground'] }
    ]
  },
  {
    name: 'SafeWalk',
    category: 'PLAYER',
    description: 'Предотвращение падения с краев блоков при ходьбе и строительстве',
    enabled: false,
    key: 'None',
    settings: []
  },
  {
    name: 'Scaffold',
    category: 'PLAYER',
    description: 'Автоматическая быстрая укладка блоков под ноги при беге',
    enabled: false,
    key: 'None',
    settings: [
      { name: 'Поворот головы', type: 'BOOLEAN', value: true },
      { name: 'Снижение скорости', type: 'BOOLEAN', value: false }
    ]
  },
  {
    name: 'NoHunger',
    category: 'PLAYER',
    description: 'Снижение расхода шкалы сытости персонажа',
    enabled: false,
    key: 'None',
    settings: []
  },
  {
    name: 'AntiFire',
    category: 'PLAYER',
    description: 'Быстрое сбивание огня при возгорании персонажа',
    enabled: false,
    key: 'None',
    settings: []
  },
  {
    name: 'GodMode',
    category: 'PLAYER',
    description: 'Пакетный режим бессмертия на уязвимых серверах',
    enabled: false,
    key: 'None',
    settings: []
  },

  // HUD
  {
    name: 'MiniMap',
    category: 'HUD',
    description: 'Топографический радар с высотами блоков, метками и сущностями вокруг',
    enabled: true,
    key: 'None',
    settings: [
      { name: 'Масштаб', type: 'NUMBER', value: 1.0, min: 0.5, max: 2.0, step: 0.1 },
      { name: 'Радиус сканирования', type: 'NUMBER', value: 32.0, min: 16.0, max: 64.0, step: 8.0 },
      { name: 'Сущности на радаре', type: 'BOOLEAN', value: true }
    ]
  },
  {
    name: 'ArmorHUD',
    category: 'HUD',
    description: 'Отображение экипированной брони, предметов в руках и их прочности',
    enabled: true,
    key: 'None',
    settings: [
      { name: 'Показывать урон брони', type: 'BOOLEAN', value: true },
      { name: 'Проценты прочности', type: 'BOOLEAN', value: true }
    ]
  },
  {
    name: 'Coordinates',
    category: 'HUD',
    description: 'Индикатор текущих координат X, Y, Z, биома и направления взгляда',
    enabled: true,
    key: 'None',
    settings: [
      { name: 'Показывать биом', type: 'BOOLEAN', value: true }
    ]
  },
  {
    name: 'FPSMod',
    category: 'HUD',
    description: 'Счетчик текущего количества кадров в секунду (FPS)',
    enabled: true,
    key: 'None',
    settings: []
  },
  {
    name: 'CPSMod',
    category: 'HUD',
    description: 'Счетчик кликов левой и правой кнопки мыши в секунду (LMB/RMB CPS)',
    enabled: true,
    key: 'None',
    settings: []
  },
  {
    name: 'Keystrokes',
    category: 'HUD',
    description: 'Визуализация нажатия клавиш WASD, Пробел и кнопок мыши',
    enabled: true,
    key: 'None',
    settings: [
      { name: 'Прозрачность', type: 'NUMBER', value: 0.8, min: 0.2, max: 1.0, step: 0.1 }
    ]
  },
  {
    name: 'PingMod',
    category: 'HUD',
    description: 'Отображение сетевой задержки пинга до сервера',
    enabled: true,
    key: 'None',
    settings: []
  },
  {
    name: 'TargetHUD',
    category: 'HUD',
    description: 'Стильная карточка атакуемого игрока: аватарка, ник, здоровье и броня',
    enabled: true,
    key: 'None',
    settings: [
      { name: 'Стиль карточки', type: 'MODE', value: 'Modern', modes: ['Modern', 'Compact', 'Classic'] }
    ]
  },
  {
    name: 'DirectionHUD',
    category: 'HUD',
    description: 'Компас с указанием сторон света (N, S, W, E)',
    enabled: false,
    key: 'None',
    settings: []
  },
  {
    name: 'Cooldowns',
    category: 'HUD',
    description: 'Таймеры кулдаунов жемчуга Края, золотых яблок и щита',
    enabled: true,
    key: 'None',
    settings: []
  },
  {
    name: 'TotemCounter',
    category: 'HUD',
    description: 'Количество оставшихся тотемов бессмертия в инвентаре',
    enabled: true,
    key: 'None',
    settings: []
  },
  {
    name: 'ComboCounter',
    category: 'HUD',
    description: 'Счетчик непрерывной серии нанесенных ударов (комбо)',
    enabled: false,
    key: 'None',
    settings: []
  },
  {
    name: 'ReachDisplay',
    category: 'HUD',
    description: 'Отображение точной дистанции последнего нанесенного удара в блоках',
    enabled: true,
    key: 'None',
    settings: []
  },
  {
    name: 'Waypoints',
    category: 'HUD',
    description: '3D маркеры путевых точек на экране и в мире (клавиша B для добавления)',
    enabled: true,
    key: 'B',
    settings: []
  },

  // BUILD
  {
    name: 'FastBreak',
    category: 'BUILD',
    description: 'Ускоренное разрушение блоков',
    enabled: false,
    key: 'None',
    settings: [
      { name: 'Множитель', type: 'NUMBER', value: 1.3, min: 1.0, max: 2.0, step: 0.1 }
    ]
  },

  // MISC
  {
    name: 'AutoGG',
    category: 'MISC',
    description: 'Автоматическая отправка "gg" в чат после победы или окончания игры',
    enabled: true,
    key: 'None',
    settings: []
  },
  {
    name: 'AutoText',
    category: 'MISC',
    description: 'Автоматическая отправка настраиваемых фраз или команд по таймеру',
    enabled: false,
    key: 'None',
    settings: []
  },
  {
    name: 'ChatMod',
    category: 'MISC',
    description: 'Улучшенный чат с прозрачностью, бесконечной историей и копированием строк',
    enabled: true,
    key: 'None',
    settings: []
  },
  {
    name: 'KeySounds',
    category: 'MISC',
    description: 'Приятные звуки механических клавиатур при нажатии клавиш',
    enabled: false,
    key: 'None',
    settings: [
      { name: 'Громкость', type: 'NUMBER', value: 0.5, min: 0.1, max: 1.0, step: 0.1 },
      { name: 'Переключатели', type: 'MODE', value: 'Blue', modes: ['Blue', 'Red', 'Brown'] }
    ]
  },
  {
    name: 'OofMod',
    category: 'MISC',
    description: 'Замена стандартного звука получения урона на классический звук Oof',
    enabled: false,
    key: 'None',
    settings: []
  }
];

// In-memory module state
let allModules = [];
let expandedSettings = new Set();

// Load modules from cache, REST, or defaults
function initData() {
  const saved = localStorage.getItem('astra_modules_v2');
  if (saved) {
    try {
      allModules = JSON.parse(saved);
    } catch (e) {
      allModules = JSON.parse(JSON.stringify(DEFAULT_MODULES));
    }
  } else {
    allModules = JSON.parse(JSON.stringify(DEFAULT_MODULES));
  }
  syncWithBackend();
}

function saveData() {
  localStorage.setItem('astra_modules_v2', JSON.stringify(allModules));
}

// Background sync with client API if running
async function syncWithBackend() {
  try {
    const res = await fetch('/api/modules');
    if (res.ok) {
      const data = await res.json();
      if (Array.isArray(data) && data.length > 0) {
        allModules = data;
        saveData();
        renderModules();
        const stat = document.getElementById('bridgeStatusText');
        if (stat) stat.innerText = 'Connected to Minecraft';
      }
    }
  } catch (err) {
    // Offline mode / mock mode
  }
}

document.addEventListener('DOMContentLoaded', () => {
  initData();
  setupTabs();
  setupSearch();
  setupFilters();
  setupThemes();
  setupClose();
  setupKeybindListener();
  renderModules();
});

function renderModules() {
  const grid = document.getElementById('modulesGrid');
  const query = document.getElementById('searchInput').value.toLowerCase().trim();

  const filtered = allModules.filter(m => {
    // Category match
    const matchesCat = (m.category === currentCategory) || (query.length > 0);
    // Query match across name, description, settings
    const inName = m.name.toLowerCase().includes(query);
    const inDesc = m.description && m.description.toLowerCase().includes(query);
    const inSettings = m.settings && m.settings.some(s => s.name.toLowerCase().includes(query));
    const matchesQuery = !query || inName || inDesc || inSettings;

    // Filter chip match
    let matchesFilter = true;
    if (activeFilter === 'enabled') matchesFilter = m.enabled;
    if (activeFilter === 'settings') matchesFilter = m.settings && m.settings.length > 0;

    return matchesCat && matchesQuery && matchesFilter;
  });

  document.getElementById('currentCatTitle').innerText = query ? `Поиск: "${query}"` : currentCategory;
  document.getElementById('moduleCountBadge').innerText = `${filtered.length} модулей`;
  
  const activeTotal = allModules.filter(m => m.enabled).length;
  const activeEl = document.getElementById('activeCount');
  if (activeEl) activeEl.innerText = activeTotal;

  grid.innerHTML = filtered.map(mod => {
    const isExpanded = expandedSettings.has(mod.name);
    const hasSettings = mod.settings && mod.settings.length > 0;

    return `
      <div class="module-card ${mod.enabled ? 'enabled' : ''}" id="card-${mod.name}">
        <div class="mod-top">
          <div class="mod-title-area">
            <span class="mod-name">${mod.name}</span>
            <button class="keybind-btn ${bindingModule === mod.name ? 'binding' : ''}" 
                    onclick="startBinding('${mod.name}', event)">
              [${mod.key || 'None'}]
            </button>
          </div>

          <div class="mod-controls">
            ${hasSettings ? `
              <button class="settings-btn ${isExpanded ? 'open' : ''}" 
                      onclick="toggleSettings('${mod.name}')" 
                      title="Настройки модуля">
                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="12" cy="12" r="3"></circle>
                  <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"></path>
                </svg>
              </button>
            ` : ''}

            <label class="switch">
              <input type="checkbox" ${mod.enabled ? 'checked' : ''} onchange="toggleModule('${mod.name}', this.checked)">
              <span class="slider"></span>
            </label>
          </div>
        </div>

        <p class="mod-desc">${mod.description || 'Модуль клиента'}</p>

        ${isExpanded && hasSettings ? `
          <div class="module-settings">
            ${mod.settings.map(s => renderSetting(mod.name, s)).join('')}
          </div>
        ` : ''}
      </div>
    `;
  }).join('');
}

function renderSetting(modName, s) {
  if (s.type === 'NUMBER') {
    return `
      <div class="setting-item">
        <div class="setting-header">
          <span>${s.name}</span>
          <span class="setting-val-badge" id="val-${modName}-${s.name}">${s.value}</span>
        </div>
        <input type="range" 
               class="range-slider" 
               min="${s.min}" 
               max="${s.max}" 
               step="${s.step}" 
               value="${s.value}" 
               oninput="updateNumberSetting('${modName}', '${s.name}', this.value)">
      </div>
    `;
  } else if (s.type === 'BOOLEAN') {
    return `
      <div class="setting-bool-row">
        <span>${s.name}</span>
        <label class="switch" style="width: 32px; height: 18px;">
          <input type="checkbox" ${s.value ? 'checked' : ''} onchange="updateBoolSetting('${modName}', '${s.name}', this.checked)">
          <span class="slider"></span>
        </label>
      </div>
    `;
  } else if (s.type === 'MODE') {
    return `
      <div class="setting-item">
        <div class="setting-header">
          <span>${s.name}</span>
        </div>
        <div class="mode-chips">
          ${s.modes.map(m => `
            <button class="mode-chip ${m === s.value ? 'active' : ''}" 
                    onclick="updateModeSetting('${modName}', '${s.name}', '${m}')">
              ${m}
            </button>
          `).join('')}
        </div>
      </div>
    `;
  }
  return '';
}

window.toggleModule = function(name, state) {
  const mod = allModules.find(m => m.name === name);
  if (mod) mod.enabled = state;

  const card = document.getElementById(`card-${name}`);
  if (card) {
    if (state) card.classList.add('enabled');
    else card.classList.remove('enabled');
  }

  saveData();
  const activeEl = document.getElementById('activeCount');
  if (activeEl) activeEl.innerText = allModules.filter(m => m.enabled).length;

  // Send to backend if available
  fetch(`/api/toggle?name=${encodeURIComponent(name)}`).catch(() => {});
};

window.toggleSettings = function(name) {
  if (expandedSettings.has(name)) {
    expandedSettings.delete(name);
  } else {
    expandedSettings.add(name);
  }
  renderModules();
};

window.updateNumberSetting = function(modName, settingName, val) {
  const num = parseFloat(val);
  const badge = document.getElementById(`val-${modName}-${settingName}`);
  if (badge) badge.innerText = num;

  const mod = allModules.find(m => m.name === modName);
  if (mod) {
    const s = mod.settings.find(st => st.name === settingName);
    if (s) s.value = num;
  }
  saveData();
  fetch(`/api/setting?module=${encodeURIComponent(modName)}&setting=${encodeURIComponent(settingName)}&value=${num}`).catch(() => {});
};

window.updateBoolSetting = function(modName, settingName, val) {
  const mod = allModules.find(m => m.name === modName);
  if (mod) {
    const s = mod.settings.find(st => st.name === settingName);
    if (s) s.value = val;
  }
  saveData();
  fetch(`/api/setting?module=${encodeURIComponent(modName)}&setting=${encodeURIComponent(settingName)}&value=${val}`).catch(() => {});
};

window.updateModeSetting = function(modName, settingName, val) {
  const mod = allModules.find(m => m.name === modName);
  if (mod) {
    const s = mod.settings.find(st => st.name === settingName);
    if (s) s.value = val;
  }
  saveData();
  renderModules();
  fetch(`/api/setting?module=${encodeURIComponent(modName)}&setting=${encodeURIComponent(settingName)}&value=${encodeURIComponent(val)}`).catch(() => {});
};

window.startBinding = function(modName, event) {
  event.stopPropagation();
  bindingModule = modName;
  renderModules();
};

function setupKeybindListener() {
  window.addEventListener('keydown', (e) => {
    if (!bindingModule) return;
    e.preventDefault();

    let keyName = e.key.toUpperCase();
    if (e.key === 'Escape') {
      bindingModule = null;
      renderModules();
      return;
    }
    if (e.key === 'Backspace' || e.key === 'Delete') {
      keyName = 'None';
    }

    const mod = allModules.find(m => m.name === bindingModule);
    if (mod) {
      mod.key = keyName;
    }
    const current = bindingModule;
    bindingModule = null;
    saveData();
    renderModules();

    fetch(`/api/keybind?module=${encodeURIComponent(current)}&key=${encodeURIComponent(keyName)}`).catch(() => {});
  });
}

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

function setupFilters() {
  const chips = document.querySelectorAll('.chip');
  chips.forEach(c => {
    c.addEventListener('click', () => {
      chips.forEach(x => x.classList.remove('active'));
      c.classList.add('active');
      activeFilter = c.getAttribute('data-filter');
      renderModules();
    });
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
      document.documentElement.style.setProperty('--accent-glow', (colors[theme] || '#6366F1') + '55');
    });
  });
}

function setupClose() {
  document.getElementById('closeBtn').addEventListener('click', () => {
    if (window.close) window.close();
  });
}

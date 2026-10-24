# Astra Client

<p align="center">
  <img src="https://img.shields.io/badge/Minecraft-1.16.5-5E9C3A?style=for-the-badge" alt="Minecraft 1.16.5">
  <img src="https://img.shields.io/badge/Fabric-Loaded-62A47A?style=for-the-badge" alt="Fabric">
  <img src="https://img.shields.io/badge/License-Private-red?style=for-the-badge" alt="Private License">
</p>

<p align="center">
  <b>Astra Client</b> — это мод для Minecraft 1.16.5 на базе Fabric, предоставляющий набор модулей для PvP, движения, визуалов и интерфейса.
</p>

---

## 📋 Содержание

- [Установка](#установка)
- [Сборка](#сборка)
- [Использование](#использование)
- [Модули](#модули)
- [Интерфейс](#интерфейс)
- [Лицензия](#лицензия)

---

## 📦 Установка

1. Установите [Fabric Loader](https://fabricmc.net/use/installer/) для Minecraft 1.16.5
2. Установите [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api) в папку `mods`
3. Поместите `astra-1.0.0.jar` в папку `mods`
4. Запустите Minecraft с профилем Fabric

---

## 🔨 Сборка

Требования:
- Java 17+
- Gradle

```bash
# Клонирование репозитория
git clone <repository-url>
cd mc_cheat

# Сборка
./gradlew build

# Запуск клиента
./gradlew runClient
```

Артефакт сборки: `build/libs/astra-1.0.0.jar`

---

## 🎮 Использование

- **Right Shift** — открыть/закрыть ClickGUI
- **Клик по модулю** — включить/выключить
- **Правый клик** — раскрыть настройки модуля
- **Средний клик** — назначить клавишу

---

## 🧩 Модули

### Combat
| Модуль | Описание |
|--------|----------|
| KillAura | Автоматическая атака по целям |
| AimAssist | Помощь в прицеливании |
| AutoClicker | Автоматический клик |
| EntityLauncher | Запуск сущностей |
| Velocity | Управление отбрасыванием |
| Reach | Увеличение дальности |

### Movement
| Модуль | Описание |
|--------|----------|
| Sprint | Автоматический спринт |
| Fly | Полёт |
| Speed | Ускорение |
| FreeLook | Свободная камера |
| FastClimb | Быстрое лазание |

### Render
| Модуль | Описание |
|--------|----------|
| Fullbright | Полная яркость |
| ESP | Подсветка сущностей |
| HUD | Интерфейс |
| Keystrokes | Отображение клавиш |
| ArmorHUD | Отображение брони |
| TargetHUD | Информация о цели |

### Player
| Модуль | Описание |
|--------|----------|
| AutoTotem | Автототем |
| FastPlace | Быстрое размещение |
| NoFall | Без урона от падения |
| GodMode | Режим бога |

---

## 🎨 Интерфейс

Astra Client имеет современный веб-подобный интерфейс в стиле shadcn/ui:

- **Закруглённые элементы** — все карточки и панели с радиусом скругления
- **Гладкие тени** — многослойные тени для глубины
- **Анимированные партиклы** — интерактивные частицы на фоне
- **Красивый фон** — космическая тема с градиентами
- **Цветовые темы** — 8 встроенных тем (Indigo, Cyan, Emerald, Crimson, Sunset, Sakura, Obsidian, Rainbow)

---

## 📄 Лицензия

Этот проект распространяется под **Private License**.

- ✅ Можно скачивать и использовать для себя
- ✅ Можно модифицировать для личного использования
- ❌ Нельзя распространять
- ❌ Нельзя передавать другим лицам
- ❌ Нельзя использовать в коммерческих целях

Подробнее см. в файле [LICENSE](LICENSE).

---

<p align="center">
  <sub>© 2024 EvelentDev. All rights reserved.</sub>
</p>

@echo off
title Astra Client 1.16.5 - Official One-Click Installer (v1.1.0)
color 0B
chcp 65001 >nul
cls

echo =====================================================================
echo           ★  A S T R A   C L I E N T   1 . 1 6 . 5  ★
echo           Официальный быстрый установщик (v1.1.0 Esports Pro)
echo =====================================================================
echo.

set "TARGET_DIR=%APPDATA%\.minecraft\mods"
set "SOURCE_JAR=%~dp0astra-1.1.0.jar"

if not exist "%SOURCE_JAR%" (
    if exist "%~dp0releases\v1.1.0\astra-1.1.0.jar" (
        set "SOURCE_JAR=%~dp0releases\v1.1.0\astra-1.1.0.jar"
    )
)

if not exist "%SOURCE_JAR%" (
    if exist "%~dp0astra-1.0.0.jar" (
        set "SOURCE_JAR=%~dp0astra-1.0.0.jar"
    )
)

if not exist "%SOURCE_JAR%" (
    echo [ОШИБКА] Файл astra-1.1.0.jar не найден рядом с установщиком!
    echo Пожалуйста, убедитесь, что архив распакован полностью.
    echo.
    pause
    exit /b 1
)

echo [1/3] Проверка папки Minecraft: %APPDATA%\.minecraft
if not exist "%APPDATA%\.minecraft" (
    echo [ВНИМАНИЕ] Папка .minecraft не найдена по стандартному пути.
    echo Создаем директорию...
    mkdir "%APPDATA%\.minecraft"
)

echo [2/3] Подготовка папки mods...
if not exist "%TARGET_DIR%" (
    mkdir "%TARGET_DIR%"
)

echo [3/3] Копирование Astra Client v1.1.0 в mods...
copy /y "%SOURCE_JAR%" "%TARGET_DIR%\astra-1.1.0.jar" >nul

if %ERRORLEVEL% EQU 0 (
    echo.
    echo =====================================================================
    echo  [УСПЕХ] Astra Client v1.1.0 успешно установлен в папку модов!
    echo  Путь: %TARGET_DIR%\astra-1.1.0.jar
    echo =====================================================================
    echo.
    echo  ИНСТРУКЦИЯ ПО ЗАПУСКУ:
    echo   1. Откройте любой лаунчер (Astra Launcher, TLauncher, Prism, Modrinth)
    echo   2. Выберите профиль: Fabric Loader 1.16.5
    echo   3. Запустите игру!
    echo.
    echo  ГОРЯЧИЕ КЛАВИШИ В ИГРЕ:
    echo   • Right Shift  - Открыть Shadcn UI меню (слайдеры, бинды, модули)
    echo   • Right Alt    - Редактор элементов HUD
    echo   • M            - Открыть интерактивную карту мира (World Map)
    echo   • B            - Поставить путевую точку (Waypoint)
    echo   • Браузер      - http://127.0.0.1:3000 (Внешний Web Dashboard)
    echo =====================================================================
) else (
    echo.
    echo [ОШИБКА] Не удалось скопировать мод.
    echo Убедитесь, что Minecraft закрыт и повторите запуск от имени администратора.
)

echo.
pause

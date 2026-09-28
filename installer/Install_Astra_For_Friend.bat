@echo off
title Astra Client 1.16.5 - One-Click Installer
color 0A
chcp 65001 >nul
cls
echo ========================================================
echo        ★ УСТАНОВЩИК ASTRA CLIENT 1.16.5 (FABRIC) ★
echo ========================================================
echo.
echo Установка фирменного чита/клиента Astra в ваш Minecraft...
echo.

set "TARGET_DIR=%APPDATA%\.minecraft\mods"

if not exist "%TARGET_DIR%" (
    echo Создание папки mods: %TARGET_DIR%
    mkdir "%TARGET_DIR%"
)

if exist "%~dp0astra-1.0.0.jar" (
    copy /y "%~dp0astra-1.0.0.jar" "%TARGET_DIR%\astra-1.0.0.jar" >nul
) else (
    copy /y "%~dp0..\build\libs\astra-1.0.0.jar" "%TARGET_DIR%\astra-1.0.0.jar" >nul
)

if %ERRORLEVEL% EQU 0 (
    echo [УСПЕХ] Мод astra-1.0.0.jar успешно установлен в:
    echo        %TARGET_DIR%\astra-1.0.0.jar
    echo.
    echo ========================================================
    echo  ИНСТРУКЦИЯ ПО ЗАПУСКУ:
    echo   1. Запустите TLauncher / Minecraft Launcher
    echo   2. Выберите версию: Fabric 1.16.5
    echo   3. В игре нажмите RShift для открытия Dashboard
    echo ========================================================
) else (
    echo [ОШИБКА] Не удалось скопировать файл мода. Убедитесь, что Minecraft закрыт.
)

echo.
pause

@echo off
setlocal
cd /d "%~dp0"
title StockFlow - Testes Automatizados

echo ============================================================
echo           STOCKFLOW - TESTES AUTOMATIZADOS
echo ============================================================
echo.

where mvn >nul 2>nul
if errorlevel 1 (
    echo ERRO: Apache Maven nao foi encontrado no PATH.
    echo Instale o Maven e adicione a pasta bin ao PATH do Windows.
    echo Guia oficial: https://maven.apache.org/install.html
    echo.
    pause
    exit /b 1
)

echo Executando testes JUnit 5...
echo O primeiro teste pode demorar enquanto o Maven baixa as dependencias.
echo.
call mvn test
set TEST_RESULT=%ERRORLEVEL%
echo.
if not "%TEST_RESULT%"=="0" (
    echo RESULTADO: foram encontrados erros ou falhas nos testes.
    echo Consulte os detalhes em target\surefire-reports\
) else (
    echo RESULTADO: todos os testes foram concluidos sem falhas.
    echo Relatorios: target\surefire-reports\
)
echo.
pause
exit /b %TEST_RESULT%

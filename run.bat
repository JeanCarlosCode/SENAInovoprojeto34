@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul

echo ====================================
echo   Iniciando Cadastro de Produtos
echo ====================================

REM Procura javac e java no PATH
set "JAVAC="
set "JAVA="

where javac >nul 2>nul
if %errorlevel% equ 0 (
    set "JAVAC=javac"
    set "JAVA=java"
) else (
    REM Procura em locais comuns caso não esteja no PATH
    for %%p in (
        "C:\Program Files\Android\Android Studio\jbr\bin"
        "C:\Program Files\Java\jdk*"
        "C:\Program Files\Eclipse Adoptium\jdk*"
    ) do (
        if exist "%%~p\javac.exe" (
            set "JAVAC=%%~p\javac.exe"
            set "JAVA=%%~p\java.exe"
        )
    )
)

if "%JAVAC%"=="" (
    echo [ERRO] JDK não encontrado! Por favor, importe e execute pelo Eclipse.
    echo Pressione qualquer tecla para sair...
    pause >nul
    exit /b 1
)

echo [OK] JDK encontrado. Crtl+C para interromper.

REM Compilando
echo [..] Compilando codigo...
if not exist "bin" mkdir bin
if not exist "bin\view" mkdir bin\view

"%JAVAC%" -encoding UTF-8 -cp "lib\mysql-connector-j-8.3.0.jar;lib\javafx-sdk-21.0.2\lib\*" -d bin src\model\*.java src\dao\*.java src\controller\*.java src\application\*.java

REM Copiando resources (FXML e CSS)
copy src\view\*.fxml bin\view\ >nul
copy src\view\*.css bin\view\ >nul

REM Executando
echo [OK] Executando aplicacao...
"%JAVA%" -Dfile.encoding=UTF-8 --module-path "lib\javafx-sdk-21.0.2\lib" --add-modules javafx.controls,javafx.fxml -cp "bin;lib\mysql-connector-j-8.3.0.jar" application.App

pause

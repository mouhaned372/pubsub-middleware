@echo off
chcp 65001 >nul
title Middleware Pub-Sub Simple

echo.
echo ====================================================
echo   MIDDLEWARE PUB-SUB - DÉMARRAGE SIMPLE
echo ====================================================
echo.

REM Compiler si nécessaire
if not exist "target\classes" (
    echo 📦 Compilation en cours...
    mvn clean compile
    if errorlevel 1 (
        echo ❌ Erreur de compilation
        pause
        exit /b 1
    )
)

echo.
echo 🚀 Lancement de la démonstration...
echo 📝 Messages affichés dans cette console
echo ⏱️  Durée : 60 secondes
echo.
echo Appuyez sur Ctrl+C pour arrêter prématurément
echo.

mvn exec:java -Dexec.mainClass="fr.telecom.middleware.examples.DashboardDemo" -q

echo.
echo ✅ Démonstration terminée !
echo.
pause
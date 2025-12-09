#!/bin/bash

echo "========================================"
echo " Middleware Pub-Sub - Temps Réel"
echo "========================================"
echo

# Vérifier Maven
if ! command -v mvn &> /dev/null; then
    echo "ERREUR: Maven n'est pas installé!"
    echo "Téléchargez-le depuis: https://maven.apache.org"
    exit 1
fi

show_menu() {
    echo "MENU PRINCIPAL:"
    echo "1. Compiler le projet"
    echo "2. Exécuter les tests"
    echo "3. Lancer la démonstration"
    echo "4. Exécuter les benchmarks"
    echo "5. Tout exécuter"
    echo "6. Quitter"
    echo
}

while true; do
    show_menu
    read -p "Votre choix (1-6): " choice

    case $choice in
        1)
            echo "Compilation en cours..."
            mvn clean compile
            if [ $? -eq 0 ]; then
                echo "SUCCÈS: Compilation terminée!"
            fi
            ;;
        2)
            echo "Exécution des tests..."
            mvn test
            ;;
        3)
            echo "Lancement de la démonstration..."
            mvn exec:java -Dexec.mainClass="fr.telecom.middleware.examples.CriticalSystemDemo"
            ;;
        4)
            echo "Exécution des benchmarks..."
            mvn exec:java -Dexec.mainClass="fr.telecom.middleware.test.PerformanceBenchmark"
            ;;
        5)
            echo "Exécution complète..."
            mvn clean compile test
            mvn exec:java -Dexec.mainClass="fr.telecom.middleware.examples.CriticalSystemDemo"
            ;;
        6)
            echo "Au revoir!"
            exit 0
            ;;
        *)
            echo "Choix invalide!"
            ;;
    esac

    echo
    read -p "Appuyez sur Entrée pour continuer..."
    clear
done
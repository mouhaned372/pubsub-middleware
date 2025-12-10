#!/bin/bash

# ================================================
# Middleware Pub-Sub Temps Réel - Script d'exécution
# Version: 1.0.0
# Auteur: Télécom Eniso
# ================================================

# Configuration
SCRIPT_NAME="build-and-run.sh"
VERSION="1.0.0"
PROJECT_NAME="Middleware Pub-Sub Temps Réel"
MAIN_CLASS_DEMO="fr.telecom.middleware.examples.CriticalSystemDemo"
MAIN_CLASS_BENCHMARK="fr.telecom.middleware.test.PerformanceBenchmark"

# Couleurs pour le terminal
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
MAGENTA='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color
BOLD='\033[1m'
UNDERLINE='\033[4m'

# Fonctions d'affichage
print_header() {
    echo -e "${BLUE}${BOLD}"
    echo "╔══════════════════════════════════════════════════════════╗"
    echo "║                                                          ║"
    echo "║  ${PROJECT_NAME}          ║"
    echo "║  ${UNDERLINE}Pour Systèmes Embarqués Critiques${NC}${BLUE}${BOLD}                ║"
    echo "║                                                          ║"
    echo "╚══════════════════════════════════════════════════════════╝"
    echo -e "${NC}"
}

print_info() {
    echo -e "${CYAN}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCÈS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[ATTENTION]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERREUR]${NC} $1"
}

print_step() {
    echo -e "${MAGENTA}${BOLD}▶ $1${NC}"
}

# Vérification des prérequis
check_prerequisites() {
    print_step "Vérification des prérequis"

    # Vérifier Maven
    if ! command -v mvn &> /dev/null; then
        print_error "Maven n'est pas installé ou n'est pas dans le PATH"
        echo "Pour installer Maven:"
        echo "  Ubuntu/Debian: sudo apt install maven"
        echo "  CentOS/RHEL:   sudo yum install maven"
        echo "  macOS:         brew install maven"
        echo "  Windows:       Téléchargez depuis https://maven.apache.org"
        exit 1
    fi

    print_info "Maven version: $(mvn -version | grep 'Apache Maven' | cut -d' ' -f3)"

    # Vérifier Java
    if ! command -v java &> /dev/null; then
        print_error "Java n'est pas installé ou n'est pas dans le PATH"
        echo "Java 11+ est requis"
        echo "Téléchargez depuis: https://openjdk.org/"
        exit 1
    fi

    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
    print_info "Java version: $JAVA_VERSION"

    # Vérifier la version Java
    if [[ ! "$JAVA_VERSION" =~ ^11\. ]]; then
        print_warning "Java $JAVA_VERSION détecté. Java 11+ est recommandé"
    fi

    print_success "Tous les prérequis sont satisfaits"
}

# Nettoyage du projet
clean_project() {
    print_step "Nettoyage du projet"
    mvn clean
    if [ $? -eq 0 ]; then
        print_success "Nettoyage terminé"
    else
        print_error "Échec du nettoyage"
        exit 1
    fi
}

# Compilation du projet
compile_project() {
    print_step "Compilation du projet"
    echo "Cette opération peut prendre quelques instants..."
    mvn compile
    if [ $? -eq 0 ]; then
        print_success "Compilation terminée avec succès"
    else
        print_error "Échec de la compilation"
        exit 1
    fi
}

# Exécution des tests
run_tests() {
    print_step "Exécution des tests"
    echo "Exécution de la suite de tests complète..."
    mvn test
    if [ $? -eq 0 ]; then
        print_success "Tous les tests ont réussi"
    else
        print_error "Certains tests ont échoué"
    fi
}

# Lancement de la démonstration
run_demo() {
    print_step "Lancement de la démonstration"
    echo "Démarrage de la simulation système critique..."
    echo "Durée: 15 secondes"
    echo -e "${YELLOW}Appuyez sur Ctrl+C pour arrêter prématurément${NC}"
    echo

    mvn exec:java -Dexec.mainClass="$MAIN_CLASS_DEMO" -q
    if [ $? -eq 0 ]; then
        print_success "Démonstration terminée avec succès"
    else
        print_error "La démonstration a rencontré une erreur"
    fi
}

# Exécution des benchmarks
run_benchmarks() {
    print_step "Exécution des benchmarks de performance"
    echo "Cette opération peut prendre plusieurs minutes..."
    echo "Benchmarks exécutés:"
    echo "  ✓ Latence de publication"
    echo "  ✓ Débit (throughput)"
    echo "  ✓ Utilisation mémoire"
    echo "  ✓ Accès concurrent"
    echo "  ✓ Deadlines temps-réel"

    mvn exec:java -Dexec.mainClass="$MAIN_CLASS_BENCHMARK" -q
    if [ $? -eq 0 ]; then
        print_success "Benchmarks terminés"
    else
        print_error "Les benchmarks ont rencontré une erreur"
    fi
}

# Exécution complète
run_all() {
    print_step "Exécution complète du pipeline"
    echo "Ceci va exécuter toutes les étapes:"
    echo "  1. Nettoyage"
    echo "  2. Compilation"
    echo "  3. Tests"
    echo "  4. Démonstration"

    read -p "Continuer ? (o/n): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Oo]$ ]]; then
        print_info "Annulé par l'utilisateur"
        return
    fi

    clean_project
    compile_project
    run_tests
    run_demo

    print_success "Pipeline complet exécuté avec succès"
}

# Affichage du menu
show_menu() {
    clear
    print_header

    echo -e "${BOLD}${CYAN}MENU PRINCIPAL${NC}"
    echo -e "${BLUE}══════════════════════════════════════════════════════════${NC}"
    echo
    echo -e "  ${GREEN}1.${NC} ${BOLD}Compiler le projet${NC}"
    echo -e "     Compilation complète avec Maven"
    echo
    echo -e "  ${GREEN}2.${NC} ${BOLD}Exécuter les tests${NC}"
    echo -e "     Tests unitaires et d'intégration"
    echo
    echo -e "  ${GREEN}3.${NC} ${BOLD}Lancer la démonstration${NC}"
    echo -e "     Simulation système critique (15 secondes)"
    echo
    echo -e "  ${GREEN}4.${NC} ${BOLD}Exécuter les benchmarks${NC}"
    echo -e "     Tests de performance complets"
    echo
    echo -e "  ${GREEN}5.${NC} ${BOLD}Tout exécuter${NC}"
    echo -e "     Clean + Compile + Tests + Démo"
    echo
    echo -e "  ${GREEN}6.${NC} ${BOLD}Options avancées${NC}"
    echo -e "     Profils Maven et options spéciales"
    echo
    echo -e "  ${GREEN}7.${NC} ${BOLD}Informations système${NC}"
    echo -e "     Afficher la configuration"
    echo
    echo -e "  ${RED}8.${NC} ${BOLD}Quitter${NC}"
    echo
    echo -e "${BLUE}══════════════════════════════════════════════════════════${NC}"
    echo
}

# Menu options avancées
show_advanced_menu() {
    clear
    print_header

    echo -e "${BOLD}${YELLOW}OPTIONS AVANCÉES${NC}"
    echo -e "${BLUE}══════════════════════════════════════════════════════════${NC}"
    echo
    echo -e "  ${GREEN}1.${NC} ${BOLD}Compiler avec optimisation${NC}"
    echo -e "     mvn compile -Poptimized"
    echo
    echo -e "  ${GREEN}2.${NC} ${BOLD}Exécuter sans tests${NC}"
    echo -e "     mvn package -Pskip-tests"
    echo
    echo -e "  ${GREEN}3.${NC} ${BOLD}Générer la documentation${NC}"
    echo -e "     mvn javadoc:javadoc"
    echo
    echo -e "  ${GREEN}4.${NC} ${BOLD}Créer le JAR exécutable${NC}"
    echo -e "     mvn package"
    echo
    echo -e "  ${GREEN}5.${NC} ${BOLD}Retour au menu principal${NC}"
    echo
    echo -e "${BLUE}══════════════════════════════════════════════════════════${NC}"
    echo
}

# Informations système
show_system_info() {
    clear
    print_header

    echo -e "${BOLD}${CYAN}INFORMATIONS SYSTÈME${NC}"
    echo -e "${BLUE}══════════════════════════════════════════════════════════${NC}"
    echo

    # Informations générales
    echo -e "${BOLD}● Informations générales:${NC}"
    echo "  Script:      $SCRIPT_NAME v$VERSION"
    echo "  Date:        $(date)"
    echo "  Répertoire:  $(pwd)"
    echo

    # Informations Java
    echo -e "${BOLD}● Environnement Java:${NC}"
    echo "  Java Home:   $JAVA_HOME"
    echo "  Java Version: $(java -version 2>&1 | head -n 1)"
    echo "  Java Vendor:  $(java -version 2>&1 | grep 'Runtime' | cut -d' ' -f1)"
    echo

    # Informations Maven
    echo -e "${BOLD}● Environnement Maven:${NC}"
    echo "  Maven Home:  $MAVEN_HOME"
    echo "  Maven Version: $(mvn -version | grep 'Apache Maven' | cut -d' ' -f3)"
    echo

    # Informations système
    echo -e "${BOLD}● Système d'exploitation:${NC}"
    echo "  OS:          $(uname -s)"
    echo "  Version:     $(uname -r)"
    echo "  Architecture: $(uname -m)"
    echo

    # Informations projet
    echo -e "${BOLD}● Informations projet:${NC}"
    if [ -f "pom.xml" ]; then
        echo "  ArtifactId:  $(grep -oP '(?<=<artifactId>)[^<]+' pom.xml | head -1)"
        echo "  Version:     $(grep -oP '(?<=<version>)[^<]+' pom.xml | head -1)"
        echo "  Packaging:   $(grep -oP '(?<=<packaging>)[^<]+' pom.xml | head -1)"
    else
        echo "  pom.xml non trouvé"
    fi
    echo

    # Vérification de l'état
    echo -e "${BOLD}● État du projet:${NC}"
    if [ -d "target/classes" ]; then
        echo -e "  Compilation: ${GREEN}✓${NC} Prête"
    else
        echo -e "  Compilation: ${RED}✗${NC} Non compilé"
    fi

    if [ -d "target/test-classes" ]; then
        echo -e "  Tests:       ${GREEN}✓${NC} Compilés"
    else
        echo -e "  Tests:       ${YELLOW}⚠${NC} Non compilés"
    fi

    echo
    echo -e "${BLUE}══════════════════════════════════════════════════════════${NC}"
    echo
    read -p "Appuyez sur Entrée pour continuer..."
}

# Fonction principale
main() {
    # Vérifier les prérequis
    check_prerequisites

    while true; do
        show_menu
        read -p "Votre choix (1-8): " choice

        case $choice in
            1)
                clean_project
                compile_project
                ;;
            2)
                run_tests
                ;;
            3)
                run_demo
                ;;
            4)
                run_benchmarks
                ;;
            5)
                run_all
                ;;
            6)
                while true; do
                    show_advanced_menu
                    read -p "Votre choix (1-5): " adv_choice

                    case $adv_choice in
                        1)
                            print_step "Compilation avec optimisation"
                            mvn compile -Poptimized
                            if [ $? -eq 0 ]; then
                                print_success "Compilation optimisée terminée"
                            fi
                            ;;
                        2)
                            print_step "Packaging sans tests"
                            mvn package -Pskip-tests
                            if [ $? -eq 0 ]; then
                                print_success "JAR créé avec succès"
                            fi
                            ;;
                        3)
                            print_step "Génération de la documentation"
                            mvn javadoc:javadoc
                            if [ $? -eq 0 ]; then
                                print_success "Documentation générée dans target/site/apidocs"
                            fi
                            ;;
                        4)
                            print_step "Création du JAR exécutable"
                            mvn package
                            if [ $? -eq 0 ]; then
                                print_success "JAR créé dans target/"
                            fi
                            ;;
                        5)
                            break
                            ;;
                        *)
                            print_error "Choix invalide!"
                            ;;
                    esac

                    echo
                    read -p "Appuyez sur Entrée pour continuer..."
                done
                ;;
            7)
                show_system_info
                ;;
            8)
                echo
                print_success "Au revoir !"
                exit 0
                ;;
            *)
                print_error "Choix invalide! Veuillez saisir un nombre entre 1 et 8."
                ;;
        esac

        echo
        echo -e "${BLUE}══════════════════════════════════════════════════════════${NC}"
        echo
        read -p "Appuyez sur Entrée pour revenir au menu..."
    done
}

# Point d'entrée
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi

## **30. 📄 docs/deployment.md**

```markdown
# Guide de Déploiement

## Environnements Supportés

### 1. Développement (PC/Mac)
- Java 11+
- Maven 3.6+
- 512MB RAM minimum
- Pas de hardware spécifique requis

### 2. Embarqué Léger (Raspberry Pi)
- Raspberry Pi 3/4
- Java 11 ARM
- 1GB RAM recommandé
- Stockage: 2GB minimum

### 3. Systèmes Critiques
- Cartes temps-réel (Xilinx, NXP)
- JVM temps-réel (IBM, Aicas)
- Mémoire ECC
- Redondance hardware

## Installation

### 1. Prérequis
```bash
# Vérifier Java
java -version  # Doit être >= 11

# Vérifier Maven
mvn -version   # Doit être >= 3.6
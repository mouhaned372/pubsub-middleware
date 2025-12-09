# Architecture du Middleware Pub-Sub Temps-Réel

## Vue d'ensemble

### Objectifs
- Fournir un middleware léger pour systèmes embarqués critiques
- Garantir des performances temps-réel avec des deadlines
- Maintenir un footprint mémoire minimal (< 500KB)
- Offrir une tolérance aux pannes intégrée

## Architecture Technique

### Composants Principaux

#### 1. Core
- **Middleware** : Point d'entrée principal, orchestre tous les composants
- **Topic** : Canal de communication avec QoS spécifique
- **Subscriber** : Interface pour recevoir des messages
- **Publisher** : Interface pour publier des messages

#### 2. Qualité de Service (QoS)
- **QoS** : Configuration des garanties (fiabilité, priorité, deadlines)
- **Deadline** : Représentation d'une contrainte temporelle
- **RedundancyManager** : Gestion de la redondance pour tolérance aux pannes

#### 3. Temps-Réel
- **DeadlineMonitor** : Surveillance des deadlines
- **Scheduler** : Planificateur pour tâches temps-réel

#### 4. Tolérance aux Pannes
- **FaultDetector** : Détection de pannes des subscribers
- **RecoveryManager** : Récupération après pannes

#### 5. API
- **MiddlewareAPI** : API simplifiée pour les utilisateurs
- **Message** : Structure des messages échangés

## Flux de Données

### Publication d'un Message
1. L'application crée un Message
2. Appel à `Middleware.publish()`
3. Vérification mémoire et deadlines
4. Distribution aux subscribers via le Topic
5. Gestion de la redondance si configuré

### Réception d'un Message
1. Le Topic reçoit le message
2. Application de la QoS (fiabilité, retry, etc.)
3. Appel à `Subscriber.onMessage()`
4. Surveillance santé du subscriber

## Garanties Temps-Réel

### Deadlines
- Configuration par topic (0-1000ms)
- Surveillance continue par DeadlineMonitor
- Notification en cas de dépassement
- Statistiques de performance

### Priorités
- 4 niveaux : LOW, MEDIUM, HIGH, CRITICAL
- Impact sur l'ordonnancement
- Gestion des ressources

## Gestion Mémoire

### Stratégies
- Limite mémoire configurable
- Nettoyage automatique des anciens messages
- Garbage collection manuel si nécessaire
- Buffer circulaire pour l'historique

### Optimisations
- Utilisation de structures concurrentes
- Pool de threads limité
- Réutilisation d'objets
- Allocation mémoire prévisible

## Tolérance aux Pannes

### Détection
- Heartbeat des subscribers
- Timeout configurable
- Compteur d'échecs
- Notification de pannes

### Récupération
- Buffer de messages pour récupération
- Republier les messages perdus
- Cooldown entre récupérations
- Statistiques de récupération

## Performances

### Métriques Clés
- Latence de publication : < 1ms
- Latence de distribution : < 10ms (95th percentile)
- Débit : > 1000 msg/s
- Mémoire : 200-500KB

### Worst-Case Execution Time (WCET)
- Publication : 200µs max
- Distribution : 5ms max
- Vérification deadline : 10µs max

## Sécurité (Extensions Possibles)
- Chiffrement des messages
- Authentification des publishers/subscribers
- Contrôle d'accès par topic
- Audit des opérations

## Évolutivité
- Architecture modulaire
- API extensible
- Support multi-thread
- Configuration dynamique
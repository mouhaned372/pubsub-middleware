package fr.telecom.middleware.examples;

import fr.telecom.middleware.core.*;
import fr.telecom.middleware.qos.*;
import fr.telecom.middleware.api.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Démonstration complète du système pour applications critiques.
 */
public class CriticalSystemDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=".repeat(60));
        System.out.println("🚀 DÉMONSTRATION MIDDLEWARE PUB-SUB TEMPS-RÉEL");
        System.out.println("   Pour Systèmes Embarqués Critiques");
        System.out.println("=".repeat(60));
        System.out.println();

        // 1. Initialisation du middleware
        System.out.println("1. 📦 Initialisation du Middleware");
        System.out.println("   ".repeat(15));

        Middleware middleware = new Middleware();
        middleware.setMaxMemoryKB(512); // Limite pour systèmes embarqués
        middleware.setRealtimeEnabled(true);

        System.out.println("   ✅ Middleware initialisé");
        System.out.println("   Mémoire maximale: 512KB");
        System.out.println("   Mode temps-réel: activé");
        System.out.println();

        // 2. Création des topics avec différents QoS
        System.out.println("2. 🏗️  Création des Topics avec QoS");
        System.out.println("   ".repeat(15));

        // QoS pour données critiques (moteur, freins, etc.)
        QoS criticalQoS = new QoS.Builder()
                .reliability(QoS.Reliability.EXACTLY_ONCE)
                .priority(QoS.Priority.CRITICAL)
                .deadlineMs(20) // 20ms max pour données critiques
                .maxLatencyMs(50)
                .redundancyLevel(2) // Double redondance
                .build();

        // QoS pour télémétrie normale
        QoS telemetryQoS = new QoS.Builder()
                .reliability(QoS.Reliability.AT_LEAST_ONCE)
                .priority(QoS.Priority.HIGH)
                .deadlineMs(100)
                .maxLatencyMs(200)
                .build();

        // QoS pour logging/info
        QoS loggingQoS = new QoS.Builder()
                .reliability(QoS.Reliability.BEST_EFFORT)
                .priority(QoS.Priority.LOW)
                .build();

        Topic engineTopic = middleware.createTopic("vehicle/engine/critical", criticalQoS);
        Topic telemetryTopic = middleware.createTopic("vehicle/telemetry", telemetryQoS);
        Topic loggingTopic = middleware.createTopic("system/logging", loggingQoS);

        System.out.println("   ✅ Topics créés:");
        System.out.printf("   - %s: %s%n", engineTopic.getName(), engineTopic.getQoS());
        System.out.printf("   - %s: %s%n", telemetryTopic.getName(), telemetryTopic.getQoS());
        System.out.printf("   - %s: %s%n", loggingTopic.getName(), loggingTopic.getQoS());
        System.out.println();

        // 3. Création et abonnement des subscribers
        System.out.println("3. 👥 Création des Subscribers");
        System.out.println("   ".repeat(15));

        SubscriberExample engineMonitor = new SubscriberExample("Engine-Monitor");
        SubscriberExample telemetryProcessor = new SubscriberExample("Telemetry-Processor");
        SubscriberExample dataLogger = new SubscriberExample("Data-Logger");
        SubscriberExample dashboard = new SubscriberExample("Dashboard");

        middleware.subscribe("vehicle/engine/critical", engineMonitor);
        middleware.subscribe("vehicle/telemetry", telemetryProcessor);
        middleware.subscribe("vehicle/telemetry", dataLogger);
        middleware.subscribe("vehicle/telemetry", dashboard);
        middleware.subscribe("system/logging", dataLogger);

        System.out.println("   ✅ Subscribers créés et abonnés");
        System.out.println("   Engine-Monitor → vehicle/engine/critical");
        System.out.println("   Telemetry-Processor → vehicle/telemetry");
        System.out.println("   Data-Logger → vehicle/telemetry, system/logging");
        System.out.println("   Dashboard → vehicle/telemetry");
        System.out.println();

        // 4. Création des publishers
        System.out.println("4. 📡 Création des Publishers");
        System.out.println("   ".repeat(15));

        PublisherExample enginePublisher = new PublisherExample(middleware, "vehicle/engine/critical");
        PublisherExample telemetryPublisher = new PublisherExample(middleware, "vehicle/telemetry");
        PublisherExample loggingPublisher = new PublisherExample(middleware, "system/logging");

        System.out.println("   ✅ Publishers créés");
        System.out.println();

        // 5. Simulation de données
        System.out.println("5. 🔧 Simulation de Données en Temps Réel");
        System.out.println("   ".repeat(15));
        System.out.println("   Démarrage dans 2 secondes...");
        System.out.println();

        Thread.sleep(2000);

        // Planificateur pour la simulation
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);

        // 5.1 Données critiques du moteur (toutes les 50ms)
        System.out.println("   🚗 Données critiques moteur (50ms):");
        scheduler.scheduleAtFixedRate(() -> {
            double oilPressure = 80 + (Math.random() * 40); // 80-120 psi
            double coolantTemp = 85 + (Math.random() * 20); // 85-105°C

            enginePublisher.publishSensorData("oil_pressure", oilPressure, "psi");
            enginePublisher.publishSensorData("coolant_temp", coolantTemp, "°C");

            // Alerte occasionnelle
            if (Math.random() < 0.05) { // 5% de chance
                enginePublisher.publishAlert(
                        "ENGINE_FAULT",
                        "Pression d'huile anormale détectée",
                        4
                );
            }
        }, 0, 50, TimeUnit.MILLISECONDS);

        // 5.2 Données de télémétrie (toutes les 200ms)
        System.out.println("   📊 Données de télémétrie (200ms):");
        scheduler.scheduleAtFixedRate(() -> {
            double speed = 60 + (Math.random() * 40); // 60-100 km/h
            double rpm = 2000 + (Math.random() * 1500); // 2000-3500 RPM
            double fuelLevel = 30 + (Math.random() * 50); // 30-80%

            telemetryPublisher.publishSensorData("speed", speed, "km/h");
            telemetryPublisher.publishSensorData("rpm", rpm, "RPM");
            telemetryPublisher.publishSensorData("fuel_level", fuelLevel, "%");

            // Message d'info
            if (Math.random() < 0.1) { // 10% de chance
                telemetryPublisher.publishTestMessage(
                        String.format("Véhicule en fonctionnement normal. Vitesse: %.0f km/h", speed)
                );
            }
        }, 0, 200, TimeUnit.MILLISECONDS);

        // 5.3 Logging système (toutes les 500ms)
        System.out.println("   📝 Logging système (500ms):");
        scheduler.scheduleAtFixedRate(() -> {
            String[] logMessages = {
                    "Système de communication actif",
                    "Vérification des capteurs OK",
                    "Mise à jour des paramètres",
                    "Sauvegarde des données",
                    "Rapport de diagnostic généré"
            };

            String randomLog = logMessages[(int) (Math.random() * logMessages.length)];
            loggingPublisher.publishTestMessage("[LOG] " + randomLog);
        }, 0, 500, TimeUnit.MILLISECONDS);

        System.out.println();
        System.out.println("   ⏱️  Simulation en cours pendant 15 secondes...");
        System.out.println("   ".repeat(15));

        // 6. Exécution pendant 15 secondes
        Thread.sleep(15000);

        // 7. Arrêt propre
        System.out.println();
        System.out.println("6. 🛑 Arrêt de la Simulation");
        System.out.println("   ".repeat(15));

        scheduler.shutdown();
        scheduler.awaitTermination(2, TimeUnit.SECONDS);

        middleware.shutdown();

        // 8. Affichage des statistiques
        System.out.println();
        System.out.println("📊 STATISTIQUES FINALES");
        System.out.println("=".repeat(40));

        System.out.printf("Topics actifs: %d%n", middleware.getAllTopics().size());

        for (Topic topic : middleware.getAllTopics()) {
            System.out.printf("%nTopic: %s%n", topic.getName());
            System.out.printf("  Messages publiés: %d%n", topic.getMessageCount());
            System.out.printf("  Subscribers: %d%n", topic.getSubscribers().size());
            System.out.printf("  Dernière publication: %tT%n", topic.getLastPublishTime());
        }

        System.out.println();
        System.out.println("📈 Statistiques Subscribers:");
        engineMonitor.printStats();
        telemetryProcessor.printStats();
        dataLogger.printStats();
        dashboard.printStats();

        System.out.println();
        System.out.println("✅ DÉMONSTRATION TERMINÉE AVEC SUCCÈS!");
        System.out.println("=".repeat(60));

        // Nettoyage final
        System.gc();
    }

    /**
     * Méthode utilitaire pour formater le temps.
     */
    private static String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
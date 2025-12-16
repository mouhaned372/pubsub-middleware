package fr.telecom.middleware.examples;

import fr.telecom.middleware.core.*;
import fr.telecom.middleware.qos.*;
import fr.telecom.middleware.api.Message;

/**
 * Démonstration avec dashboard WebSocket.
 */
public class DashboardDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=".repeat(60));
        System.out.println("   Middleware Pub-Sub Temps Réel");
        System.out.println("=".repeat(60));
        System.out.println();

        // 1. Créer le middleware
        System.out.println("1. 📊 Initialisation du Middleware");
        Middleware middleware = new Middleware();
        middleware.setMaxMemoryKB(512);
        middleware.setRealtimeEnabled(true);

        System.out.println("   ✅ Middleware initialisé");
        System.out.println();

        // 2. Créer des topics
        System.out.println("2. 🏗️  Création des Topics");

        QoS criticalQoS = new QoS.Builder()
                .reliability(QoS.Reliability.EXACTLY_ONCE)
                .priority(QoS.Priority.CRITICAL)
                .deadlineMs(50)
                .build();

        QoS normalQoS = new QoS.Builder()
                .reliability(QoS.Reliability.AT_LEAST_ONCE)
                .priority(QoS.Priority.HIGH)
                .deadlineMs(200)
                .build();

        middleware.createTopic("vehicle/engine", criticalQoS);
        middleware.createTopic("vehicle/sensors", normalQoS);
        middleware.createTopic("system/logs", new QoS.Builder()
                .reliability(QoS.Reliability.BEST_EFFORT)
                .priority(QoS.Priority.LOW)
                .build());

        System.out.println("   ✅ Topics créés");
        System.out.println();

        // 3. Ajouter des subscribers
        System.out.println("3. 👥 Configuration des Subscribers");

        middleware.subscribe("vehicle/engine", new Subscriber() {
            @Override
            public void onMessage(Message message) {
                message.markAsReceived();
                System.out.println("[Engine] Message reçu: " + message.getPayload());
            }

            @Override
            public String getSubscriberId() {
                return "EngineController";
            }
        });

        middleware.subscribe("vehicle/sensors", new Subscriber() {
            @Override
            public void onMessage(Message message) {
                message.markAsReceived();
                System.out.println("[Sensors] Donnée: " + message.getPayload());
            }

            @Override
            public String getSubscriberId() {
                return "SensorProcessor";
            }
        });

        System.out.println("   ✅ Subscribers configurés");
        System.out.println();

        // 4. Démarrer la simulation
        System.out.println("4. 🔄 Démarrage de la Simulation");
        System.out.println("   La simulation va durer 60 secondes");
        System.out.println("   Messages envoyés dans la console...");
        System.out.println();

        // Simulation simple
        for (int i = 0; i < 60; i++) {
            // Message moteur
            Message engineMsg = Message.builder("vehicle/engine")
                    .payload("RPM: " + (2000 + Math.random() * 1500))
                    .header("timestamp", System.currentTimeMillis())
                    .build();
            middleware.publish("vehicle/engine", engineMsg);

            // Message capteurs
            Message sensorMsg = Message.builder("vehicle/sensors")
                    .payload("Temp: " + (20 + Math.random() * 30) + "°C")
                    .header("timestamp", System.currentTimeMillis())
                    .build();
            middleware.publish("vehicle/sensors", sensorMsg);

            // Log toutes les 5 secondes
            if (i % 5 == 0) {
                Message logMsg = Message.builder("system/logs")
                        .payload("Système opérationnel - " + i + "s")
                        .header("level", "INFO")
                        .build();
                middleware.publish("system/logs", logMsg);
            }

            Thread.sleep(1000); // 1 seconde entre les messages

            // Afficher la progression
            System.out.print("\r⏱️  Progression: " + (i + 1) + "/60 secondes");
        }

        System.out.println();
        System.out.println();

        // 5. Arrêt propre
        System.out.println("5. 🛑 Arrêt de la Simulation");
        middleware.shutdown();

        System.out.println();
        System.out.println("✅ Démonstration terminée!");
        System.out.println("=".repeat(60));
    }
}
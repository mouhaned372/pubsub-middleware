package examples;

import fr.telecom.middleware.core.*;
import fr.telecom.middleware.qos.*;
import fr.telecom.middleware.api.Message;

/**
 * Démonstration simple et rapide du middleware.
 * Parfait pour tester rapidement sans configuration complexe.
 */
public class SimpleDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=".repeat(50));
        System.out.println("🚀 DÉMONSTRATION SIMPLE - Middleware Pub-Sub");
        System.out.println("   Pour systèmes embarqués critiques");
        System.out.println("=".repeat(50));
        System.out.println();

        // 1. Initialisation du middleware
        System.out.println("1. 📦 INITIALISATION DU MIDDLEWARE");
        System.out.println("   ".repeat(20));

        Middleware middleware = new Middleware();
        middleware.setMaxMemoryKB(256); // Limite mémoire pour embarqué
        middleware.setRealtimeEnabled(true);

        System.out.println("   ✅ Middleware initialisé");
        System.out.println("   Mémoire max: 256KB");
        System.out.println("   Mode temps-réel: activé");
        System.out.println();

        // 2. Création de topics simples
        System.out.println("2. 🏗️  CRÉATION DES TOPICS");
        System.out.println("   ".repeat(20));

        // Topic pour données critiques
        QoS criticalQoS = new QoS.Builder()
                .reliability(QoS.Reliability.EXACTLY_ONCE)
                .priority(QoS.Priority.CRITICAL)
                .deadlineMs(50)
                .build();

        // Topic pour données normales
        QoS normalQoS = new QoS.Builder()
                .reliability(QoS.Reliability.AT_LEAST_ONCE)
                .priority(QoS.Priority.MEDIUM)
                .build();

        Topic criticalTopic = middleware.createTopic("system/critical", criticalQoS);
        Topic normalTopic = middleware.createTopic("sensors/data", normalQoS);

        System.out.println("   ✅ Topics créés:");
        System.out.printf("   - %s (critique, deadline 50ms)%n", criticalTopic.getName());
        System.out.printf("   - %s (normal, AT_LEAST_ONCE)%n", normalTopic.getName());
        System.out.println();

        // 3. Création des subscribers
        System.out.println("3. 👥 CRÉATION DES SUBSCRIBERS");
        System.out.println("   ".repeat(20));

        // Subscriber pour données critiques
        Subscriber criticalSubscriber = new Subscriber() {
            private int messageCount = 0;

            @Override
            public void onMessage(Message message) {
                messageCount++;
                System.out.printf("   🔴 [CRITIQUE] Message #%d: %s (âge: %dms)%n",
                        messageCount, message.getPayload(), message.getAgeMs());

                // Vérification de la deadline (pour démo)
                if (message.getAgeMs() > 50) {
                    System.out.println("      ⚠️  ALERTE: Deadline potentiellement dépassée!");
                }
            }

            @Override
            public String getSubscriberId() {
                return "Critical-Monitor";
            }
        };

        // Subscriber pour données normales
        Subscriber normalSubscriber = new Subscriber() {
            private int messageCount = 0;

            @Override
            public void onMessage(Message message) {
                messageCount++;
                System.out.printf("   🔵 [NORMAL] Message #%d: %s%n",
                        messageCount, message.getPayload());
            }

            @Override
            public String getSubscriberId() {
                return "Data-Processor";
            }
        };

        // Subscriber pour logging
        Subscriber loggingSubscriber = new Subscriber() {
            @Override
            public void onMessage(Message message) {
                System.out.printf("   📝 [LOG] Topic: %s, Message: %s%n",
                        message.getTopic(), message.getId().substring(0, 8));
            }

            @Override
            public String getSubscriberId() {
                return "System-Logger";
            }
        };

        // 4. Abonnement aux topics
        System.out.println("4. 📡 ABONNEMENT AUX TOPICS");
        System.out.println("   ".repeat(20));

        middleware.subscribe("system/critical", criticalSubscriber);
        middleware.subscribe("sensors/data", normalSubscriber);
        middleware.subscribe("sensors/data", loggingSubscriber); // Multiple subscribers
        middleware.subscribe("system/critical", loggingSubscriber);

        System.out.println("   ✅ Subscribers abonnés:");
        System.out.println("   Critical-Monitor → system/critical");
        System.out.println("   Data-Processor → sensors/data");
        System.out.println("   System-Logger → system/critical, sensors/data");
        System.out.println();

        // 5. Simulation de publication
        System.out.println("5. 📤 SIMULATION DE PUBLICATION");
        System.out.println("   ".repeat(20));
        System.out.println("   Démarrage dans 2 secondes...");
        Thread.sleep(2000);
        System.out.println();

        // Messages critiques
        System.out.println("   📊 Publication de messages critiques:");
        for (int i = 1; i <= 3; i++) {
            Message criticalMsg = new Message("system/critical",
                    String.format("Alerte système #%d - Température critique: %.1f°C",
                            i, 85.0 + Math.random() * 10));

            criticalMsg.addHeader("source", "sensor-temp-001");
            criticalMsg.addHeader("severity", "HIGH");
            criticalMsg.addHeader("timestamp", System.currentTimeMillis());

            middleware.publish("system/critical", criticalMsg);
            Thread.sleep(100); // Petite pause entre les messages
        }

        // Messages normaux
        System.out.println("\n   📊 Publication de messages normaux:");
        String[] sensorTypes = {"température", "pression", "humidité", "vibration"};

        for (int i = 1; i <= 5; i++) {
            String sensorType = sensorTypes[(i - 1) % sensorTypes.length];
            double value = 20 + Math.random() * 60;
            String unit = getUnitForSensor(sensorType);

            Message normalMsg = new Message("sensors/data",
                    String.format("%s: %.1f %s", sensorType, value, unit));

            normalMsg.addHeader("sensor_id", "sensor-" + i);
            normalMsg.addHeader("location", "zone-" + ((i % 3) + 1));
            normalMsg.addHeader("battery", 85 - (i * 2) + "%");

            middleware.publish("sensors/data", normalMsg);
            Thread.sleep(200); // Pause un peu plus longue
        }

        // Message de test avec payload complexe
        System.out.println("\n   🧪 Publication d'un message avec payload complexe:");
        SensorData complexData = new SensorData("multi-sensor",
                25.5, 1013.2, 65.8, "N45.1234,E2.5678");

        Message complexMsg = new Message("sensors/data", complexData);
        complexMsg.addHeader("sensor_model", "MS-5000");
        complexMsg.addHeader("firmware", "v2.1.5");

        middleware.publish("sensors/data", complexMsg);

        // 6. Attente pour la réception de tous les messages
        System.out.println("\n   ⏳ Attente de la réception des messages...");
        Thread.sleep(1000);
        System.out.println();

        // 7. Affichage des statistiques
        System.out.println("6. 📈 STATISTIQUES FINALES");
        System.out.println("   ".repeat(20));

        System.out.printf("   Topics actifs: %d%n",
                middleware.getAllTopics().size());

        for (Topic topic : middleware.getAllTopics()) {
            System.out.printf("   📍 Topic: %s%n", topic.getName());
            System.out.printf("      Messages publiés: %d%n", topic.getMessageCount());
            System.out.printf("      Subscribers: %d%n", topic.getSubscribers().size());

            if (topic.getMessageHistory().size() > 0) {
                System.out.printf("      Dernier message: il y a %dms%n",
                        topic.getMessageHistory().peek().getAgeMs());
            }
        }

        // 8. Test de mémoire
        System.out.println("\n   💾 Test d'utilisation mémoire:");
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024;
        System.out.printf("      Mémoire utilisée: %d KB%n", usedMemory);
        System.out.printf("      Limite configurée: %d KB%n", middleware.getMaxMemoryKB());

        if (usedMemory > middleware.getMaxMemoryKB() * 0.8) {
            System.out.println("      ⚠️  Mémoire proche de la limite!");
        } else {
            System.out.println("      ✅ Mémoire dans les limites");
        }

        // 9. Arrêt propre
        System.out.println("\n7. 🛑 ARRÊT PROPRE DU SYSTÈME");
        System.out.println("   ".repeat(20));

        middleware.shutdown();

        System.out.println("   ✅ Middleware arrêté proprement");
        System.out.println();
        System.out.println("🎉 DÉMONSTRATION TERMINÉE AVEC SUCCÈS!");
        System.out.println("=".repeat(50));
    }

    /**
     * Retourne l'unité appropriée pour un type de capteur.
     */
    private static String getUnitForSensor(String sensorType) {
        switch (sensorType.toLowerCase()) {
            case "température":
                return "°C";
            case "pression":
                return "hPa";
            case "humidité":
                return "%";
            case "vibration":
                return "g";
            default:
                return "unités";
        }
    }

    /**
     * Classe pour données de capteur complexes.
     */
    public static class SensorData {
        private final String type;
        private final double temperature;
        private final double pressure;
        private final double humidity;
        private final String location;
        private final long timestamp;

        public SensorData(String type, double temperature, double pressure,
                          double humidity, String location) {
            this.type = type;
            this.temperature = temperature;
            this.pressure = pressure;
            this.humidity = humidity;
            this.location = location;
            this.timestamp = System.currentTimeMillis();
        }

        // Getters
        public String getType() { return type; }
        public double getTemperature() { return temperature; }
        public double getPressure() { return pressure; }
        public double getHumidity() { return humidity; }
        public String getLocation() { return location; }
        public long getTimestamp() { return timestamp; }

        @Override
        public String toString() {
            return String.format(
                    "SensorData{type='%s', temp=%.1f°C, pressure=%.1fhPa, humidity=%.1f%%, location='%s'}",
                    type, temperature, pressure, humidity, location
            );
        }
    }
}
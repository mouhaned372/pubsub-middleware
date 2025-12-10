import fr.telecom.middleware.core.Middleware;
import fr.telecom.middleware.core.Topic;
import fr.telecom.middleware.core.Subscriber;
import fr.telecom.middleware.qos.QoS;
import fr.telecom.middleware.api.Message;

public class MonApplication {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("🚀 Démarrage de l'application middleware...");

        // 1. Créer une instance du middleware
        Middleware middleware = new Middleware();

        // 2. Configurer les paramètres système
        middleware.setMaxMemoryKB(512);      // Limite mémoire à 512KB
        middleware.setRealtimeEnabled(true); // Activer le mode temps-réel

        System.out.println("✅ Middleware configuré (mémoire: " + middleware.getMaxMemoryKB() + "KB)");

        // 3. Créer un topic avec QoS
        QoS telemetryQoS = new QoS.Builder()
                .reliability(QoS.Reliability.AT_LEAST_ONCE)
                .priority(QoS.Priority.HIGH)
                .deadlineMs(100)      // Deadline de 100ms
                .maxLatencyMs(200)    // Latence maximale de 200ms
                .redundancyLevel(0)   // Une copie redondante
                .build();

        Topic telemetryTopic = middleware.createTopic("system/telemetry", telemetryQoS);
        System.out.println("✅ Topic créé: " + telemetryTopic.getName());

        // 4. Créer et abonner un subscriber
        middleware.subscribe("system/telemetry", new Subscriber() {
            @Override
            public void onMessage(Message message) {
                // IMPORTANT: Marquer le message comme reçu pour le calcul d'âge
                message.markAsReceived();

                System.out.println("\n📨 Télémétrie reçue: " + message.getPayload());
                System.out.println("   ID Message: " + message.getId().substring(0, 8));
                System.out.println("   Âge: " + message.getAgeMs() + "ms");
                System.out.println("   Deadline: 100ms, Status: " +
                        (message.getAgeMs() <= 100 ? "✓ RESPECTÉE" : "✗ DÉPASSÉE"));

                // Afficher les headers
                System.out.println("   Headers:");
                message.getHeaders().forEach((key, value) -> {
                    System.out.println("     - " + key + ": " + value);
                });
            }

            @Override
            public String getSubscriberId() {
                return "TelemetryProcessor";
            }

            @Override
            public void onDeadlineMissed(String topicName, Message message) {
                System.out.println("\n⏰ ALERTE: Deadline manquée pour le topic: " + topicName);
                System.out.println("   Message: " + message.getId().substring(0, 8));
                System.out.println("   Âge: " + message.getAgeMs() + "ms");
            }
        });

        System.out.println("✅ Subscriber enregistré");

        // Petite pause pour laisser le système s'initialiser
        Thread.sleep(100);

        // 5. Publier un message
        System.out.println("\n📤 Publication d'un message...");
        Message msg = Message.builder("system/telemetry")
                .payload("Température: 45°C")
                .header("sensor_id", "temp-sensor-001")
                .header("timestamp", System.currentTimeMillis())
                .header("unit", "celsius")
                .build();

        long startTime = System.currentTimeMillis();
        middleware.publish("system/telemetry", msg);
        long publishTime = System.currentTimeMillis() - startTime;

        System.out.println("✅ Message publié en " + publishTime + "ms");
        System.out.println("   ID: " + msg.getId().substring(0, 8));

        // Attendre la réception et le traitement
        Thread.sleep(500);

        // 6. Arrêt propre
        System.out.println("\n🛑 Arrêt du middleware...");
        middleware.shutdown();

        System.out.println("✅ Application terminée avec succès!");
    }
}
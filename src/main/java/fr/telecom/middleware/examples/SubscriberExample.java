package fr.telecom.middleware.examples;

import fr.telecom.middleware.core.Subscriber;
import fr.telecom.middleware.api.Message;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Exemple de subscriber pour démonstration.
 */
public class SubscriberExample implements Subscriber {
    private static final AtomicInteger instanceCounter = new AtomicInteger(0);

    private final String subscriberId;
    private final String name;

    private AtomicInteger messageCount = new AtomicInteger(0);
    private long lastMessageTime = 0;
    private long totalProcessingTime = 0;

    public SubscriberExample(String name) {
        this.name = name;
        this.subscriberId = name + "-" + instanceCounter.incrementAndGet();
    }

    @Override
    public void onMessage(Message message) {
        long startTime = System.currentTimeMillis();
        int count = messageCount.incrementAndGet();
        lastMessageTime = System.currentTimeMillis();

        // Marquer le message comme reçu
        message.markAsReceived();

        System.out.printf("[%s] 📥 Message #%d reçu: %s%n",
                subscriberId, count, message.getId().substring(0, 8));

        // Afficher les détails du message
        System.out.printf("   Topic: %s%n", message.getTopic());
        System.out.printf("   Age: %dms%n", message.getAgeMs());

        // Traiter le payload
        Object payload = message.getPayload();
        if (payload != null) {
            System.out.printf("   Payload: %s%n", payload);

            // Traitement spécifique selon le type
            if (payload instanceof PublisherExample.SensorData) {
                processSensorData((PublisherExample.SensorData) payload);
            } else if (payload instanceof PublisherExample.Alert) {
                processAlert((PublisherExample.Alert) payload);
            } else if (payload instanceof String) {
                processString((String) payload);
            }
        }

        // Vérifier si le message est "vieux" (pour démonstration)
        if (message.getAgeMs() > 100) {
            System.out.printf("   ⚠️ Message vieux de %dms%n", message.getAgeMs());
        }

        long processingTime = System.currentTimeMillis() - startTime;
        totalProcessingTime += processingTime;

        System.out.printf("   Traité en %dms%n", processingTime);
        System.out.println();
    }

    /**
     * Traite les données de capteur.
     */
    private void processSensorData(PublisherExample.SensorData data) {
        // Simulation de traitement
        System.out.printf("   📊 Capteur: %s = %.2f %s%n",
                data.type, data.value, data.unit);

        // Détection d'anomalies simples
        if (data.type.equals("temperature") && data.value > 80) {
            System.out.println("   🔥 ALERTE: Température critique!");
        } else if (data.type.equals("pressure") && data.value < 50) {
            System.out.println("   ⚠️  AVERTISSEMENT: Pression basse");
        }
    }

    /**
     * Traite les alertes.
     */
    private void processAlert(PublisherExample.Alert alert) {
        System.out.printf("   🚨 Alerte %s: %s%n", alert.type, alert.message);

        // Couleur selon la sévérité
        if (alert.severity >= 4) {
            System.out.println("   ❗❗ URGENCE: Action immédiate requise!");
        } else if (alert.severity >= 3) {
            System.out.println("   ⚠️  Attention: Intervention nécessaire");
        }
    }

    /**
     * Traite les chaînes de caractères.
     */
    private void processString(String text) {
        System.out.printf("   📝 Texte: %s%n", text);
    }

    @Override
    public void onDeadlineMissed(String topicName, Message message) {
        System.out.printf("[%s] ⏰ DEADLINE MANQUÉE pour topic: %s, message: %s%n",
                subscriberId, topicName, message.getId().substring(0, 8));
    }

    @Override
    public String getSubscriberId() {
        return subscriberId;
    }

    // Getters pour statistiques
    public int getMessageCount() {
        return messageCount.get();
    }

    public long getLastMessageTime() {
        return lastMessageTime;
    }

    public double getAverageProcessingTime() {
        int count = messageCount.get();
        return count > 0 ? (double) totalProcessingTime / count : 0;
    }

    /**
     * Affiche les statistiques du subscriber.
     */
    public void printStats() {
        System.out.printf("%n📊 Statistiques pour %s:%n", subscriberId);
        System.out.printf("  Messages reçus: %d%n", getMessageCount());
        System.out.printf("  Dernier message: %tT%n", getLastMessageTime());
        System.out.printf("  Temps traitement moyen: %.2fms%n", getAverageProcessingTime());
    }
}
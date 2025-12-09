package fr.telecom.middleware.examples;

import fr.telecom.middleware.core.*;
import fr.telecom.middleware.qos.*;
import fr.telecom.middleware.api.Message;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Exemple de publisher pour démonstration.
 */
public class PublisherExample implements Publisher {
    private static final AtomicInteger instanceCounter = new AtomicInteger(0);

    private final Middleware middleware;
    private final String publisherId;
    private final String topicName;

    private long messageCount = 0;
    private long lastPublishTime = 0;

    public PublisherExample(Middleware middleware, String topicName) {
        this.middleware = middleware;
        this.topicName = topicName;
        this.publisherId = "Publisher-" + instanceCounter.incrementAndGet();
    }

    @Override
    public void publish(String topicName, Message message) {
        middleware.publish(topicName, message);
        messageCount++;
        lastPublishTime = System.currentTimeMillis();

        System.out.printf("[%s] 📤 Message publié: %s%n",
                publisherId, message.getId().substring(0, 8));
    }

    /**
     * Publie des données de capteur.
     */
    public void publishSensorData(String sensorType, double value, String unit) {
        SensorData data = new SensorData(sensorType, value, unit);

        Message message = Message.builder(topicName)
                .payload(data)
                .header("sensor_id", "sensor-" + sensorType + "-001")
                .header("timestamp", System.currentTimeMillis())
                .header("publisher_id", publisherId)
                .header("message_count", messageCount)
                .build();

        publish(topicName, message);
    }

    /**
     * Publie un message d'alerte.
     */
    public void publishAlert(String alertType, String messageText, int severity) {
        Alert alert = new Alert(alertType, messageText, severity);

        Message message = Message.builder(topicName)
                .payload(alert)
                .header("alert_timestamp", System.currentTimeMillis())
                .header("severity", severity)
                .header("source", publisherId)
                .build();

        publish(topicName, message);
    }

    /**
     * Publie un message de test simple.
     */
    public void publishTestMessage(String content) {
        Message message = Message.builder(topicName)
                .payload(content)
                .header("test_id", "test-" + System.currentTimeMillis())
                .header("sequence", messageCount)
                .build();

        publish(topicName, message);
    }

    // Getters
    @Override
    public String getPublisherId() {
        return publisherId;
    }

    public long getMessageCount() {
        return messageCount;
    }

    public long getLastPublishTime() {
        return lastPublishTime;
    }

    /**
     * Données de capteur.
     */
    public static class SensorData {
        public final String type;
        public final double value;
        public final String unit;
        public final long timestamp;

        public SensorData(String type, double value, String unit) {
            this.type = type;
            this.value = value;
            this.unit = unit;
            this.timestamp = System.currentTimeMillis();
        }

        @Override
        public String toString() {
            return String.format("%s: %.2f %s @ %tT", type, value, unit, timestamp);
        }
    }

    /**
     * Données d'alerte.
     */
    public static class Alert {
        public final String type;
        public final String message;
        public final int severity; // 1-5, 5 étant le plus critique
        public final long timestamp;

        public Alert(String type, String message, int severity) {
            this.type = type;
            this.message = message;
            this.severity = severity;
            this.timestamp = System.currentTimeMillis();
        }

        @Override
        public String toString() {
            return String.format("[%s] %s (severity: %d)", type, message, severity);
        }
    }
}
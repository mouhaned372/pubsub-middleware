package fr.telecom.middleware.api;

import fr.telecom.middleware.core.*;
import fr.telecom.middleware.qos.*;
import java.util.function.Consumer;

/**
 * API simplifiée pour utiliser le middleware.
 */
public class MiddlewareAPI {
    private final Middleware middleware;

    public MiddlewareAPI() {
        this.middleware = new Middleware();
    }

    public MiddlewareAPI(int maxMemoryKB, boolean realtimeEnabled) {
        this.middleware = new Middleware();
        this.middleware.setMaxMemoryKB(maxMemoryKB);
        this.middleware.setRealtimeEnabled(realtimeEnabled);
    }

    /**
     * Crée un topic avec une QoS.
     */
    public String createTopic(String name, QoS.Builder qosBuilder) {
        QoS qos = qosBuilder.build();
        middleware.createTopic(name, qos);
        return name;
    }

    /**
     * Publie un message sur un topic.
     */
    public void publish(String topicName, Object payload) {
        Message message = Message.builder(topicName)
                .payload(payload)
                .build();

        middleware.publish(topicName, message);
    }

    /**
     * Publie un message avec des métadonnées.
     */
    public void publish(String topicName, Object payload, Consumer<Message.Builder> configurator) {
        Message.Builder builder = Message.builder(topicName)
                .payload(payload);

        configurator.accept(builder);
        Message message = builder.build();

        middleware.publish(topicName, message);
    }

    /**
     * S'abonne à un topic avec un handler.
     */
    public void subscribe(String topicName, Consumer<Message> handler) {
        Subscriber subscriber = new Subscriber() {
            @Override
            public void onMessage(Message message) {
                handler.accept(message);
            }

            @Override
            public String getSubscriberId() {
                return "Handler-" + topicName + "-" + hashCode();
            }
        };

        middleware.subscribe(topicName, subscriber);
    }

    /**
     * S'abonne à un topic avec un subscriber personnalisé.
     */
    public void subscribe(String topicName, Subscriber subscriber) {
        middleware.subscribe(topicName, subscriber);
    }

    /**
     * Récupère les statistiques du middleware.
     */
    public MiddlewareStats getStats() {
        MiddlewareStats stats = new MiddlewareStats();
        stats.topicCount = (int) middleware.getAllTopics().stream().count();
        stats.totalMessages = middleware.getAllTopics().stream()
                .mapToLong(Topic::getMessageCount)
                .sum();
        stats.activeSubscribers = middleware.getAllTopics().stream()
                .mapToInt(t -> t.getSubscribers().size())
                .sum();

        return stats;
    }

    /**
     * Arrête le middleware.
     */
    public void shutdown() {
        middleware.shutdown();
    }

    /**
     * Classe pour les statistiques.
     */
    public static class MiddlewareStats {
        public int topicCount;
        public long totalMessages;
        public int activeSubscribers;

        @Override
        public String toString() {
            return String.format(
                    "MiddlewareStats{topics=%d, messages=%d, subscribers=%d}",
                    topicCount, totalMessages, activeSubscribers
            );
        }
    }

    /**
     * Retourne l'instance du middleware.
     */
    public Middleware getMiddleware() {
        return middleware;
    }
}
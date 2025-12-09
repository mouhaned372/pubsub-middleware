package fr.telecom.middleware.fault;

import fr.telecom.middleware.core.Topic;
import fr.telecom.middleware.core.Subscriber;
import java.util.*;
import java.util.concurrent.*;
import org.slf4j.*;

/**
 * Détecte les pannes des subscribers.
 */
public class FaultDetector {
    private static final Logger logger = LoggerFactory.getLogger(FaultDetector.class);

    private final ScheduledExecutorService healthChecker;
    private final Map<String, SubscriberHealth> monitoredSubscribers;
    private final Map<String, Integer> failureCounts;

    private static final long HEALTH_CHECK_INTERVAL_MS = 5000;
    private static final long SUBSCRIBER_TIMEOUT_MS = 10000;
    private static final int MAX_FAILURES = 3;

    public FaultDetector() {
        this.healthChecker = Executors.newScheduledThreadPool(1);
        this.monitoredSubscribers = new ConcurrentHashMap<>();
        this.failureCounts = new ConcurrentHashMap<>();

        startHealthChecking();
        logger.info("FaultDetector initialisé (intervalle: {}ms)", HEALTH_CHECK_INTERVAL_MS);
    }

    /**
     * Classe interne pour suivre la santé d'un subscriber.
     */
    private static class SubscriberHealth {
        Subscriber subscriber;
        Topic topic;
        long lastHeartbeat;
        long lastMessageReceived;

        SubscriberHealth(Subscriber subscriber, Topic topic) {
            this.subscriber = subscriber;
            this.topic = topic;
            this.lastHeartbeat = System.currentTimeMillis();
            this.lastMessageReceived = System.currentTimeMillis();
        }
    }

    /**
     * Démarre la vérification périodique de santé.
     */
    private void startHealthChecking() {
        healthChecker.scheduleAtFixedRate(
                this::checkSubscribersHealth,
                HEALTH_CHECK_INTERVAL_MS,
                HEALTH_CHECK_INTERVAL_MS,
                TimeUnit.MILLISECONDS
        );
    }

    /**
     * Commence à surveiller un subscriber.
     */
    public void monitorSubscriber(Subscriber subscriber, Topic topic) {
        String key = createKey(subscriber, topic);
        if (!monitoredSubscribers.containsKey(key)) {
            monitoredSubscribers.put(key, new SubscriberHealth(subscriber, topic));
            logger.info("Surveillance activée pour: {}", key);
        }
    }

    /**
     * Arrête la surveillance d'un subscriber.
     */
    public void stopMonitoring(Subscriber subscriber, Topic topic) {
        String key = createKey(subscriber, topic);
        monitoredSubscribers.remove(key);
        logger.info("Surveillance arrêtée pour: {}", key);
    }

    /**
     * Enregistre un heartbeat d'un subscriber.
     */
    public void heartbeat(Subscriber subscriber, Topic topic) {
        String key = createKey(subscriber, topic);
        SubscriberHealth health = monitoredSubscribers.get(key);
        if (health != null) {
            health.lastHeartbeat = System.currentTimeMillis();
            failureCounts.remove(key); // Réinitialiser le compteur d'échecs
        }
    }

    /**
     * Enregistre la réception d'un message.
     */
    public void recordMessageReceived(Subscriber subscriber, Topic topic) {
        String key = createKey(subscriber, topic);
        SubscriberHealth health = monitoredSubscribers.get(key);
        if (health != null) {
            health.lastMessageReceived = System.currentTimeMillis();
        }
    }

    /**
     * Vérifie la santé de tous les subscribers surveillés.
     */
    private void checkSubscribersHealth() {
        long now = System.currentTimeMillis();

        monitoredSubscribers.entrySet().removeIf(entry -> {
            String key = entry.getKey();
            SubscriberHealth health = entry.getValue();

            // Vérifier le timeout
            if (now - health.lastHeartbeat > SUBSCRIBER_TIMEOUT_MS) {
                int failures = failureCounts.getOrDefault(key, 0) + 1;
                failureCounts.put(key, failures);

                logger.warn("Subscriber potentiellement en panne: {} (échecs: {})",
                        key, failures);

                if (failures >= MAX_FAILURES) {
                    logger.error("Subscriber déclaré hors service: {}", key);
                    notifySubscriberFailure(health.subscriber, health.topic);
                    return true; // Supprimer de la surveillance
                }
            } else {
                // Réinitialiser le compteur d'échecs si le subscriber répond
                failureCounts.remove(key);
            }

            return false;
        });
    }

    /**
     * Notifie qu'un subscriber est en panne.
     */
    private void notifySubscriberFailure(Subscriber subscriber, Topic topic) {
        // Ici, on pourrait notifier d'autres composants du système
        // ou déclencher des actions de récupération
        logger.error("Panne détectée: subscriber={}, topic={}",
                subscriber.getSubscriberId(), topic.getName());

        System.err.printf("[FAULT] Subscriber en panne: %s sur topic %s%n",
                subscriber.getSubscriberId(), topic.getName());
    }

    /**
     * Crée une clé unique pour un subscriber/topic.
     */
    private String createKey(Subscriber subscriber, Topic topic) {
        return subscriber.getSubscriberId() + "@" + topic.getName();
    }

    /**
     * Récupère la liste des subscribers surveillés.
     */
    public List<String> getMonitoredSubscribers() {
        return new ArrayList<>(monitoredSubscribers.keySet());
    }

    /**
     * Récupère les statistiques d'échecs.
     */
    public Map<String, Integer> getFailureStats() {
        return new HashMap<>(failureCounts);
    }

    /**
     * Arrête le détecteur de pannes.
     */
    public void shutdown() {
        healthChecker.shutdown();
        try {
            if (!healthChecker.awaitTermination(5, TimeUnit.SECONDS)) {
                healthChecker.shutdownNow();
            }
        } catch (InterruptedException e) {
            healthChecker.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.info("FaultDetector arrêté");
    }
}
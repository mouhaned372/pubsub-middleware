package fr.telecom.middleware.core;

import fr.telecom.middleware.qos.*;
import fr.telecom.middleware.realtime.*;
import fr.telecom.middleware.fault.*;
import fr.telecom.middleware.api.*;
import java.util.*;
import java.util.concurrent.*;
import org.slf4j.*;

/**
 * Classe principale du middleware.
 * Gère les topics, les publications et les abonnements.
 */
public class Middleware {
    private static final Logger logger = LoggerFactory.getLogger(Middleware.class);

    private final Map<String, Topic> topics = new ConcurrentHashMap<>();
    private final ExecutorService executor;
    private final RedundancyManager redundancyManager;
    private final DeadlineMonitor deadlineMonitor;
    private final FaultDetector faultDetector;

    private int maxMemoryKB = 1024;
    private boolean realtimeEnabled = true;

    public Middleware() {
        this.executor = Executors.newFixedThreadPool(4);
        this.redundancyManager = new RedundancyManager();
        this.deadlineMonitor = new DeadlineMonitor();
        this.faultDetector = new FaultDetector();

        logger.info("Middleware initialisé");
        logger.info("Mémoire maximale configurée: {} KB", maxMemoryKB);
    }

    /**
     * Crée un nouveau topic avec une QoS spécifique.
     */
    public Topic createTopic(String name, QoS qos) {
        if (topics.containsKey(name)) {
            logger.warn("Topic '{}' existe déjà, retour de l'existant", name);
            return topics.get(name);
        }

        Topic topic = new Topic(name, qos);
        topics.put(name, topic);

        if (qos.hasDeadline()) {
            deadlineMonitor.monitorTopic(topic);
        }

        logger.info("Topic créé: '{}' avec QoS: {}", name, qos);
        return topic;
    }

    /**
     * Publie un message sur un topic.
     */
    public void publish(String topicName, Message message) {
        Topic topic = topics.get(topicName);
        if (topic == null) {
            logger.error("Topic '{}' non trouvé", topicName);
            return;
        }

        // Vérification mémoire
        if (getCurrentMemoryUsage() > maxMemoryKB) {
            logger.warn("Limite mémoire atteinte ({} KB), nettoyage...", maxMemoryKB);
            cleanupMemory();
        }

        // Vérification deadline
        if (realtimeEnabled && topic.getQoS().hasDeadline()) {
            if (!deadlineMonitor.checkPublishDeadline(topic, message)) {
                logger.error("Deadline manquée pour la publication sur '{}'", topicName);
                return;
            }
        }

        // Publication avec redondance si nécessaire
        if (topic.getQoS().getRedundancyLevel() > 0) {
            redundancyManager.sendWithRedundancy(topic, message);
        } else {
            topic.publish(message);
        }

        logger.debug("Message publié sur '{}': {}", topicName, message.getId());
    }

    /**
     * Abonne un subscriber à un topic.
     */
    public void subscribe(String topicName, Subscriber subscriber) {
        Topic topic = topics.get(topicName);
        if (topic != null) {
            topic.addSubscriber(subscriber);
            faultDetector.monitorSubscriber(subscriber, topic);
            logger.info("Subscriber '{}' abonné à '{}'",
                    subscriber.getSubscriberId(), topicName);
        } else {
            logger.error("Impossible de s'abonner: topic '{}' non trouvé", topicName);
        }
    }

    /**
     * Désabonne un subscriber d'un topic.
     */
    public void unsubscribe(String topicName, Subscriber subscriber) {
        Topic topic = topics.get(topicName);
        if (topic != null) {
            topic.removeSubscriber(subscriber);
            logger.info("Subscriber '{}' désabonné de '{}'",
                    subscriber.getSubscriberId(), topicName);
        }
    }

    /**
     * Récupère un topic par son nom.
     */
    public Topic getTopic(String name) {
        return topics.get(name);
    }

    /**
     * Retourne tous les topics.
     */
    public Collection<Topic> getAllTopics() {
        return topics.values();
    }

    /**
     * Calcule l'utilisation mémoire actuelle.
     */
    private int getCurrentMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        return (int) (usedMemory / 1024);
    }

    /**
     * Nettoie la mémoire en supprimant les anciens messages.
     */
    private void cleanupMemory() {
        topics.values().forEach(Topic::cleanupOldMessages);
        System.gc();
    }

    /**
     * Arrête proprement le middleware.
     */
    public void shutdown() {
        executor.shutdown();
        deadlineMonitor.shutdown();
        faultDetector.shutdown();

        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        logger.info("Middleware arrêté");
    }

    // Getters et Setters
    public void setMaxMemoryKB(int maxMemoryKB) {
        this.maxMemoryKB = maxMemoryKB;
    }

    public void setRealtimeEnabled(boolean realtimeEnabled) {
        this.realtimeEnabled = realtimeEnabled;
    }

    public int getMaxMemoryKB() {
        return maxMemoryKB;
    }

    public boolean isRealtimeEnabled() {
        return realtimeEnabled;
    }
}
package fr.telecom.middleware.fault;

import fr.telecom.middleware.core.Topic;
import fr.telecom.middleware.api.Message;
import java.util.*;
import java.util.concurrent.*;
import org.slf4j.*;

/**
 * Gère la récupération après pannes.
 */
public class RecoveryManager {
    private static final Logger logger = LoggerFactory.getLogger(RecoveryManager.class);

    private final Map<String, List<Message>> recoveryBuffer;
    private final Map<String, Long> lastRecoveryTime;

    private static final int MAX_RECOVERY_BUFFER_SIZE = 100;
    private static final long RECOVERY_COOLDOWN_MS = 5000;

    public RecoveryManager() {
        this.recoveryBuffer = new ConcurrentHashMap<>();
        this.lastRecoveryTime = new ConcurrentHashMap<>();
        logger.info("RecoveryManager initialisé");
    }

    /**
     * Enregistre un message pour récupération future.
     */
    public void bufferForRecovery(String topicName, Message message) {
        recoveryBuffer
                .computeIfAbsent(topicName, k -> new CopyOnWriteArrayList<>())
                .add(message);

        // Limiter la taille du buffer
        List<Message> buffer = recoveryBuffer.get(topicName);
        if (buffer.size() > MAX_RECOVERY_BUFFER_SIZE) {
            buffer.subList(0, buffer.size() - MAX_RECOVERY_BUFFER_SIZE / 2).clear();
        }

        logger.debug("Message bufferisé pour récupération: topic={}, message={}",
                topicName, message.getId().substring(0, 8));
    }

    /**
     * Tente de récupérer les messages perdus pour un topic.
     */
    public void recoverMessages(String topicName, Topic topic) {
        String key = topicName;
        long now = System.currentTimeMillis();

        // Vérifier le cooldown
        Long lastRecovery = lastRecoveryTime.get(key);
        if (lastRecovery != null && now - lastRecovery < RECOVERY_COOLDOWN_MS) {
            logger.debug("Récupération en cooldown pour: {}", topicName);
            return;
        }

        List<Message> buffer = recoveryBuffer.get(topicName);
        if (buffer == null || buffer.isEmpty()) {
            return;
        }

        logger.info("Début récupération pour '{}': {} messages",
                topicName, buffer.size());

        // Republier les messages
        int recoveredCount = 0;
        for (Message message : buffer) {
            try {
                topic.publish(message);
                recoveredCount++;
                logger.debug("Message récupéré: {}", message.getId().substring(0, 8));
            } catch (Exception e) {
                logger.error("Erreur lors de la récupération: {}", e.getMessage());
            }
        }

        // Vider le buffer
        buffer.clear();
        lastRecoveryTime.put(key, now);

        logger.info("Récupération terminée: {} messages récupérés sur {}",
                recoveredCount, topicName);
    }

    /**
     * Vérifie si des messages sont en attente de récupération.
     */
    public boolean hasPendingRecovery(String topicName) {
        List<Message> buffer = recoveryBuffer.get(topicName);
        return buffer != null && !buffer.isEmpty();
    }

    /**
     * Récupère le nombre de messages en attente.
     */
    public int getPendingRecoveryCount(String topicName) {
        List<Message> buffer = recoveryBuffer.get(topicName);
        return buffer != null ? buffer.size() : 0;
    }

    /**
     * Vider le buffer de récupération.
     */
    public void clearRecoveryBuffer(String topicName) {
        List<Message> buffer = recoveryBuffer.get(topicName);
        if (buffer != null) {
            buffer.clear();
            logger.info("Buffer de récupération vidé pour: {}", topicName);
        }
    }

    /**
     * Récupère les statistiques de récupération.
     */
    public Map<String, Integer> getRecoveryStats() {
        Map<String, Integer> stats = new HashMap<>();
        recoveryBuffer.forEach((topic, buffer) -> {
            stats.put(topic, buffer.size());
        });
        return stats;
    }

    /**
     * Arrête le gestionnaire de récupération.
     */
    public void shutdown() {
        recoveryBuffer.clear();
        lastRecoveryTime.clear();
        logger.info("RecoveryManager arrêté");
    }
}
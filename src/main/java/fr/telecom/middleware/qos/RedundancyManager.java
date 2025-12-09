package fr.telecom.middleware.qos;

import fr.telecom.middleware.core.Topic;
import fr.telecom.middleware.api.Message;
import java.util.*;
import java.util.concurrent.*;
import org.slf4j.*;

/**
 * Gère la redondance des messages pour la tolérance aux pannes.
 */
public class RedundancyManager {
    private static final Logger logger = LoggerFactory.getLogger(RedundancyManager.class);

    private final Map<String, List<Message>> redundantMessages;
    private final Map<String, Integer> deliveryAttempts;

    public RedundancyManager() {
        this.redundantMessages = new ConcurrentHashMap<>();
        this.deliveryAttempts = new ConcurrentHashMap<>();
    }

    /**
     * Publie un message avec redondance.
     */
    public void sendWithRedundancy(Topic topic, Message originalMessage) {
        String topicName = topic.getName();
        int redundancyLevel = topic.getQoS().getRedundancyLevel();

        if (redundancyLevel <= 0) {
            topic.publish(originalMessage);
            return;
        }

        // Stocker le message original
        storeRedundantMessage(topicName, originalMessage);

        // Publier les copies redondantes
        for (int i = 0; i < redundancyLevel; i++) {
            Message redundantCopy = createRedundantCopy(originalMessage, i + 1);
            topic.publish(redundantCopy);

            logger.debug("Message redondant {}/{} publié sur '{}'",
                    i + 1, redundancyLevel, topicName);
        }

        // Nettoyer les anciens messages
        cleanupOldMessages(topicName);
    }

    /**
     * Crée une copie redondante d'un message.
     */
    private Message createRedundantCopy(Message original, int copyIndex) {
        Message copy = Message.builder(original.getTopic())
                .payload(original.getPayload())
                .build();

        // Copier les headers
        original.getHeaders().forEach(copy::addHeader);

        // Ajouter des métadonnées de redondance
        copy.addHeader("redundancy_index", copyIndex);
        copy.addHeader("original_message_id", original.getId());
        copy.addHeader("redundancy_timestamp", System.currentTimeMillis());

        return copy;
    }

    /**
     * Stocke un message pour référence future.
     */
    private void storeRedundantMessage(String topicName, Message message) {
        redundantMessages
                .computeIfAbsent(topicName, k -> new CopyOnWriteArrayList<>())
                .add(message);

        // Limiter le nombre de messages stockés
        List<Message> messages = redundantMessages.get(topicName);
        if (messages.size() > 100) {
            messages.subList(0, messages.size() - 50).clear();
        }
    }

    /**
     * Nettoie les anciens messages redondants.
     */
    private void cleanupOldMessages(String topicName) {
        List<Message> messages = redundantMessages.get(topicName);
        if (messages != null && messages.size() > 50) {
            // Garder seulement les 50 derniers messages
            messages.subList(0, messages.size() - 50).clear();
        }
    }

    /**
     * Récupère les messages redondants pour un topic.
     */
    public List<Message> getRedundantMessages(String topicName) {
        return redundantMessages.getOrDefault(topicName, new ArrayList<>());
    }

    /**
     * Enregistre une tentative de livraison.
     */
    public void recordDeliveryAttempt(String messageId) {
        deliveryAttempts.merge(messageId, 1, Integer::sum);
    }

    /**
     * Vérifie si un message a été livré avec succès.
     */
    public boolean isMessageDelivered(String messageId) {
        return deliveryAttempts.getOrDefault(messageId, 0) > 0;
    }

    /**
     * Réinitialise les compteurs pour un message.
     */
    public void resetDeliveryAttempts(String messageId) {
        deliveryAttempts.remove(messageId);
    }
}
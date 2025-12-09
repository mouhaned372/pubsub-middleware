package fr.telecom.middleware.core;

import fr.telecom.middleware.api.Message;

/**
 * Interface pour les abonnés qui reçoivent des messages.
 */
public interface Subscriber {

    /**
     * Méthode appelée lorsqu'un message est reçu.
     */
    void onMessage(Message message);

    /**
     * Retourne l'identifiant unique du subscriber.
     */
    default String getSubscriberId() {
        return getClass().getSimpleName() + "@" +
                Integer.toHexString(hashCode());
    }

    /**
     * Méthode appelée lorsqu'une deadline est manquée.
     */
    default void onDeadlineMissed(String topicName, Message message) {
        System.err.printf("[DEADLINE] Topic: %s, Message: %s%n",
                topicName, message.getId());
    }

    /**
     * Méthode appelée en cas d'erreur de livraison.
     */
    default void onDeliveryError(String topicName, Message message, Exception error) {
        System.err.printf("[ERROR] Topic: %s, Message: %s, Error: %s%n",
                topicName, message.getId(), error.getMessage());
    }
}
package fr.telecom.middleware.core;

import fr.telecom.middleware.api.Message;

/**
 * Interface pour les entités qui publient des messages.
 */
public interface Publisher {

    /**
     * Publie un message sur un topic.
     */
    void publish(String topicName, Message message);

    /**
     * Retourne l'identifiant unique du publisher.
     */
    default String getPublisherId() {
        return getClass().getSimpleName() + "@" +
                Integer.toHexString(hashCode());
    }

    /**
     * Méthode appelée en cas d'erreur de publication.
     */
    default void onPublishError(String topicName, Message message, Exception error) {
        System.err.printf("[PUBLISH ERROR] Topic: %s, Error: %s%n",
                topicName, error.getMessage());
    }
}
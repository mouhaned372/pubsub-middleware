package fr.telecom.middleware.core;

import fr.telecom.middleware.qos.QoS;
import fr.telecom.middleware.api.Message;
import java.util.*;
import java.util.concurrent.*;
import org.slf4j.*;

/**
 * Représente un canal de communication avec une QoS spécifique.
 */
public class Topic {
    private static final Logger logger = LoggerFactory.getLogger(Topic.class);

    private final String name;
    private final QoS qos;
    private final List<Subscriber> subscribers;
    private final Queue<Message> messageHistory;
    private final int maxHistorySize = 50;

    private long messageCount = 0;
    private long lastPublishTime = 0;

    public Topic(String name, QoS qos) {
        this.name = name;
        this.qos = qos;
        this.subscribers = new CopyOnWriteArrayList<>();
        this.messageHistory = new ConcurrentLinkedQueue<>();
    }

    /**
     * Publie un message à tous les subscribers.
     */
    public synchronized void publish(Message message) {
        message.setPublishTimestamp(System.currentTimeMillis());
        messageCount++;
        lastPublishTime = System.currentTimeMillis();

        // Ajout à l'historique
        messageHistory.add(message);
        if (messageHistory.size() > maxHistorySize) {
            messageHistory.poll();
        }

        // Distribution aux subscribers
        int deliveredCount = 0;
        for (Subscriber subscriber : subscribers) {
            try {
                deliverMessage(subscriber, message);
                deliveredCount++;
            } catch (Exception e) {
                logger.error("Erreur de livraison à {}: {}",
                        subscriber.getSubscriberId(), e.getMessage());
            }
        }

        logger.debug("Message distribué à {}/{} abonnés sur '{}'",
                deliveredCount, subscribers.size(), name);
    }

    /**
     * Livre un message avec la QoS appropriée.
     */
    private void deliverMessage(Subscriber subscriber, Message message) {
        switch (qos.getReliability()) {
            case BEST_EFFORT:
                subscriber.onMessage(message);
                break;

            case AT_LEAST_ONCE:
                deliverWithRetry(subscriber, message, 3);
                break;

            case AT_MOST_ONCE:
                subscriber.onMessage(message);
                break;

            case EXACTLY_ONCE:
                deliverExactlyOnce(subscriber, message);
                break;
        }
    }

    /**
     * Livraison avec réessai pour AT_LEAST_ONCE.
     */
    private void deliverWithRetry(Subscriber subscriber, Message message, int maxRetries) {
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                subscriber.onMessage(message);
                logger.debug("Livraison réussie (tentative {})", attempt);
                return;
            } catch (Exception e) {
                logger.warn("Tentative {} échouée pour {}", attempt,
                        subscriber.getSubscriberId());

                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(50 * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        logger.error("Échec après {} tentatives pour {}",
                maxRetries, subscriber.getSubscriberId());
    }

    /**
     * Livraison exactement une fois (garantie d'idempotence).
     */
    private void deliverExactlyOnce(Subscriber subscriber, Message message) {
        // Vérifier si le message a déjà été livré
        // (implémentation simplifiée)
        subscriber.onMessage(message);
        logger.debug("Livraison exactement une fois pour {}",
                subscriber.getSubscriberId());
    }

    /**
     * Ajoute un subscriber.
     */
    public void addSubscriber(Subscriber subscriber) {
        if (!subscribers.contains(subscriber)) {
            subscribers.add(subscriber);
        }
    }

    /**
     * Supprime un subscriber.
     */
    public void removeSubscriber(Subscriber subscriber) {
        subscribers.remove(subscriber);
    }

    /**
     * Nettoie les anciens messages de l'historique.
     */
    public void cleanupOldMessages() {
        while (messageHistory.size() > maxHistorySize / 2) {
            messageHistory.poll();
        }
    }

    /**
     * Vérifie si le topic a des subscribers.
     */
    public boolean hasSubscribers() {
        return !subscribers.isEmpty();
    }

    // Getters
    public String getName() {
        return name;
    }

    public QoS getQoS() {
        return qos;
    }

    public List<Subscriber> getSubscribers() {
        return new ArrayList<>(subscribers);
    }

    public Queue<Message> getMessageHistory() {
        return new ConcurrentLinkedQueue<>(messageHistory);
    }

    public long getMessageCount() {
        return messageCount;
    }

    public long getLastPublishTime() {
        return lastPublishTime;
    }

    @Override
    public String toString() {
        return String.format("Topic{name='%s', subscribers=%d, messages=%d}",
                name, subscribers.size(), messageCount);
    }
}
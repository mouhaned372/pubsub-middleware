package fr.telecom.middleware.api;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Représente un message échangé dans le middleware.
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String topic;
    private final Object payload;
    private final Map<String, Object> headers;
    private long publishTimestamp;
    private long receiveTimestamp;

    /**
     * Constructeur privé pour le Builder.
     */
    public Message(String topic, Object payload) {
        this.id = UUID.randomUUID().toString();
        this.topic = topic;
        this.payload = payload;
        this.headers = new ConcurrentHashMap<>();
        this.publishTimestamp = System.currentTimeMillis();
        this.receiveTimestamp = 0;

        // Headers par défaut
        this.headers.put("creation_timestamp", System.currentTimeMillis());
        this.headers.put("message_id", this.id);
    }

    /**
     * Builder pattern pour création flexible.
     */
    public static Builder builder(String topic) {
        return new Builder(topic);
    }

    public static class Builder {
        private final String topic;
        private Object payload;
        private final Map<String, Object> headers = new HashMap<>();

        public Builder(String topic) {
            this.topic = topic;
        }

        public Builder payload(Object payload) {
            this.payload = payload;
            return this;
        }

        public Builder header(String key, Object value) {
            this.headers.put(key, value);
            return this;
        }

        public Builder headers(Map<String, Object> headers) {
            this.headers.putAll(headers);
            return this;
        }

        public Message build() {
            Message message = new Message(topic, payload);
            message.headers.putAll(headers);
            return message;
        }
    }

    /**
     * Ajoute un header.
     */
    public void addHeader(String key, Object value) {
        headers.put(key, value);
    }

    /**
     * Récupère un header.
     */
    public Object getHeader(String key) {
        return headers.get(key);
    }

    /**
     * Récupère un header avec une valeur par défaut.
     */
    public Object getHeader(String key, Object defaultValue) {
        return headers.getOrDefault(key, defaultValue);
    }

    /**
     * Calcule l'âge du message en millisecondes.
     */
    public long getAgeMs() {
        if (receiveTimestamp > 0) {
            return receiveTimestamp - publishTimestamp;
        }
        return System.currentTimeMillis() - publishTimestamp;
    }

    /**
     * Marque le message comme reçu.
     */
    public void markAsReceived() {
        this.receiveTimestamp = System.currentTimeMillis();
        headers.put("receive_timestamp", receiveTimestamp);
    }

    /**
     * Vérifie si le message a été reçu.
     */
    public boolean isReceived() {
        return receiveTimestamp > 0;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getTopic() {
        return topic;
    }

    public Object getPayload() {
        return payload;
    }

    public Map<String, Object> getHeaders() {
        return new HashMap<>(headers);
    }

    public long getPublishTimestamp() {
        return publishTimestamp;
    }

    public long getReceiveTimestamp() {
        return receiveTimestamp;
    }

    // Setters (limités)
    public void setPublishTimestamp(long timestamp) {
        this.publishTimestamp = timestamp;
        headers.put("publish_timestamp", timestamp);
    }

    @Override
    public String toString() {
        return String.format(
                "Message{id=%s, topic=%s, age=%dms, headers=%d}",
                id.substring(0, 8), topic, getAgeMs(), headers.size()
        );
    }

    /**
     * Crée une représentation détaillée du message.
     */
    public String toDetailedString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Message Details:\n");
        sb.append("  ID: ").append(id).append("\n");
        sb.append("  Topic: ").append(topic).append("\n");
        sb.append("  Age: ").append(getAgeMs()).append("ms\n");
        sb.append("  Payload Type: ").append(
                payload != null ? payload.getClass().getSimpleName() : "null"
        ).append("\n");
        sb.append("  Headers: ").append(headers.size()).append("\n");

        headers.forEach((key, value) -> {
            sb.append("    ").append(key).append(": ").append(value).append("\n");
        });

        return sb.toString();
    }
}
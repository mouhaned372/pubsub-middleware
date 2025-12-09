package fr.telecom.middleware.qos;

import fr.telecom.middleware.api.Message;

/**
 * Représente une deadline à respecter.
 */
public class Deadline {
    private final String deadlineId;
    private final String topicName;
    private final Message message;
    private final long deadlineTimestamp;
    private boolean met = false;
    private boolean expired = false;

    public Deadline(String topicName, Message message, int deadlineMs) {
        this.deadlineId = "DL-" + System.currentTimeMillis() + "-" +
                topicName.hashCode();
        this.topicName = topicName;
        this.message = message;
        this.deadlineTimestamp = System.currentTimeMillis() + deadlineMs;
    }

    /**
     * Marque la deadline comme respectée.
     */
    public void markAsMet() {
        this.met = true;
    }

    /**
     * Vérifie si la deadline est expirée.
     */
    public boolean isExpired() {
        if (!expired && !met) {
            expired = System.currentTimeMillis() > deadlineTimestamp;
        }
        return expired;
    }

    /**
     * Calcule le temps restant avant la deadline.
     */
    public long getRemainingTimeMs() {
        long remaining = deadlineTimestamp - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    /**
     * Calcule le dépassement de deadline.
     */
    public long getOvertimeMs() {
        if (!isExpired()) return 0;
        return System.currentTimeMillis() - deadlineTimestamp;
    }

    // Getters
    public String getDeadlineId() {
        return deadlineId;
    }

    public String getTopicName() {
        return topicName;
    }

    public Message getMessage() {
        return message;
    }

    public boolean isMet() {
        return met;
    }

    public long getDeadlineTimestamp() {
        return deadlineTimestamp;
    }

    @Override
    public String toString() {
        return String.format(
                "Deadline{id=%s, topic=%s, remaining=%dms, met=%s}",
                deadlineId.substring(0, 8), topicName, getRemainingTimeMs(), met
        );
    }
}
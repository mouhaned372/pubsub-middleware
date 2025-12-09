package fr.telecom.middleware.qos;

/**
 * Configuration de la Qualité de Service.
 */
public class QoS {

    /**
     * Niveaux de fiabilité.
     */
    public enum Reliability {
        BEST_EFFORT,       // Aucune garantie
        AT_LEAST_ONCE,     // Au moins une fois (avec retry)
        AT_MOST_ONCE,      // Au plus une fois (sans retry)
        EXACTLY_ONCE       // Exactement une fois (idempotent)
    }

    /**
     * Priorités des messages.
     */
    public enum Priority {
        LOW(1), MEDIUM(2), HIGH(3), CRITICAL(4);

        private final int value;

        Priority(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private Reliability reliability = Reliability.BEST_EFFORT;
    private Priority priority = Priority.MEDIUM;
    private int deadlineMs = 0;
    private int maxLatencyMs = 1000;
    private int redundancyLevel = 0;
    private boolean persistence = false;

    /**
     * Builder pattern pour création facile.
     */
    public static class Builder {
        private final QoS qos = new QoS();

        public Builder reliability(Reliability reliability) {
            qos.reliability = reliability;
            return this;
        }

        public Builder priority(Priority priority) {
            qos.priority = priority;
            return this;
        }

        public Builder deadlineMs(int deadlineMs) {
            qos.deadlineMs = deadlineMs;
            return this;
        }

        public Builder maxLatencyMs(int maxLatencyMs) {
            qos.maxLatencyMs = maxLatencyMs;
            return this;
        }

        public Builder redundancyLevel(int redundancyLevel) {
            qos.redundancyLevel = redundancyLevel;
            return this;
        }

        public Builder persistence(boolean persistence) {
            qos.persistence = persistence;
            return this;
        }

        public QoS build() {
            return qos;
        }
    }

    // Constructeurs
    public QoS() {}

    public QoS(Reliability reliability, Priority priority, int deadlineMs) {
        this.reliability = reliability;
        this.priority = priority;
        this.deadlineMs = deadlineMs;
    }

    // Getters
    public Reliability getReliability() {
        return reliability;
    }

    public Priority getPriority() {
        return priority;
    }

    public int getDeadlineMs() {
        return deadlineMs;
    }

    public int getMaxLatencyMs() {
        return maxLatencyMs;
    }

    public int getRedundancyLevel() {
        return redundancyLevel;
    }

    public boolean isPersistence() {
        return persistence;
    }

    // Méthodes utilitaires
    public boolean hasDeadline() {
        return deadlineMs > 0;
    }

    public boolean isReliable() {
        return reliability != Reliability.BEST_EFFORT;
    }

    public boolean isCritical() {
        return priority == Priority.CRITICAL;
    }

    @Override
    public String toString() {
        return String.format(
                "QoS{reliability=%s, priority=%s, deadline=%dms, latency=%dms, redundancy=%d}",
                reliability, priority, deadlineMs, maxLatencyMs, redundancyLevel
        );
    }
}
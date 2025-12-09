package fr.telecom.middleware.realtime;

import fr.telecom.middleware.core.Topic;
import fr.telecom.middleware.api.Message;
import fr.telecom.middleware.qos.Deadline;
import java.util.*;
import java.util.concurrent.*;
import org.slf4j.*;

/**
 * Surveille et vérifie le respect des deadlines.
 */
public class DeadlineMonitor {
    private static final Logger logger = LoggerFactory.getLogger(DeadlineMonitor.class);

    private final ScheduledExecutorService scheduler;
    private final Map<String, List<Deadline>> activeDeadlines;
    private final Map<String, Long> missedDeadlines;

    private static final int CHECK_INTERVAL_MS = 10;

    public DeadlineMonitor() {
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.activeDeadlines = new ConcurrentHashMap<>();
        this.missedDeadlines = new ConcurrentHashMap<>();

        // Démarrer la surveillance périodique
        startMonitoring();

        logger.info("DeadlineMonitor initialisé (intervalle: {}ms)", CHECK_INTERVAL_MS);
    }

    /**
     * Démarre la surveillance périodique.
     */
    private void startMonitoring() {
        scheduler.scheduleAtFixedRate(
                this::checkAllDeadlines,
                CHECK_INTERVAL_MS,
                CHECK_INTERVAL_MS,
                TimeUnit.MILLISECONDS
        );
    }

    /**
     * Surveille un topic pour les deadlines.
     */
    public void monitorTopic(Topic topic) {
        String topicName = topic.getName();
        if (topic.getQoS().hasDeadline()) {
            activeDeadlines.putIfAbsent(topicName, new CopyOnWriteArrayList<>());
            logger.info("Surveillance deadline activée pour '{}' ({}ms)",
                    topicName, topic.getQoS().getDeadlineMs());
        }
    }

    /**
     * Vérifie si une publication respecte les deadlines.
     */
    public boolean checkPublishDeadline(Topic topic, Message message) {
        int deadlineMs = topic.getQoS().getDeadlineMs();
        if (deadlineMs <= 0) {
            return true; // Pas de deadline
        }

        String topicName = topic.getName();
        Deadline deadline = new Deadline(topicName, message, deadlineMs);

        // Enregistrer la deadline
        activeDeadlines
                .computeIfAbsent(topicName, k -> new CopyOnWriteArrayList<>())
                .add(deadline);

        // Planifier la vérification
        scheduler.schedule(
                () -> verifySingleDeadline(deadline),
                deadlineMs,
                TimeUnit.MILLISECONDS
        );

        return true;
    }

    /**
     * Vérifie une deadline spécifique.
     */
    private void verifySingleDeadline(Deadline deadline) {
        if (!deadline.isMet() && deadline.isExpired()) {
            handleMissedDeadline(deadline);
        }
    }

    /**
     * Vérifie toutes les deadlines actives.
     */
    private void checkAllDeadlines() {
        activeDeadlines.forEach((topicName, deadlines) -> {
            deadlines.removeIf(deadline -> {
                if (deadline.isExpired() && !deadline.isMet()) {
                    handleMissedDeadline(deadline);
                    return true;
                }
                return deadline.isMet();
            });
        });
    }

    /**
     * Gère une deadline manquée.
     */
    private void handleMissedDeadline(Deadline deadline) {
        String topicName = deadline.getTopicName();

        // Statistiques
        missedDeadlines.merge(topicName, 1L, Long::sum);

        // Log
        logger.error("⏰ DEADLINE MANQUÉE: topic='{}', message={}, dépassement={}ms",
                topicName,
                deadline.getMessage().getId().substring(0, 8),
                deadline.getOvertimeMs());

        // Notification (pourrait être envoyée aux subscribers concernés)
        System.err.printf("[DEADLINE MISSED] Topic: %s, Message: %s, Overtime: %dms%n",
                topicName,
                deadline.getMessage().getId(),
                deadline.getOvertimeMs());
    }

    /**
     * Marque une deadline comme respectée.
     */
    public void markDeadlineAsMet(String deadlineId) {
        activeDeadlines.values().forEach(deadlines -> {
            deadlines.removeIf(d -> {
                if (d.getDeadlineId().equals(deadlineId)) {
                    d.markAsMet();
                    return true;
                }
                return false;
            });
        });
    }

    /**
     * Récupère les statistiques des deadlines manquées.
     */
    public Map<String, Long> getMissedDeadlinesStats() {
        return new HashMap<>(missedDeadlines);
    }

    /**
     * Arrête le moniteur.
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.info("DeadlineMonitor arrêté");
    }
}
package fr.telecom.middleware.realtime;

import java.util.concurrent.*;
import org.slf4j.*;

/**
 * Planificateur pour les tâches temps-réel.
 */
public class Scheduler {
    private static final Logger logger = LoggerFactory.getLogger(Scheduler.class);

    private final ScheduledExecutorService realtimeScheduler;
    private final ScheduledExecutorService backgroundScheduler;

    public Scheduler() {
        // Scheduler temps-réel avec priorité élevée
        this.realtimeScheduler = Executors.newScheduledThreadPool(
                2,
                r -> {
                    Thread t = new Thread(r);
                    t.setPriority(Thread.MAX_PRIORITY);
                    t.setName("Realtime-Scheduler-" + t.getId());
                    return t;
                }
        );

        // Scheduler pour tâches de fond
        this.backgroundScheduler = Executors.newScheduledThreadPool(2);

        logger.info("Scheduler temps-réel initialisé");
    }

    /**
     * Planifie une tâche temps-réel.
     */
    public ScheduledFuture<?> scheduleRealtimeTask(Runnable task, long delay, TimeUnit unit) {
        return realtimeScheduler.schedule(() -> {
            long startTime = System.nanoTime();
            try {
                task.run();
            } catch (Exception e) {
                logger.error("Erreur dans la tâche temps-réel: {}", e.getMessage());
            }
            long duration = System.nanoTime() - startTime;
            logger.debug("Tâche temps-réel exécutée en {} ns", duration);
        }, delay, unit);
    }

    /**
     * Planifie une tâche périodique temps-réel.
     */
    public ScheduledFuture<?> scheduleRealtimeAtFixedRate(Runnable task,
                                                          long initialDelay,
                                                          long period,
                                                          TimeUnit unit) {
        return realtimeScheduler.scheduleAtFixedRate(() -> {
            try {
                task.run();
            } catch (Exception e) {
                logger.error("Erreur dans la tâche périodique temps-réel: {}", e.getMessage());
            }
        }, initialDelay, period, unit);
    }

    /**
     * Planifie une tâche de fond.
     */
    public ScheduledFuture<?> scheduleBackgroundTask(Runnable task, long delay, TimeUnit unit) {
        return backgroundScheduler.schedule(() -> {
            try {
                task.run();
            } catch (Exception e) {
                logger.error("Erreur dans la tâche de fond: {}", e.getMessage());
            }
        }, delay, unit);
    }

    /**
     * Exécute une tâche de façon asynchrone.
     */
    public CompletableFuture<Void> executeAsync(Runnable task) {
        return CompletableFuture.runAsync(task, realtimeScheduler);
    }

    /**
     * Arrête les schedulers.
     */
    public void shutdown() {
        realtimeScheduler.shutdown();
        backgroundScheduler.shutdown();

        try {
            if (!realtimeScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                realtimeScheduler.shutdownNow();
            }
            if (!backgroundScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                backgroundScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            realtimeScheduler.shutdownNow();
            backgroundScheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }

        logger.info("Scheduler arrêté");
    }

    /**
     * Vérifie si le scheduler est actif.
     */
    public boolean isRunning() {
        return !realtimeScheduler.isShutdown() && !backgroundScheduler.isShutdown();
    }
}
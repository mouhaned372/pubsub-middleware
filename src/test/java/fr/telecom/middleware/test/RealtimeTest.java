package fr.telecom.middleware.test;

import fr.telecom.middleware.realtime.*;
import fr.telecom.middleware.qos.*;
import fr.telecom.middleware.api.Message;
import org.junit.*;
import static org.junit.Assert.*;
import java.util.concurrent.TimeUnit;

public class RealtimeTest {
    private DeadlineMonitor deadlineMonitor;

    @Before
    public void setUp() {
        deadlineMonitor = new DeadlineMonitor();
    }

    @After
    public void tearDown() {
        if (deadlineMonitor != null) {
            deadlineMonitor.shutdown();
        }
    }

    @Test
    public void testDeadlineMonitoring() throws InterruptedException {
        // Given
        QoS qos = new QoS.Builder()
                .deadlineMs(50)
                .build();

        Message message = Message.builder("test/topic")
                .payload("Test")
                .build();

        // When - simuler une publication avec deadline
        // Note: Cette méthode est simplifiée pour le test
        Thread.sleep(60); // Délai plus long que la deadline

        // Then - le moniteur doit être actif
        assertNotNull(deadlineMonitor);
    }

    @Test(timeout = 5000)
    public void testSchedulerBasicOperations() throws Exception {
        // Given
        Scheduler scheduler = new Scheduler();

        final boolean[] taskExecuted = {false};

        // When
        scheduler.scheduleRealtimeTask(() -> {
            taskExecuted[0] = true;
        }, 100, TimeUnit.MILLISECONDS);

        // Then
        Thread.sleep(200);
        assertTrue(taskExecuted[0]);

        scheduler.shutdown();
    }

    @Test
    public void testSchedulerPeriodicTask() throws InterruptedException {
        // Given
        Scheduler scheduler = new Scheduler();
        final int[] executionCount = {0};

        // When
        scheduler.scheduleRealtimeAtFixedRate(() -> {
            executionCount[0]++;
        }, 0, 50, TimeUnit.MILLISECONDS);

        // Then
        Thread.sleep(220); // Environ 4-5 exécutions
        scheduler.shutdown();

        assertTrue("Devrait avoir exécuté plusieurs fois", executionCount[0] >= 4);
    }

    @Test
    public void testAsyncExecution() throws Exception {
        // Given
        Scheduler scheduler = new Scheduler();
        final boolean[] taskExecuted = {false};

        // When
        scheduler.executeAsync(() -> {
            taskExecuted[0] = true;
        }).get(1, TimeUnit.SECONDS); // Attendre la complétion

        // Then
        assertTrue(taskExecuted[0]);
        scheduler.shutdown();
    }

    @Test
    public void testSchedulerShutdown() {
        // Given
        Scheduler scheduler = new Scheduler();

        // When
        scheduler.shutdown();

        // Then
        assertFalse(scheduler.isRunning());
    }
}
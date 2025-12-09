package fr.telecom.middleware.test;

import fr.telecom.middleware.qos.*;
import org.junit.*;
import static org.junit.Assert.*;

public class QoSTest {

    @Test
    public void testQoSBuilder() {
        // When
        QoS qos = new QoS.Builder()
                .reliability(QoS.Reliability.EXACTLY_ONCE)
                .priority(QoS.Priority.CRITICAL)
                .deadlineMs(50)
                .maxLatencyMs(100)
                .redundancyLevel(2)
                .persistence(true)
                .build();

        // Then
        assertEquals(QoS.Reliability.EXACTLY_ONCE, qos.getReliability());
        assertEquals(QoS.Priority.CRITICAL, qos.getPriority());
        assertEquals(50, qos.getDeadlineMs());
        assertEquals(100, qos.getMaxLatencyMs());
        assertEquals(2, qos.getRedundancyLevel());
        assertTrue(qos.isPersistence());
    }

    @Test
    public void testQoSDefaults() {
        // When
        QoS qos = new QoS();

        // Then
        assertEquals(QoS.Reliability.BEST_EFFORT, qos.getReliability());
        assertEquals(QoS.Priority.MEDIUM, qos.getPriority());
        assertEquals(0, qos.getDeadlineMs());
        assertEquals(1000, qos.getMaxLatencyMs());
        assertEquals(0, qos.getRedundancyLevel());
        assertFalse(qos.isPersistence());
    }

    @Test
    public void testQoSMethods() {
        // Given
        QoS qos1 = new QoS.Builder()
                .deadlineMs(100)
                .build();

        QoS qos2 = new QoS.Builder()
                .reliability(QoS.Reliability.BEST_EFFORT)
                .build();

        QoS qos3 = new QoS.Builder()
                .priority(QoS.Priority.CRITICAL)
                .build();

        // Then
        assertTrue(qos1.hasDeadline());
        assertFalse(qos2.isReliable());
        assertTrue(qos3.isCritical());
    }

    @Test
    public void testDeadlineCreation() {
        // Given
        String topicName = "test/topic";
        String messageId = "test-message";

        // When
        Deadline deadline = new Deadline(topicName, null, 100);

        // Then
        assertNotNull(deadline.getDeadlineId());
        assertEquals(topicName, deadline.getTopicName());
        assertTrue(deadline.getRemainingTimeMs() > 0);
        assertFalse(deadline.isExpired());
        assertFalse(deadline.isMet());
    }

    @Test
    public void testDeadlineExpiration() throws InterruptedException {
        // Given
        Deadline deadline = new Deadline("test", null, 10); // 10ms deadline

        // When
        Thread.sleep(20); // Attendre plus que la deadline

        // Then
        assertTrue(deadline.isExpired());
        assertTrue(deadline.getOvertimeMs() > 0);
    }

    @Test
    public void testDeadlineMet() {
        // Given
        Deadline deadline = new Deadline("test", null, 100);

        // When
        deadline.markAsMet();

        // Then
        assertTrue(deadline.isMet());
        assertFalse(deadline.isExpired());
    }

    @Test
    public void testPriorityValues() {
        assertEquals(1, QoS.Priority.LOW.getValue());
        assertEquals(2, QoS.Priority.MEDIUM.getValue());
        assertEquals(3, QoS.Priority.HIGH.getValue());
        assertEquals(4, QoS.Priority.CRITICAL.getValue());
    }
}
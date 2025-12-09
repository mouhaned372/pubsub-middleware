package fr.telecom.middleware.test;

import fr.telecom.middleware.core.*;
import fr.telecom.middleware.qos.*;
import fr.telecom.middleware.api.Message;
import org.junit.*;
import static org.junit.Assert.*;

public class MiddlewareTest {
    private Middleware middleware;

    @Before
    public void setUp() {
        middleware = new Middleware();
        middleware.setMaxMemoryKB(256);
        middleware.setRealtimeEnabled(false); // Désactiver temps-réel pour tests
    }

    @After
    public void tearDown() {
        if (middleware != null) {
            middleware.shutdown();
        }
    }

    @Test
    public void testTopicCreation() {
        // Given
        String topicName = "test/topic";
        QoS qos = new QoS.Builder()
                .reliability(QoS.Reliability.AT_LEAST_ONCE)
                .priority(QoS.Priority.HIGH)
                .deadlineMs(100)
                .build();

        // When
        Topic topic = middleware.createTopic(topicName, qos);

        // Then
        assertNotNull(topic);
        assertEquals(topicName, topic.getName());
        assertEquals(QoS.Reliability.AT_LEAST_ONCE, topic.getQoS().getReliability());
        assertTrue(topic.getQoS().hasDeadline());
        assertEquals(100, topic.getQoS().getDeadlineMs());
    }

    @Test
    public void testPublishSubscribe() throws InterruptedException {
        // Given
        String topicName = "test/data";
        QoS qos = new QoS.Builder().build();
        middleware.createTopic(topicName, qos);

        TestSubscriber subscriber = new TestSubscriber();
        middleware.subscribe(topicName, subscriber);

        Message message = Message.builder(topicName)
                .payload("Hello World")
                .build();

        // When
        middleware.publish(topicName, message);

        // Wait for delivery
        Thread.sleep(100);

        // Then
        assertEquals(1, subscriber.getMessageCount());
        assertEquals(message, subscriber.getLastMessage());
    }

    @Test
    public void testMultipleSubscribers() throws InterruptedException {
        // Given
        String topicName = "test/multi";
        middleware.createTopic(topicName, new QoS.Builder().build());

        TestSubscriber sub1 = new TestSubscriber();
        TestSubscriber sub2 = new TestSubscriber();
        TestSubscriber sub3 = new TestSubscriber();

        middleware.subscribe(topicName, sub1);
        middleware.subscribe(topicName, sub2);
        middleware.subscribe(topicName, sub3);

        Message message = Message.builder(topicName)
                .payload("Test")
                .build();

        // When
        middleware.publish(topicName, message);

        // Wait for delivery
        Thread.sleep(150);

        // Then
        assertEquals(1, sub1.getMessageCount());
        assertEquals(1, sub2.getMessageCount());
        assertEquals(1, sub3.getMessageCount());
    }

    @Test
    public void testQoSReliability() throws InterruptedException {
        // Given
        String topicName = "test/reliable";
        QoS qos = new QoS.Builder()
                .reliability(QoS.Reliability.EXACTLY_ONCE)
                .build();

        middleware.createTopic(topicName, qos);

        TestSubscriber subscriber = new TestSubscriber();
        middleware.subscribe(topicName, subscriber);

        // When
        for (int i = 0; i < 5; i++) {
            Message msg = Message.builder(topicName)
                    .payload("Message " + i)
                    .build();
            middleware.publish(topicName, msg);
            Thread.sleep(50);
        }

        // Wait for all deliveries
        Thread.sleep(200);

        // Then
        assertEquals(5, subscriber.getMessageCount());
    }

    @Test
    public void testTopicRetrieval() {
        // Given
        String topicName = "test/retrieve";
        QoS qos = new QoS.Builder().build();

        // When
        middleware.createTopic(topicName, qos);
        Topic retrieved = middleware.getTopic(topicName);

        // Then
        assertNotNull(retrieved);
        assertEquals(topicName, retrieved.getName());
    }

    @Test
    public void testUnsubscribe() throws InterruptedException {
        // Given
        String topicName = "test/unsub";
        middleware.createTopic(topicName, new QoS.Builder().build());

        TestSubscriber subscriber = new TestSubscriber();
        middleware.subscribe(topicName, subscriber);

        // When - publish first message
        Message msg1 = Message.builder(topicName).payload("Msg1").build();
        middleware.publish(topicName, msg1);
        Thread.sleep(50);

        // Then - should receive
        assertEquals(1, subscriber.getMessageCount());

        // When - unsubscribe and publish second message
        middleware.unsubscribe(topicName, subscriber);
        Message msg2 = Message.builder(topicName).payload("Msg2").build();
        middleware.publish(topicName, msg2);
        Thread.sleep(50);

        // Then - should NOT receive second message
        assertEquals(1, subscriber.getMessageCount());
    }

    @Test
    public void testMemoryLimit() {
        // Given
        middleware.setMaxMemoryKB(1); // Très faible limite

        String topicName = "test/memory";
        middleware.createTopic(topicName, new QoS.Builder().build());

        // When - publier plusieurs messages
        for (int i = 0; i < 10; i++) {
            Message msg = Message.builder(topicName)
                    .payload("Large payload " + i + " ".repeat(1000))
                    .build();
            middleware.publish(topicName, msg);
        }

        // Then - ne doit pas crasher
        assertTrue(true); // Si on arrive ici, c'est bon
    }

    // Subscriber de test pour les tests unitaires
    private static class TestSubscriber implements Subscriber {
        private Message lastMessage;
        private int messageCount = 0;

        @Override
        public void onMessage(Message message) {
            this.lastMessage = message;
            this.messageCount++;
        }

        public Message getLastMessage() {
            return lastMessage;
        }

        public int getMessageCount() {
            return messageCount;
        }
    }
}
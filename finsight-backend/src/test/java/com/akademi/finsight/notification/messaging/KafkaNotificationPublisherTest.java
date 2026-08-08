package com.akademi.finsight.notification.messaging;

import com.akademi.finsight.notification.exception.NotificationPublishException;
import com.akademi.finsight.notification.model.NotificationRequestedEvent;
import com.akademi.finsight.notification.support.NotificationFixtures;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaNotificationPublisherTest {

    @Mock
    private KafkaTemplate<String, NotificationRequestedEvent> kafkaTemplate;

    private MeterRegistry meterRegistry;
    private KafkaNotificationPublisher publisher;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        publisher = new KafkaNotificationPublisher(kafkaTemplate, NotificationFixtures.properties(), meterRegistry);
    }

    // ──────────────────────────────────────────────────────────────
    // publish
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("publish")
    class Publish {

        /** Ayni kullanicinin bildirimleri sirali kalsin diye partition key e-postadir. */
        @Test
        @DisplayName("should send to configured topic keyed by email")
        void publish_shouldSendToConfiguredTopicKeyedByEmail() {
            NotificationRequestedEvent event = NotificationFixtures.event();
            stubSend(CompletableFuture.completedFuture(null));

            publisher.publish(event);

            verify(kafkaTemplate).send(NotificationFixtures.TOPIC, NotificationFixtures.EMAIL, event);
        }

        /** Ack beklenmedigi icin broker hatasi cagirana yansimamali, yalnizca metrige dusmeli. */
        @Test
        @DisplayName("should count async failure without throwing to caller")
        void publish_shouldCountAsyncFailureWithoutThrowing() {
            NotificationRequestedEvent event = NotificationFixtures.event();
            CompletableFuture<SendResult<String, NotificationRequestedEvent>> failed = new CompletableFuture<>();
            failed.completeExceptionally(new IllegalStateException("broker erisilemez"));
            stubSend(failed);

            assertDoesNotThrow(() -> publisher.publish(event));

            assertEquals(1.0, meterRegistry.get("notification.publish.failed")
                    .tag("type", "VERIFICATION_EMAIL").counter().count());
        }

        @Test
        @DisplayName("should wrap synchronous failure in NotificationPublishException")
        void publish_shouldWrapSynchronousFailure() {
            NotificationRequestedEvent event = NotificationFixtures.event();
            when(kafkaTemplate.send(anyString(), anyString(), any(NotificationRequestedEvent.class)))
                    .thenThrow(new IllegalStateException("producer kapali"));

            NotificationPublishException exception = assertThrows(
                    NotificationPublishException.class, () -> publisher.publish(event));

            assertInstanceOf(IllegalStateException.class, exception.getCause());
        }
    }

    // ──────────────────────────────────────────────────────────────
    // helpers
    // ──────────────────────────────────────────────────────────────

    private void stubSend(CompletableFuture<SendResult<String, NotificationRequestedEvent>> result) {
        when(kafkaTemplate.send(anyString(), anyString(), any(NotificationRequestedEvent.class)))
                .thenReturn(result);
    }
}

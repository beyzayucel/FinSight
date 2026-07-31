package com.akademi.finsight.notification.config;

import com.akademi.finsight.common.masking.MaskType;
import com.akademi.finsight.notification.exception.InvalidNotificationException;
import com.akademi.finsight.notification.messaging.RawKafkaTemplate;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.errors.SerializationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

@Slf4j
@Configuration
public class KafkaConfiguration {

    @Bean
    NewTopic notificationTopic(NotificationProperties properties) {
        return TopicBuilder.name(properties.kafka().topic())
                .partitions(properties.kafka().partitions())
                .replicas(properties.kafka().replicas())
                .build();
    }

    /** DLT saklama suresi acikca verilir, broker varsayilaninda (7 gun) zehirli kayitlar sessizce silinirdi. */
    @Bean
    NewTopic notificationDeadLetterTopic(NotificationProperties properties) {
        return TopicBuilder.name(properties.kafka().topic() + properties.kafka().deadLetterSuffix())
                .partitions(properties.kafka().partitions())
                .replicas(properties.kafka().replicas())
                .config(TopicConfig.RETENTION_MS_CONFIG,
                        String.valueOf(properties.kafka().deadLetterRetention().toMillis()))
                .build();
    }

    @Bean
    RawKafkaTemplate rawKafkaTemplate(ProducerFactory<Object, Object> producerFactory) {
        return new RawKafkaTemplate(producerFactory);
    }

    @Bean
    DefaultErrorHandler kafkaErrorHandler(
            RawKafkaTemplate rawKafkaTemplate,
            MeterRegistry meterRegistry,
            NotificationProperties properties
    ) {
        String deadLetterSuffix = properties.kafka().deadLetterSuffix();
        DeadLetterPublishingRecoverer deadLetterRecoverer = new DeadLetterPublishingRecoverer(
                rawKafkaTemplate.template(),
                (record, exception) -> new TopicPartition(record.topic() + deadLetterSuffix, record.partition())
        );

        ConsumerRecordRecoverer recoverer = (record, exception) -> {
            Throwable cause = exception.getCause() == null ? exception : exception.getCause();
            log.error("Failed to process notification, moving to DLT: topic={} partition={} offset={} key={} reason={}",
                    record.topic(), record.partition(), record.offset(), MaskType.mask(MaskType.FULL, String.valueOf(record.key())),
                    cause.getClass().getSimpleName(), exception);
            meterRegistry.counter("notification.dead_letter",
                    "topic", record.topic(),
                    "reason", cause.getClass().getSimpleName()).increment();
            deadLetterRecoverer.accept(record, exception);
        };

        NotificationProperties.Kafka.Retry retry = properties.kafka().retry();
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(retry.maxAttempts());
        backOff.setInitialInterval(retry.initialInterval().toMillis());
        backOff.setMultiplier(retry.multiplier());
        backOff.setMaxInterval(retry.maxInterval().toMillis());

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);
        handler.addNotRetryableExceptions(InvalidNotificationException.class, SerializationException.class);
        return handler;
    }
}

package com.akademi.finsight.notification.config;

import com.akademi.finsight.notification.messaging.RawKafkaTemplate;
import com.akademi.finsight.notification.service.InvalidNotificationException;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.errors.SerializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

@Configuration
public class KafkaConfiguration {

    private static final Logger log = LoggerFactory.getLogger(KafkaConfiguration.class);

    @Bean
    NewTopic notificationTopic(NotificationProperties properties) {
        return TopicBuilder.name(properties.kafka().topic())
                .partitions(3)
                .replicas(1)
                .build();
    }

    /** DLT saklama suresi acikca verilir, broker varsayilaninda (7 gun) zehirli kayitlar sessizce silinirdi. */
    @Bean
    NewTopic notificationDeadLetterTopic(NotificationProperties properties) {
        return TopicBuilder.name(properties.kafka().topic() + ".DLT")
                .partitions(3)
                .replicas(1)
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
            MeterRegistry meterRegistry
    ) {
        DeadLetterPublishingRecoverer deadLetterRecoverer = new DeadLetterPublishingRecoverer(
                rawKafkaTemplate.template(),
                (record, exception) -> new TopicPartition(record.topic() + ".DLT", record.partition())
        );

        ConsumerRecordRecoverer recoverer = (record, exception) -> {
            Throwable cause = exception.getCause() == null ? exception : exception.getCause();
            log.error("Bildirim islenemedi, DLT'ye tasiniyor: topic={} partition={} offset={} key={} sebep={}",
                    record.topic(), record.partition(), record.offset(), record.key(),
                    cause.getClass().getSimpleName(), exception);
            meterRegistry.counter("notification.dead_letter",
                    "topic", record.topic(),
                    "reason", cause.getClass().getSimpleName()).increment();
            deadLetterRecoverer.accept(record, exception);
        };

        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(3);
        backOff.setInitialInterval(1_000L);
        backOff.setMultiplier(2);
        backOff.setMaxInterval(4_000L);

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);
        handler.addNotRetryableExceptions(InvalidNotificationException.class, SerializationException.class);
        return handler;
    }
}

package com.akademi.finsight.notification.messaging;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.DelegatingByTypeSerializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/** Ham deger yazabilen producer (DLT icin); bean tipi Boot'un varsayilan KafkaTemplate'iyle catismasin diye sarmalanmis. */
public class RawKafkaTemplate implements DisposableBean {

    private final DefaultKafkaProducerFactory<Object, Object> producerFactory;
    private final KafkaTemplate<Object, Object> template;

    public RawKafkaTemplate(ProducerFactory<Object, Object> source) {
        Map<String, Object> configs = new HashMap<>(source.getConfigurationProperties());
        configs.remove(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG);
        configs.remove(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG);

        this.producerFactory = new DefaultKafkaProducerFactory<>(
                configs, byTypeSerializer(), byTypeSerializer());
        this.template = new KafkaTemplate<>(this.producerFactory);
    }

    public KafkaTemplate<Object, Object> template() {
        return this.template;
    }

    private static Serializer<Object> byTypeSerializer() {
        Map<Class<?>, Serializer<?>> delegates = new LinkedHashMap<>();
        delegates.put(String.class, new StringSerializer());
        delegates.put(Object.class, new JacksonJsonSerializer<>());
        return new DelegatingByTypeSerializer(delegates, true);
    }

    @Override
    public void destroy() {
        this.producerFactory.destroy();
    }
}

package com.ars.inventory.config;

import com.ars.contract.messaging.events.OrderConfirmedEvent;
import com.ars.contract.messaging.events.OrderConfirmedPartitionedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:29092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:inventory-dev-1}")
    private String groupId;//TODO BURDA İKİ BEAN ICIN TANIMLAMA YAPILMIŞ SIKINTI OLUR MU ?

    /**
     * ORDER EVENT consumer factory (JSON -> POJO)
     * Bu bean sadece order listener için kullanılacak.
     */
    @Bean("orderConsumerFactory")
    public ConsumerFactory<String, OrderConfirmedEvent> orderConsumerFactory() {
        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        // ErrorHandlingDeserializer wrapper
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);

        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);

        // JsonDeserializer config
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.ars.*");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, OrderConfirmedEvent.class.getName());

        // Type header kullanmıyorsan false daha stabil olur (özellikle farklı servisler arası)
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean("orderListenerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, OrderConfirmedEvent> orderListenerFactory(
            @Value("#{orderConsumerFactory}") ConsumerFactory<String, OrderConfirmedEvent> cf) {

        var f = new ConcurrentKafkaListenerContainerFactory<String, OrderConfirmedEvent>();
        f.setConsumerFactory(cf);

        // Yoğun trafikte partition sayısına göre yükseltebilirsin
        // f.setConcurrency(3);

        f.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        // Startup'ta ölmesin, transient hatalarda retry yapsın
        f.setCommonErrorHandler(new DefaultErrorHandler(new FixedBackOff(1000L, 3)));

        return f;
    }

    @Bean("partitionedOrderConsumerFactory")
    public ConsumerFactory<String, OrderConfirmedPartitionedEvent> partitionedOrderConsumerFactory() {
        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);

        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);

        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.ars.*");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, OrderConfirmedPartitionedEvent.class.getName());
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean("partitionedOrderListenerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, OrderConfirmedPartitionedEvent> partitionedOrderListenerFactory(
            @Value("#{partitionedOrderConsumerFactory}") ConsumerFactory<String, OrderConfirmedPartitionedEvent> cf) {
        var f = new ConcurrentKafkaListenerContainerFactory<String, OrderConfirmedPartitionedEvent>();
        f.setConsumerFactory(cf);
        f.setConcurrency(3);
        f.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        f.setCommonErrorHandler(new DefaultErrorHandler(new FixedBackOff(1000L, 3)));
        return f;
    }

}

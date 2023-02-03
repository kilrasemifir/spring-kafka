package com.semifir.formation.kafka.worldcount.consumer.bills;

import com.semifir.formation.kafka.worldcount.consumer.MessageInfo;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class BillConsumerConfiguration {
    @Bean
    public ConsumerFactory<String, Bill> billConsumerFactory() {
        Map<String, Object> config = new HashMap<>();

        JsonDeserializer<Bill> deserializer = new JsonDeserializer<>(Bill.class);
        deserializer.ignoreTypeHeaders();

        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "group_id");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializer);

        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Bill> billKafkaListenerContainerFactory(
            ConsumerFactory<String, Bill> billConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, Bill> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(billConsumerFactory);
        return factory;
    }
}

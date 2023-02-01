package com.semifir.formation.kafka.worldcount.publisher;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Classe de configuration des publishers Kafka
 */
@Configuration
public class KafkaConfiguration {

    /**
     * Bean de configuration du ProducerFactory pour les messages de type String
     * Il permet de configurer la connexion au serveur Kafka et la sérialisation/désérialisation des messages
     * Dans notre cas nous utilisons la classe StringSerializer pour la sérialisation
     * @return le ProducerFactory configurer pour sérialiser les messages de type String
     */
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "localhost:9092");
        configProps.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        configProps.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * Bean de configuration du ProducerFactory pour les messages de type Message.
     * Il permet de configurer la connexion au serveur Kafka et la sérialisation/désérialisation des messages
     * Dans notre cas nous utilisons la classe JsonSerializer pour la sérialisation
     * @return le ProducerFactory configurer pour sérialiser les messages de type Message
     */
    @Bean
    public ProducerFactory<String, Message> messageProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "localhost:9092");
        configProps.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        configProps.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * Bean de configuration du KafkaTemplate pour les messages de type String
     * Il prend en paramètre le ProducerFactory de String
     * @return le KafkaTemplate configuré pour envoyer des messages de type String
     */
    @Bean
    public KafkaTemplate<String, String> kafkaStringTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
    /**
     * Bean de configuration du KafkaTemplate pour les messages de type Message
     * Il prend en paramètre le ProducerFactory de Message
     * @return le KafkaTemplate configuré pour envoyer des messages de type Message
     */
    @Bean
    public KafkaTemplate<String, Message> kafkaMessageTemplate() {
        return new KafkaTemplate<>(messageProducerFactory());
    }
}

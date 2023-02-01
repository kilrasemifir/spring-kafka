package com.semifir.formation.kafka.worldcount.publisher;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class SimplePublisher {

    /**
     * KafkaTemplate permet d'envoyer des messages sur un topic
     */
    private final KafkaTemplate<String, String> kafkaTemplate;

    public SimplePublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publie un message dans le topic "simple"
     * @param message le message à publier
     */
    public void sendMessage(String message) {
        kafkaTemplate.send("simple", message);
    }

    /**
     * Publie un message dans le topic "text"
     * @param message le message à publier
     */
    public void sendMessageToTextTopic(String message) {
        kafkaTemplate.send("text", message);
    }



}

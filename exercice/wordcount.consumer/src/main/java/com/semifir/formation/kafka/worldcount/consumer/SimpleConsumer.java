package com.semifir.formation.kafka.worldcount.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class SimpleConsumer {

    /**
     * Cette méthode est appelée à chaque fois qu'un message est reçu sur le topic "simple"
     * @param message le message reçu
     */
    @KafkaListener(topics = "simple", groupId = "simple-consumer")
    public void consume(String message) {
        System.out.println("Message reçu: " + message);
    }

    @KafkaListener(topics = "words", groupId = "text-consumer")
    public void consumeWords(
            @Header(KafkaHeaders.RECEIVED_KEY) String word,
            @Payload String count) {
        System.out.println(word + " (" + count + ")");
    }

    @KafkaListener(
            topics = "infos",
            groupId = "object-consumer",
            containerFactory = "messageKafkaListenerContainerFactory")
    public void consumeInfos(MessageInfo messageInfo) {
        System.out.println(messageInfo);
    }
}

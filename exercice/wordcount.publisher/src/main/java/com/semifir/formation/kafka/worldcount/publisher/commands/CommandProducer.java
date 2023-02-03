package com.semifir.formation.kafka.worldcount.publisher.commands;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class CommandProducer {

    private final KafkaTemplate<String, Command> kafkaTemplate;

    public CommandProducer(KafkaTemplate<String, Command> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendCommand(Command command) {
        kafkaTemplate.send("command", command);
    }
}

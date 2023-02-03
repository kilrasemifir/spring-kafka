package com.semifir.formation.kafka.worldcount.publisher.persons;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PersonProducer {

    private final KafkaTemplate<String, Person> kafkaTemplate;

    public PersonProducer(KafkaTemplate<String, Person> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendPerson(Person person) {
        kafkaTemplate.send("person", person);
    }
}

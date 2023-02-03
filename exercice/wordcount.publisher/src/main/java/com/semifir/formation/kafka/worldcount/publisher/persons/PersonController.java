package com.semifir.formation.kafka.worldcount.publisher.persons;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/persons")
public class PersonController {

        private final PersonProducer personProducer;

        public PersonController(PersonProducer personProducer) {
            this.personProducer = personProducer;
        }

        @PostMapping
        public void publishPerson(@RequestBody Person person) {
            personProducer.sendPerson(person);
        }
}

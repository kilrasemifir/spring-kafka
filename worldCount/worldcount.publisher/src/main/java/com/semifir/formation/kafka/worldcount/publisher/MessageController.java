package com.semifir.formation.kafka.worldcount.publisher;

import org.springframework.web.bind.annotation.*;

@RestController
public class MessageController {

    private final SimplePublisher simplePublisherService;

    public MessageController(SimplePublisher simplePublisherService) {
        this.simplePublisherService = simplePublisherService;
    }

    @PostMapping("/message")
    public void publishSimpleMessage(@RequestBody String message) {
        simplePublisherService.sendMessage(message);
    }

    @PostMapping("/text")
    public void publishTextMessage(@RequestBody String message) {
        simplePublisherService.sendMessageToTextTopic(message);
    }

    @PostMapping("/object")
    public void publishObjectMessage(@RequestBody Message message) {
        simplePublisherService.sendObject(message);
    }
}

package com.semifir.formation.kafka.worldcount.publisher.commands;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/commands")
public class CommandController {

    private final CommandProducer commandProducer;

    public CommandController(CommandProducer commandProducer) {
        this.commandProducer = commandProducer;
    }

    @PostMapping
    public void publishCommand(@RequestBody Command command) {
        commandProducer.sendCommand(command);
    }
}

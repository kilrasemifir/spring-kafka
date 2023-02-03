package com.semifir.formation.kafka.worldcount.consumer.bills;

import java.util.HashSet;
import java.util.Set;

public class Bill {
    private Person person;
    private Set<Command> commands = new HashSet<>();

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public Set<Command> getCommands() {
        return commands;
    }

    public void setCommands(Set<Command> commands) {
        this.commands = commands;
    }
}
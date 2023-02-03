package com.semifir.formation.kafka.worldcount.stream.bill;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;

@Component
public class BillProcessor {

    @Autowired
    public void billPipeline(StreamsBuilder builder) {
        JsonSerde<Person> personSerde = new JsonSerde<>(Person.class);
        personSerde.ignoreTypeHeaders();

        JsonSerde<Command> commandSerde = new JsonSerde<>(Command.class);
        commandSerde.ignoreTypeHeaders();

        JsonSerde<Bill> billSerde = new JsonSerde<>(Bill.class);

        KStream<String, Person> personStream = builder.stream("person", Consumed.with(Serdes.String(), personSerde));
        KStream<String, Command> commandStream = builder.stream("command", Consumed.with(Serdes.String(), commandSerde));

        personStream = personStream.mapValues(person -> {
                    System.out.println(person);
                    return person;
                });

        KTable<String, Person> personTable = personStream.toTable();
        KTable<String, Command> commandTable = commandStream.toTable();

        ValueJoiner<Person, Command, Bill> joiner = (person, command) -> {
            System.out.println("Person: " + person + " Command: " + command);
            if (person.getId().equals(command.getPersonId())) {
                Bill bill = new Bill();
                bill.setPerson(person);
                bill.getCommands().add(command);
                return bill;
            }
            return null;
        };

        KTable<String, Bill> billTable = personTable.leftJoin(commandTable, joiner);
        KGroupedTable<String, Bill> groupedTable = billTable.groupBy((key, bill) -> {
            return KeyValue.pair(""+bill.getPerson().getId(), bill);
        });

        Reducer<Bill> add = (bill1, bill2) -> {
            bill1.getCommands().addAll(bill2.getCommands());
            return bill1;
        };

        Reducer<Bill> remove = (bill1, bill2) -> {
            bill1.getCommands().removeAll(bill2.getCommands());
            return bill1;
        };

        KTable<String, Bill> groupedBill = groupedTable.reduce(add, remove);


        groupedBill.toStream().to("bill", Produced.with(Serdes.String(), billSerde));
    }
}

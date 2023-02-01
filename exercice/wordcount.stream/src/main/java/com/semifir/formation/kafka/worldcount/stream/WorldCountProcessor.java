package com.semifir.formation.kafka.worldcount.stream;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class WorldCountProcessor {

    @Autowired
    public void buildPipeline(StreamsBuilder builder) {

        KStream<String, String> stream = builder.stream("text");

        KTable<String, String> wordCounts = stream
                .flatMapValues(value -> Arrays.asList(value.toLowerCase().split("\\W+")))
                .mapValues(value -> {
                    System.out.println("value = " + value);
                    return value;
                })
                .groupBy((key, value) -> value)
                .count()
                .mapValues((key, value) -> {
                    return ""+value;
                });

        wordCounts.toStream().to("words");
    }

    @Autowired
    public void messagePipeline(StreamsBuilder builder){
        JsonSerde<Message> messageSerde = new JsonSerde<>(Message.class);
        messageSerde.ignoreTypeHeaders();
        KStream<String, Message> stream = builder.stream("object", Consumed.with(Serdes.String(), messageSerde));
        stream.mapValues(value -> {
            MessageInfo messageInfo = new MessageInfo();
            messageInfo.setAuthor(value.getAuthor());
            messageInfo.setMessage(value.getMessage());
            messageInfo.setSize(value.getMessage().length());

            return messageInfo;
        }).to("infos", Produced.with(Serdes.String(), new JsonSerde<>(MessageInfo.class)));
    }

}

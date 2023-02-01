package com.semifir.formation.kafka.worldcount.stream;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Printed;
import org.springframework.beans.factory.annotation.Autowired;
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

}

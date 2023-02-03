package com.semifir.formation.kafka.worldcount.consumer.bills;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BillConsumer {

    @KafkaListener(topics = "bill", groupId = "bill-consumer", containerFactory = "billKafkaListenerContainerFactory")
    public void listen(Bill bill) {
        System.out.println(bill);
    }
}

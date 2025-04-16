package com.kk.cschat.kafka.producer;

import com.kk.cschat.kafka.dto.KafkaMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaMessageProducer {

    private final KafkaTemplate<String, KafkaMessage> kafkaTemplate;

    public void sendQuestion(KafkaMessage message) {
        log.info("[sendQuestion] Kafka: topic={}, payload={}", message.getTopic(), message);
        kafkaTemplate.send("cs-question", message);
    }

    public void sendAnswer(KafkaMessage message) {
        log.info("[sendAnswer] Kafka: topic={}, payload={}", message.getTopic(), message);
        kafkaTemplate.send("cs-answer", message);
    }
}

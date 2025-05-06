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
        kafkaTemplate.send("cs-question", message);
    }

    public void sendAnswer(KafkaMessage message) {
        kafkaTemplate.send("cs-answer", message);
    }

    public void sendAiQuestion(KafkaMessage message) {
        kafkaTemplate.send("cs-ai-question", message);
    }

    public void sendAiAnswer(KafkaMessage message) {
        kafkaTemplate.send("cs-ai-answer", message);
    }
}

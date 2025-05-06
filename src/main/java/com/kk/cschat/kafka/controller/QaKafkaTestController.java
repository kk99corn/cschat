package com.kk.cschat.kafka.controller;

import com.kk.cschat.kafka.dto.KafkaMessage;
import com.kk.cschat.kafka.dto.QuestionMessage;
import com.kk.cschat.kafka.producer.KafkaMessageProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class QaKafkaTestController {

    private final KafkaMessageProducer producer;

    @GetMapping("/cs/kafka")
    public ResponseEntity<String> sendToKafka(@RequestParam("q") String q) {
        KafkaMessage message = QuestionMessage.builder()
                                              .userId("test")
                                              .keyword(q)
                                              .rawQuestion(null)
                                              .build();
        producer.sendQuestion(message);
        return ResponseEntity.ok("Kafka 메시지 전송 완료: " + q);
    }

    @GetMapping("/ai/cs/kafka")
    public ResponseEntity<String> sendToKafkaAi(@RequestParam("q") String q) {
        KafkaMessage message = QuestionMessage.builder()
                                              .userId("test")
                                              .keyword(q)
                                              .rawQuestion(null)
                                              .build();
        producer.sendAiQuestion(message);
        return ResponseEntity.ok("Kafka 메시지 전송 완료: " + q);
    }
}

package com.kk.cschat.slack.controller;

import com.kk.cschat.kafka.dto.QuestionMessage;
import com.kk.cschat.kafka.producer.KafkaMessageProducer;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/slack")
@RequiredArgsConstructor
public class SlackController {

    private final KafkaMessageProducer producer;

    @PostMapping("/cs")
    public ResponseEntity<String> handleSlackCommand(@RequestParam Map<String, String> body) {
        String keyword = body.get("text");
        String responseUrl = body.get("response_url");
        String userId = body.get("user_id");

        QuestionMessage message = QuestionMessage.builder()
                                                 .userId(userId)
                                                 .keyword(keyword)
                                                 .responseUrl(responseUrl)
                                                 .build();
        producer.sendQuestion(message);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/ai-cs")
    public ResponseEntity<String> handleSlackAiCommand(@RequestParam Map<String, String> body) {
        String keyword = body.get("text");
        String responseUrl = body.get("response_url");
        String userId = body.get("user_id");

        QuestionMessage message = QuestionMessage.builder()
                                                 .userId(userId)
                                                 .keyword(keyword)
                                                 .responseUrl(responseUrl)
                                                 .build();
        producer.sendAiQuestion(message);

        return ResponseEntity.ok().build();
    }
}

package com.kk.cschat.kafka.consumer;

import com.kk.cschat.answer.service.QaService;
import com.kk.cschat.kafka.dto.AnswerMessage;
import com.kk.cschat.kafka.dto.KafkaMessage;
import com.kk.cschat.kafka.dto.QuestionMessage;
import com.kk.cschat.kafka.producer.KafkaMessageProducer;
import com.kk.cschat.kafka.util.KafkaSlackNotifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaAnswerConsumer {

    private final KafkaSlackNotifier notifier;

    @KafkaListener(topics = "cs-answer", groupId = "cs-chat-group")
    public void handleAnswer(AnswerMessage q) {
        log.info("[handleAnswer] 수신 - q: {}", q.toString());

        if (q.getResponseUrl() != null) {
            String reply = "*[" + q.getKeyword() + "]* → " + q.getAnswer();
            notifier.sendToSlack(q.getResponseUrl(), reply);
        }
    }
}

package com.kk.cschat.kafka.consumer;

import com.kk.cschat.answer.service.QaService;
import com.kk.cschat.kafka.dto.AnswerMessage;
import com.kk.cschat.kafka.dto.KafkaMessage;
import com.kk.cschat.kafka.dto.QuestionMessage;
import com.kk.cschat.kafka.producer.KafkaMessageProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaQuestionConsumer {

    private final QaService qaService;
    private final KafkaMessageProducer producer;

    @KafkaListener(topics = "cs-question", groupId = "cs-chat-group")
    public void handleQuestion(QuestionMessage q) {
        String answer = qaService.getAnswer(q.getKeyword());

        KafkaMessage message = AnswerMessage.builder()
                                            .userId(null)
                                            .keyword(q.getKeyword())
                                            .answer(answer)
                                            .responseUrl(q.getResponseUrl())
                                            .build();
        producer.sendAnswer(message);
    }
}

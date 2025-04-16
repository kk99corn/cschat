package com.kk.cschat.kafka.producer;

import static org.mockito.Mockito.verify;

import com.kk.cschat.kafka.dto.AnswerMessage;
import com.kk.cschat.kafka.dto.KafkaMessage;
import com.kk.cschat.kafka.dto.QuestionMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class KafkaMessageProducerTest {

    @Mock
    private KafkaTemplate<String, KafkaMessage> kafkaTemplate;

    @InjectMocks
    private KafkaMessageProducer producer;

    @Test
    void 질문전송() {
        // given
        QuestionMessage message = QuestionMessage.builder()
                                                 .userId("test")
                                                 .keyword("lru")
                                                 .rawQuestion("lru가 뭐야?")
                                                 .build();

        // when
        producer.sendQuestion(message);

        // then
        verify(kafkaTemplate).send("cs-question", message);
    }

    @Test
    void 응답전송() {
        AnswerMessage message = AnswerMessage.builder()
                                             .userId("test")
                                             .keyword("lru")
                                             .answer("Least Recently Used")
                                             .build();

        producer.sendAnswer(message);

        verify(kafkaTemplate).send("cs-answer", message);
    }
}
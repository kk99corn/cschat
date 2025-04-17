package com.kk.cschat.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class QuestionMessage implements KafkaMessage {

    private String userId;
    private String keyword;
    private String rawQuestion;
    private String responseUrl;

    @Override
    public String getTopic() {
        return "cs-question";
    }
}



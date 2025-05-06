package com.kk.cschat.kafka.util;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaSlackNotifier {

    private final WebClient webClient;

    public void sendToSlack(String responseUrl, String message) {
        webClient.post()
                 .uri(responseUrl)
                 .contentType(MediaType.APPLICATION_JSON)
                 .bodyValue(Map.of("text", message))
                 .retrieve()
                 .toBodilessEntity()
                 .doOnError(e -> log.error("slack 응답 실패", e))
                 .subscribe();
    }
}


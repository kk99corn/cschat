package com.kk.cschat.ai.service;

import com.kk.cschat.ai.dto.AiRequest;
import com.kk.cschat.ai.dto.AiResponse;
import com.kk.cschat.ai.dto.gemini.GeminiRequest;
import com.kk.cschat.ai.dto.gemini.GeminiResponse;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService implements AiAnswerService {

    private final WebClient webClient;

    @Value("${gemini.api.url}")
    private String apiUrl;

    @Value("${gemini.api.key}")
    private String apiKey;

    @SuppressWarnings("unchecked")
    @Override
    public AiResponse sendQuestion(AiRequest request) {
        String answer = "";

        GeminiRequest geminiRequest = getGeminiRequest(request.question());
        GeminiResponse geminiResponse = webClient.post()
                                                 .uri(apiUrl + "?key=" + apiKey)
                                                 .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                                 .bodyValue(geminiRequest)
                                                 .retrieve()
                                                 .bodyToMono(GeminiResponse.class)
                                                 .block();

        // gemini 리스폰스 파싱
        if (geminiResponse != null && !geminiResponse.candidates().isEmpty()) {
            Map<String, Object> candidate = geminiResponse.candidates().getFirst();
            Map<String, Object> content = (Map<String, Object>)candidate.get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>)content.get("parts");

            if (parts != null && !parts.isEmpty()) {
                Map<String, Object> part = parts.getFirst();
                answer = (String)part.get("text");
            }
        }

        return new AiResponse(request.question(), answer);
    }

    private GeminiRequest getGeminiRequest(String question) {
        String answerRole = """
            당신은 컴퓨터공학 지식을 제공하는 전문가입니다.
            항상 짧고 명확하게, 사실에 기반해 답변하세요.
            특정 표나 텍스트 강조 효과를 넣지 마시고, 문장으로 대답하세요.
            키워드 중심으로 대답하고 출처가 불확실한 내용은 제공하지 마세요.
            컴퓨터공학 관련된 물음이 아니면 답하지마세요.
            """;

        Map<String, Object> userContent;
        Map<String, Object> systemInstruction;

        userContent = Map.of(
            "role", "user",
            "parts", List.of(Map.of("text", question))
        );

        systemInstruction = Map.of(
            "role", "system",
            "parts", List.of(Map.of("text", answerRole))
        );

        return new GeminiRequest(List.of(userContent), systemInstruction);
    }
}

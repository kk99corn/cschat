package com.kk.cschat.ai.controller;

import com.kk.cschat.ai.dto.AiRequest;
import com.kk.cschat.ai.dto.AiResponse;
import com.kk.cschat.ai.service.AiAnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiAnswerService aiAnswerService;

    @GetMapping("/cs")
    public ResponseEntity<AiResponse> getAnswer(@RequestParam("q") String question) {
        return ResponseEntity.ok(aiAnswerService.sendQuestion(new AiRequest(question, null)));
    }
}

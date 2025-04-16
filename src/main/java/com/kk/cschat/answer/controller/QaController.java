package com.kk.cschat.answer.controller;

import com.kk.cschat.answer.service.QaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class QaController {

    private final QaService qaService;

    @GetMapping("/cs")
    public ResponseEntity<String> getAnswer(@RequestParam("q") String q) {
        return ResponseEntity.ok(qaService.getAnswer(q));
    }
}

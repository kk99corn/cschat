package com.kk.cschat.ai.service;

import com.kk.cschat.ai.dto.AiRequest;
import com.kk.cschat.ai.dto.AiResponse;

public interface AiAnswerService {
    AiResponse sendQuestion(AiRequest request);
}

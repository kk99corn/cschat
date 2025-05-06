package com.kk.cschat.ai.dto.gemini;

import java.util.List;
import java.util.Map;

public record GeminiResponse(
    List<Map<String, Object>> candidates
) {}

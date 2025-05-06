package com.kk.cschat.ai.dto.gemini;

import java.util.List;
import java.util.Map;

public record GeminiRequest(
    List<Map<String, Object>> contents,
    Map<String, Object> system_instruction
) {}

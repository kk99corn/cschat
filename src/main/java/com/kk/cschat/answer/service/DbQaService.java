package com.kk.cschat.answer.service;

import com.kk.cschat.answer.entity.CsQa;
import com.kk.cschat.answer.entity.KeywordAlias;
import com.kk.cschat.answer.repository.CsQaRepository;
import com.kk.cschat.answer.repository.KeywordAliasRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DbQaService implements QaService {

    private final CsQaRepository csQaRepository;
    private final KeywordAliasRepository keywordAliasRepository;

    @Override
    public String getAnswer(String keyword) {
        // 1. alias -> 본 키워드로 매핑
        String k = keywordAliasRepository.findByAliasIgnoreCase(keyword)
            .map(KeywordAlias::getCanonicalKeyword)
            .orElse(keyword);

        // 2. 본 키워드 기반으로 Q&A 조회
        return csQaRepository.findByKeywordIgnoreCase(k)
            .map(CsQa::getAnswer)
            .orElse("해당 키워드에 대한 답변이 등록되어 있지 않습니다.");
    }
}

package com.kk.cschat.answer.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.kk.cschat.answer.entity.CsQa;
import com.kk.cschat.answer.entity.KeywordAlias;
import com.kk.cschat.answer.repository.CsQaRepository;
import com.kk.cschat.answer.repository.KeywordAliasRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Transactional
class QaServiceTest {

    @Autowired
    private CsQaRepository csQaRepository;

    @Autowired
    private KeywordAliasRepository keywordAliasRepository;

    @Autowired
    private QaService qaService;

    @BeforeEach
    void setup() {
        // 본 키워드 + 답변 저장
        csQaRepository.save(new CsQa(null, "lru", "LRU는 가장 오래 사용되지 않은 항목을 제거하는 캐시 알고리즘입니다."));

        // 여러 alias 매핑
        keywordAliasRepository.save(new KeywordAlias(null, "엔알유", "lru"));
        keywordAliasRepository.save(new KeywordAlias(null, "엘알유", "lru"));
    }

    @Test
    void 기본_키워드_검색_테스트() {
        String result = qaService.getAnswer("lru");
        assertThat(result).contains("캐시 알고리즘");
    }

    @Test
    void 별칭으로_검색_테스트() {
        String result = qaService.getAnswer("엔알유");
        assertThat(result).contains("캐시 알고리즘");
    }

    @Test
    void 없는_키워드_테스트() {
        String result = qaService.getAnswer("힙정렬");
        assertThat(result).contains("등록되어 있지 않습니다");
    }
}
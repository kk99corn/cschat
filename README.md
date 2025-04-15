# CS Chat Bot

컴퓨터공학 질문-답변 서비스

## 주요 기능
- 키워드 기반 CS 지식 답변
- alias(별칭) 매핑 지원 (ex: 엔알유 → LRU)
- REST API: `/cs?q=키워드`

## 실행 방법
```bash
docker compose up -d
./gradlew bootRun
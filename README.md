# 💬 CS Chat Bot

컴퓨터공학 지식을 키워드로 질문하면, Slack에서 실시간으로 답변해주는 **비동기 기반 Q&A 서비스**입니다.

> "LRU가 뭐야?" → `LRU는 가장 오랫동안 사용되지 않은 데이터를 제거하는 캐시 알고리즘입니다.`

---

## 🧠 주요 기능

| 기능 | 설명 |
|------|------|
| ✅ 키워드 기반 CS 지식 응답 | 등록된 키워드에 대한 설명을 응답 |
| ✅ Alias 매핑 지원 | ex) `엔알유` → `LRU` 자동 인식 |
| ✅ Slack `/q` 명령어 사용 | Slash 커맨드로 질문 가능 |
| ✅ Kafka 기반 비동기 처리 | 질문 → 답변 전송 흐름을 Kafka로 처리 |
| ✅ REST API 테스트 가능 | `/cs?q=키워드` 로 테스트 가능 |
| ✅ Docker Compose 통합 실행 | MySQL, Kafka, Kafdrop 포함한 개발 환경 구성 |

---

## ⚙️ 실행 방법

### 1. 도커 환경 실행
```bash
docker compose up -d
```

### 2. Spring Boot 실행
```bash
./gradlew bootRun
```

### 3. Slack 연동 테스트
Slack에서 다음 Slash Command를 등록하고 사용합니다
```
Command: /q
Request URL: https://<your-public-url>/slack/cs (ex: ngrok 사용)
```

## 🛠️ 사용 기술
| 분류 | 기술                      |
|------|-------------------------|
|Language|	Java 21|
|Framework| 	Spring Boot 3.4.4      |
|Messaging| 	Apache Kafka           |
|Database	| MySQL 8.x               |
|Build Tool| 	Gradle                 |
|Container| 	Docker, Docker Compose |
|Slack	| Slash Command + Web API |
|Monitoring| 	Kafdrop (Kafka UI)     |
|기타	| Ngrok (로컬 Slack 테스트용)   |


## 🗂️ 패키지 구조 설명
```
com.kk.cschat
├── answer
│   ├── controller         # CS Q&A 전용 REST API
│   ├── entity             # JPA Entity (질문, 별칭)
│   ├── repository         # Spring Data JPA
│   └── service            # DB 조회 비즈니스 로직
│
├── config
│   └── KafkaProducerConfig  # Kafka 설정
│
├── kafka
│   ├── consumer           # Kafka Listener (질문/답변 수신)
│   ├── controller         # Kafka 테스트 전용 API
│   ├── dto                # Kafka 전송 객체
│   ├── producer           # Kafka 메시지 발행
│   └── util               # Kafka 유틸 (Slack 전송 등)
│
├── slack
│   └── controller         # Slack 요청 수신용 엔드포인트
│
└── CschatApplication      # Spring Boot 메인 클래스
```

## 🔄 카프카 메시지 흐름
```
[Slack /q LRU]
     ↓
SlackController → Kafka (cs-question) 발행
     ↓
KafkaQuestionConsumer → DB 조회 후 cs-answer 발행
     ↓
KafkaAnswerConsumer → Slack에 응답 전송
```

## 🧪 API 예시
질문 테스트
```
GET /cs?q=lru
→ 응답: LRU는 가장 오랫동안 사용되지 않은 데이터를 제거하는 캐시 알고리즘입니다.
```
Slack 명령어
```
/q 엔알유
→ 자동으로 'LRU' 매핑 후 응답 반환
```
# CS Chat Bot

컴퓨터공학 지식 기반 질문-답변 서비스입니다.  
Slack, Kafka 등 다양한 채널과의 연동을 목표로 하고 있으며,  
현재는 키워드를 통해 컴퓨터공학 지식을 REST API로 제공하는 MVP 구조입니다.

---

## ✨ 주요 기능

- 🔍 **키워드 기반 CS 지식 응답**
    - ex: `/cs?q=LRU` → "Least Recently Used"
- 🔁 **Alias(별칭) 매핑 기능**
    - ex: "엘알유" → "LRU" 로 자동 매핑
- 🔗 **Kafka 비동기 메시지 처리**
    - `cs-question` → `KafkaListener` → `cs-answer` 토픽으로 응답 발행
- 📦 **Docker 기반 로컬 개발 환경 구성**

---

## 🧪 기술 스택

- **Java 21**, **Spring Boot 3.4.4**
- **MySQL 8.0**, **Kafka (Confluent)**, **Docker Compose**
- **Lombok**, **Spring Data JPA**
- **Spring for Apache Kafka**
- **Kafdrop**: Kafka 모니터링 UI

---

## 📌 REST API 사용법

| Endpoint | 설명 |
|----------|------|
| `GET /cs?q=키워드` | 해당 키워드에 대한 컴퓨터공학 지식 응답 |

```bash
curl "http://localhost:8080/cs?q=lru"
```
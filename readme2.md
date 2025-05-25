# 💬 CS Chat Bot

컴퓨터공학 지식을 키워드로 질문하면, Slack에서 실시간으로 답변해주는 **비동기 기반 Q&A 서비스**입니다.

> "LRU가 뭐야?" → `LRU는 가장 오랫동안 사용되지 않은 데이터를 제거하는 캐시 알고리즘입니다.`

---

## 📚 목차
- [주요 기능](#-주요-기능)
- [사전-준비-사항](#-사전-준비-사항)
- [애플리케이션-설정](#-애플리케이션-설정)
- [실행-방법](#️-실행-방법)
- [트러블슈팅](#-트러블슈팅)
- [사용-기술](#️-사용-기술)
- [패키지-구조-설명](#-패키지-구조-설명)
- [카프카-메시지-흐름](#-카프카-메시지-흐름)
- [API-예시](#-api-예시)
- [기여-방법](#-기여-방법)

---

## 🧠 주요 기능

| 기능 | 설명 |
|------|------|
| ✅ 키워드 기반 CS 지식 응답 | 사용자가 입력한 키워드에 대한 정의나 설명을 제공합니다. 이 정보는 내부 데이터베이스에 `CsQa` 테이블로 저장되어 있으며, `keyword`와 `answer` 컬럼으로 구성됩니다. `DbQaService`가 이 테이블에서 정보를 조회합니다. |
| ✅ Alias 매핑 지원 | 사용자가 자주 사용하는 약어나 다른 표현(예: `엔알유`)을 정식 키워드(예: `LRU`)로 자동 변환하여 응답합니다. 이 매핑 정보는 `KeywordAlias` 테이블에 `alias`와 `canonical_keyword`로 저장되며, `DbQaService`가 조회 시 우선적으로 참조합니다. |
| ✅ Slack `/q` 명령어 사용 | Slack 사용자는 `/q [키워드]` 명령어를 통해 CS 관련 질문을 할 수 있습니다. `SlackController`가 이 요청을 받아 (`/slack/cs` 엔드포인트), 질문 내용(`text`), 응답을 받을 URL(`response_url`), 사용자 ID(`user_id`)를 추출합니다. |
| ✅ Kafka 기반 비동기 처리 | Slack으로부터 받은 질문은 즉시 처리되지 않고 Kafka 메시지 큐를 통해 비동기적으로 처리됩니다. `SlackController`는 `QuestionMessage`를 `KafkaMessageProducer`를 통해 `cs-question` 토픽으로 발행합니다. `KafkaQuestionConsumer`가 이 메시지를 구독하여 답변을 처리한 후, `AnswerMessage`를 `cs-answer` 토픽으로 발행합니다. 최종적으로 `KafkaAnswerConsumer`가 이 답변 메시지를 받아 Slack으로 응답을 전송합니다. AI 기반 답변의 경우 `cs-ai-question` 및 `cs-ai-answer` 토픽이 사용됩니다. |
| ✅ REST API 테스트 가능 | `/cs?q=키워드` 형태의 GET 요청을 통해 서비스 동작을 테스트하고 답변을 직접 확인할 수 있습니다. `QaController`가 이 요청을 처리하며, 내부적으로 `DbQaService`를 호출하여 답변을 가져옵니다. |
| ✅ Docker Compose 통합 실행 | `docker compose up -d` 명령어를 통해 개발 및 테스트에 필요한 주요 서비스들(MySQL 데이터베이스, Apache Kafka 메시지 브로커, Kafdrop Kafka 모니터링 UI)을 한 번에 실행할 수 있습니다. |
| ✅ AI 기반 답변 (Experimental) | `/ai-cs [키워드]` Slack 명령어를 통해 AI (Google Gemini) 기반의 답변을 받을 수 있는 실험적인 기능입니다. `SlackController`의 `/slack/ai-cs` 엔드포인트가 요청을 받아 `cs-ai-question` 토픽으로 전달하고, `AiAnswerService`와 `GeminiService`를 통해 AI 응답을 생성하여 `cs-ai-answer` 토픽을 거쳐 Slack으로 회신합니다. |

---

## 📝 사전 준비 사항

이 프로젝트를 빌드하고 실행하기 위해 다음 소프트웨어가 필요합니다:

1.  **Java Development Kit (JDK)**
    *   **버전**: Java 21. 이 프로젝트는 Java 21을 사용하여 개발되었습니다 (`build.gradle`의 `languageVersion = JavaLanguageVersion.of(21)` 설정 참조).
    *   **설치**: [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) 또는 [OpenJDK](https://openjdk.org/projects/jdk/21/) (예: Adoptium Temurin) 등에서 다운로드하여 설치할 수 있습니다. 시스템 환경 변수 `JAVA_HOME`을 설정하고 `Path`에 JDK의 `bin` 디렉토리를 추가해야 합니다.

2.  **Docker**
    *   **설명**: Docker는 애플리케이션을 컨테이너화하여 배포하고 실행하는 플랫폼입니다. 이 프로젝트에서는 MySQL, Kafka와 같은 외부 서비스를 실행하는 데 사용됩니다.
    *   **설치**: [Docker 공식 웹사이트](https://docs.docker.com/get-docker/)에서 운영체제에 맞는 Docker Desktop 또는 Docker Engine을 설치합니다.

3.  **Docker Compose**
    *   **설명**: Docker Compose는 여러 컨테이너 Docker 애플리케이션을 정의하고 실행하기 위한 도구입니다. `docker-compose.yml` 파일을 사용하여 다중 컨테이너 환경을 쉽게 구성할 수 있습니다.
    *   **설치**: Docker Desktop에는 일반적으로 Docker Compose가 포함되어 있습니다. Docker Engine을 별도로 설치한 경우, [Docker Compose 설치 가이드](https://docs.docker.com/compose/install/)를 참조하여 설치합니다.

4.  **Git**
    *   **설명**: 프로젝트 코드를 관리하고 협업하기 위한 버전 관리 시스템입니다.
    *   **설치**: [Git 공식 웹사이트](https://git-scm.com/downloads)에서 다운로드하여 설치합니다.

5.  **(선택 사항) IDE (통합 개발 환경)**
    *   **추천**: IntelliJ IDEA, Eclipse, VS Code 등 Java 개발을 지원하는 IDE를 사용하면 개발 생산성을 높일 수 있습니다.

---

## ⚙️ 애플리케이션 설정

애플리케이션의 주요 설정은 `src/main/resources` 디렉토리의 YAML 파일들을 통해 관리됩니다.

-   **`application.yml`**: 기본 프로파일을 지정합니다. 현재 `local` 프로파일이 기본으로 활성화되어 있습니다.
    ```yaml
    spring:
      profiles:
        active: local
    ```

-   **`application-local.yml`**: 로컬 개발 환경 설정을 담당합니다.
    *   **데이터베이스 (MySQL)**:
        *   URL: `jdbc:mysql://localhost:3306/cschat` (로컬 호스트의 MySQL 사용)
        *   사용자명: `csuser`
        *   비밀번호: `cspass`
    *   **Kafka**:
        *   Bootstrap Servers: `localhost:9092` (로컬 호스트의 Kafka 사용)
    *   **JPA**:
        *   `ddl-auto: update` (애플리케이션 시작 시 Entity 변경에 따라 스키마 자동 업데이트)
        *   `show-sql: true` (JPA가 실행하는 SQL 쿼리 로그 출력)
    *   **Gemini API**:
        *   `key: ${GEMINI_API_KEY}` (AI 기능 사용 시 `GEMINI_API_KEY` 환경 변수 설정 필요)

-   **`application-prod.yml`**: 프로덕션 환경 설정을 담당합니다. Docker Compose 환경에서 주로 사용됩니다.
    *   **데이터베이스 (MySQL)**:
        *   URL: `jdbc:mysql://mysql:3306/cschat` (Docker 네트워크 내의 `mysql` 서비스 이름 사용)
        *   사용자명: `csuser`
        *   비밀번호: `cspass`
    *   **Kafka**:
        *   Bootstrap Servers: `kafka:29092` (Docker 네트워크 내의 `kafka` 서비스 이름 및 내부 포트 사용)
    *   **Gemini API**:
        *   `key: ${GEMINI_API_KEY}` (AI 기능 사용 시 `GEMINI_API_KEY` 환경 변수 설정 필요)

**주요 설정 항목 상세**:

1.  **Slack 연동**:
    *   Slack API 토큰이나 시크릿은 코드나 설정 파일에 직접 하드코딩하지 않습니다.
    *   Slack 앱 설정에서 Slash Command의 `Request URL`을 지정하면, Slack이 해당 URL로 요청을 보내고, 애플리케이션은 이 요청에 포함된 `response_url`을 사용하여 비동기적으로 응답합니다.
    *   자세한 설정 방법은 "[3. Slack 연동 테스트](#3-slack-연동-테스트)" 섹션을 참조하십시오.

2.  **Kafka 브로커 주소**:
    *   `spring.kafka.bootstrap-servers` 속성을 통해 설정됩니다.
    *   `application-local.yml`: `localhost:9092`
    *   `application-prod.yml`: `kafka:29092` (Docker Compose 네트워크 내부 주소)

3.  **데이터베이스 연결**:
    *   `spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password` 속성을 통해 설정됩니다.
    *   `application-local.yml`: 로컬 MySQL (`localhost:3306`, `cschat` DB, `csuser`/`cspass`)
    *   `application-prod.yml`: Docker Compose 내의 MySQL (`mysql:3306`, `cschat` DB, `csuser`/`cspass`)

4.  **Gemini API 키**:
    *   AI 기반 답변 기능을 사용하려면 Google Gemini API 키가 필요합니다.
    *   `gemini.api.key` 속성을 통해 설정되며, 값은 `${GEMINI_API_KEY}`로 되어 있어 환경 변수로부터 주입받습니다.
    *   애플리케이션 실행 환경에서 `GEMINI_API_KEY`라는 이름의 환경 변수에 실제 API 키를 설정해야 합니다.
    ```bash
    # Linux / macOS
    export GEMINI_API_KEY="YOUR_GEMINI_API_KEY"
    # Windows (Command Prompt)
    # set GEMINI_API_KEY="YOUR_GEMINI_API_KEY"
    # Windows (PowerShell)
    # $env:GEMINI_API_KEY="YOUR_GEMINI_API_KEY"
    ```
    또는 IDE 실행 설정에서 환경 변수를 추가할 수 있습니다.

**프로파일 변경**:
Spring Boot 애플리케이션 실행 시 `spring.profiles.active` 환경 변수를 설정하여 다른 프로파일(예: `prod`)을 활성화할 수 있습니다.
```bash
# JAR 파일 실행 시
java -jar -Dspring.profiles.active=prod build/libs/cschat-0.0.1-SNAPSHOT.jar
# Gradle로 실행 시
./gradlew bootRun --args='--spring.profiles.active=prod'
```
또는 IDE의 실행 구성에서 활성 프로파일을 변경할 수 있습니다.

---

## ⚙️ 실행 방법

### 1. 도커 환경 실행
```bash
docker compose up -d
```
이 명령어는 다음 서비스들을 백그라운드에서 실행합니다:
- **MySQL**: CS 지식 및 키워드 별칭을 저장하는 데이터베이스입니다. (접근 포트: 로컬에서 `3306`)
- **Apache Kafka**: 질문과 답변 메시지를 비동기적으로 처리하기 위한 메시지 브로커입니다. (접근 포트: 로컬에서 `9092`)
- **Kafdrop**: Kafka 토픽 및 메시지를 웹 UI를 통해 모니터링하는 도구입니다. (접속: `http://localhost:9000` - 이 URL은 `docker-compose.yml` 설정에 따라 다를 수 있으며, 일반적인 기본값입니다.)

### 2. Spring Boot 실행
```bash
./gradlew bootRun
```
기본적으로 `local` 프로파일(`application-local.yml` 사용)이 활성화됩니다. 다른 프로파일(예: `prod`)을 사용하려면 다음 중 하나의 방법으로 지정합니다:
-   `spring.profiles.active` 환경 변수 설정 (예: `export SPRING_PROFILES_ACTIVE=prod`)
-   Gradle 실행 시 인자 전달: `./gradlew bootRun --args='--spring.profiles.active=prod'`
-   IDE 실행 설정에서 프로파일 지정

### 3. Slack 연동 테스트
Slack에서 다음 Slash Command를 등록하고 사용합니다:
- **Command**: `/q` (또는 `/ai-cs` AI 기능 테스트 시)
- **Request URL**: `https://<your-public-url>/slack/cs` (또는 `/slack/ai-cs`). `<your-public-url>`은 로컬에서 테스트 시 ngrok 등으로 생성된 공개 주소여야 합니다.
  - **ngrok 사용 시 주의사항**:
    - ngrok은 일정 시간 사용 후나 재시작 시 URL이 변경될 수 있으므로, 변경될 때마다 Slack App 설정의 Request URL을 업데이트해야 합니다.
    - 무료 ngrok 플랜은 동시 터널 수 제한이 있을 수 있습니다.
- **권한 (Scopes)**: Slack 앱 설정 시 `commands` scope (Slash Commands를 위함) 및 `chat:write` (메시지 전송을 위함, response_url을 통한 응답은 이 scope가 필요 없을 수 있으나, 일반적인 메시지 전송을 위해 추가하는 것이 좋음) 권한이 필요할 수 있습니다.

---

## 🚑 트러블슈팅

다음은 프로젝트 설정 또는 실행 중 발생할 수 있는 일반적인 문제와 해결 방법입니다.

1.  **애플리케이션이 Kafka/MySQL에 연결되지 않음**
    *   **증상**: Spring Boot 애플리케이션 로그에 `Connection refused` 또는 유사한 데이터베이스/Kafka 연결 오류 메시지가 나타납니다.
    *   **진단 및 해결**:
        *   **Docker 컨테이너 상태 확인**: `docker ps` 명령어를 사용하여 MySQL 및 Kafka 컨테이너가 정상적으로 실행 중인지 확인합니다. `STATUS`가 `Up`이어야 합니다.
        *   **컨테이너 로그 확인**: `docker logs <container_name>` (예: `docker logs cschat-mysql-1` 또는 `docker logs cschat-kafka-1` - 실제 컨테이너 이름은 다를 수 있음) 명령어로 각 컨테이너의 로그를 확인하여 오류가 있는지 살펴봅니다.
        *   **설정 파일 확인**: `application-local.yml` (로컬 실행 시) 또는 `application-prod.yml` (Docker 실행 시)의 `spring.datasource.url`, `spring.kafka.bootstrap-servers` 주소가 올바른지 확인합니다.
            *   로컬 실행 시: MySQL은 `localhost:3306`, Kafka는 `localhost:9092`.
            *   Docker Compose 내에서 Spring Boot 실행 시 (일반적이지 않음, 보통 로컬에서 Spring Boot 실행 후 Docker의 서비스에 연결): 서비스 이름을 사용해야 합니다 (`mysql:3306`, `kafka:29092`).
        *   **방화벽/네트워크 문제**: 로컬 방화벽이 Docker 컨테이너 또는 애플리케이션의 포트 연결을 차단하고 있지 않은지 확인합니다.
        *   **데이터베이스 초기화**: MySQL 컨테이너가 처음 실행될 때 초기화에 시간이 걸릴 수 있습니다. 잠시 후 다시 시도하거나 컨테이너 로그를 확인하여 초기화 완료 여부를 확인합니다.

2.  **Slack 명령어가 작동하지 않음**
    *   **증상**: Slack에서 `/q` 명령어를 실행해도 응답이 없거나 Slack에 오류 메시지가 표시됩니다.
    *   **진단 및 해결**:
        *   **Request URL 확인**: Slack 앱 설정의 Slash Command `Request URL`이 올바르게 입력되었는지, 그리고 해당 URL이 현재 접근 가능한지 확인합니다.
            *   ngrok 사용 시: ngrok 터널이 활성화되어 있고, 표시된 URL이 Slack 설정과 일치하는지 확인합니다. ngrok URL은 재시작 시 변경될 수 있습니다.
        *   **Spring Boot 애플리케이션 확인**: Spring Boot 애플리케이션이 정상적으로 실행 중이고, `/slack/cs` (또는 `/slack/ai-cs`) 엔드포인트가 요청을 받을 수 있는 상태인지 확인합니다. 애플리케이션 로그에 Slack으로부터 요청이 들어오는지 확인합니다.
        *   **Kafka 메시지 흐름 확인**:
            *   `SlackController`가 Kafka로 `QuestionMessage`를 성공적으로 발행하는지 애플리케이션 로그를 통해 확인합니다.
            *   Kafdrop (`http://localhost:9000` 또는 `docker-compose.yml`에 정의된 주소)을 사용하여 `cs-question` 토픽에 메시지가 들어오는지, `KafkaQuestionConsumer`가 이를 소비하고 `cs-answer` 토픽으로 메시지를 발행하는지, `KafkaAnswerConsumer`가 최종적으로 Slack으로 응답을 보내는지 확인합니다. 각 단계에서 로그를 함께 확인합니다.
        *   **Slack API 권한**: Slack 앱에 필요한 권한(예: `commands`)이 부여되었는지 확인합니다.

3.  **Docker 컨테이너 시작 실패**
    *   **증상**: `docker compose up -d` 실행 시 오류가 발생하거나, 일부 컨테이너가 즉시 종료됩니다.
    *   **진단 및 해결**:
        *   **포트 충돌**: Docker 컨테이너가 사용하려는 포트(예: MySQL의 3306, Kafka의 9092, Kafdrop의 9000)가 이미 로컬 시스템에서 사용 중인지 확인합니다. `netstat -tulnp | grep <port>` (Linux/macOS) 또는 `Get-NetTCPConnection -LocalPort <port>` (Windows PowerShell) 등으로 확인하고, 충돌 시 해당 포트를 사용하는 다른 서비스를 중지하거나 `docker-compose.yml`에서 포트 매핑을 변경합니다.
        *   **Docker 데몬 상태**: Docker 데몬이 정상적으로 실행 중인지 확인합니다.
        *   **디스크 공간**: 디스크 공간이 부족하지 않은지 확인합니다.
        *   **이미지 다운로드 실패**: 인터넷 연결 문제로 Docker 이미지를 다운로드하지 못했을 수 있습니다. 인터넷 연결을 확인하고 다시 시도합니다.
        *   **컨테이너 로그**: `docker logs <container_name>`으로 실패한 컨테이너의 상세 로그를 확인하여 원인을 파악합니다.

4.  **Gemini API 키 관련 오류**
    *   **증상**: AI 답변 기능 사용 시 API 키 관련 오류가 발생하거나, 응답을 받지 못합니다.
    *   **진단 및 해결**:
        *   **환경 변수 설정**: `GEMINI_API_KEY` 환경 변수가 정확히 설정되었는지 확인합니다. (Linux/macOS: `echo $GEMINI_API_KEY`, Windows CMD: `echo %GEMINI_API_KEY%`, Windows PowerShell: `echo $env:GEMINI_API_KEY`)
        *   **API 키 유효성**: 발급받은 API 키가 유효한지, 해당 API에 대한 권한이 있는지 Google AI Studio 등에서 확인합니다.
        *   **네트워크**: 애플리케이션 실행 환경에서 Google API 서버 (`generativelanguage.googleapis.com`)로의 네트워크 연결이 가능한지 확인합니다.

---

## 🛠️ 사용 기술
| 분류 | 기술                      | 설명 |
|------|-------------------------|------|
|Language|	Java 21| 최신 LTS 버전의 Java를 사용하여 현대적인 프로그래밍 기능과 성능 개선을 활용합니다. |
|Framework| Spring Boot 3.4.4      | 의존성 관리, 자동 설정, 내장 웹 서버 등을 통해 독립 실행 가능한 프로덕션 등급의 Spring 기반 애플리케이션을 쉽게 개발할 수 있도록 지원합니다. REST API, 서비스 로직, 데이터 접근 등을 관리합니다. |
|Messaging| Apache Kafka           | 대규모 메시지를 안정적으로 처리하고, 서비스 간의 결합도를 낮추어 확장성과 탄력성을 높이기 위해 사용됩니다. 질문과 답변 처리를 비동기적으로 수행하여 사용자 경험을 향상시킵니다. |
|Database	| MySQL 8.x               | CS Q&A 데이터와 키워드 별칭 정보를 영구적으로 저장하고 관리하기 위한 관계형 데이터베이스입니다. |
|Build Tool| Gradle                 | 프로젝트 의존성 관리 및 빌드 자동화를 위해 사용됩니다. Kotlin DSL을 사용할 수 있어 유연한 빌드 스크립트 작성이 가능합니다. |
|Container| Docker, Docker Compose | 개발 환경의 일관성을 보장하고, MySQL, Kafka 등의 외부 서비스들을 쉽게 배포하고 관리하기 위해 사용됩니다. `docker-compose.yml` 파일을 통해 전체 애플리케이션 스택을 정의합니다. |
|Slack	| Slash Command + Web API | Slack 사용자와의 상호작용을 위해 Slash Command를 사용하며, 비동기 응답은 Slack Web API(response_url)를 통해 전달됩니다. |
|Monitoring| Kafdrop (Kafka UI)     | Kafka 클러스터의 상태, 토픽, 메시지, 컨슈머 그룹 등을 시각적으로 모니터링하고 관리할 수 있는 웹 기반 도구입니다. 개발 및 디버깅 시 유용합니다. |
|AI | Google Gemini | 실험적인 AI 답변 기능을 위해 사용되는 Google의 멀티모달 AI 모델입니다. `AiAnswerService`와 `GeminiService`를 통해 상호작용합니다. |
|기타	| Ngrok (로컬 Slack 테스트용)   | 로컬 개발 환경에서 실행 중인 웹 서버(Spring Boot 애플리케이션)를 외부에서 접근 가능한 공개 URL로 노출시켜 Slack API와 연동 테스트를 용이하게 합니다. |

---

## 🗂️ 패키지 구조 설명
```
com.kk.cschat
├── ai                     # AI 기반 답변 관련 기능
│   ├── controller         # AiController: AI 기능 테스트용 REST API (구현되어 있다면)
│   ├── dto                # AiRequest, AiResponse, Gemini 관련 DTOs
│   └── service            # AiAnswerService: AI 모델 호출 및 답변 생성 로직, GeminiService: Gemini API 연동
│
├── answer                 # 키워드 기반 Q&A 기능
│   ├── controller         # QaController: CS Q&A 전용 REST API (/cs)
│   ├── entity             # CsQa: 질문/답변 데이터 JPA Entity, KeywordAlias: 키워드 별칭 JPA Entity
│   ├── repository         # CsQaRepository, KeywordAliasRepository: Spring Data JPA 리포지토리
│   └── service            # DbQaService: DB 기반 Q&A 조회 비즈니스 로직, QaService: 인터페이스
│
├── config                 # 애플리케이션 설정
│   ├── KafkaProducerConfig  # Kafka Producer 관련 설정
│   ├── WebClientConfig    # 외부 API 호출(Slack 응답 등)을 위한 WebClient 설정
│   └── aop                # ExecutionLoggerAspect: 메소드 실행 시간 로깅 등 AOP 관련
│
├── job                    # 배치 작업 관련 (현재 README 내용에는 명시적 기능 없음)
│   ├── config             # MockInsertJobConfiguration: Spring Batch Job 설정 예시
│   ├── controller         # JobController: 배치 Job 실행을 위한 컨트롤러 예시
│   ├── dto                # MockOrderDto: 배치 작업용 DTO 예시
│   ├── entity             # MockOrder: 배치 작업용 Entity 예시
│   ├── mapper             # MockOrderMapper: MyBatis 매퍼 예시 (Spring Batch와 함께 사용 가능)
│   └── service            # MockOrderService: 배치 작업 관련 서비스 예시
│
├── kafka                  # Kafka 메시징 관련
│   ├── consumer           # KafkaQuestionConsumer: 'cs-question', 'cs-ai-question' 토픽 메시지 수신 및 처리
│                          # KafkaAnswerConsumer: 'cs-answer', 'cs-ai-answer' 토픽 메시지 수신 및 Slack 응답 전송
│   ├── controller         # QaKafkaTestController: Kafka 메시지 발행 테스트용 API
│   ├── dto                # QuestionMessage: 질문 Kafka 메시지 DTO, AnswerMessage: 답변 Kafka 메시지 DTO, KafkaMessage: 공통 인터페이스
│   ├── producer           # KafkaMessageProducer: Kafka 토픽으로 메시지 발행
│   └── util               # KafkaSlackNotifier: Kafka Consumer가 Slack으로 메시지를 보낼 때 사용하는 유틸 클래스
│
├── slack                  # Slack 연동 관련
│   └── controller         # SlackController: Slack Slash Command 요청 (/slack/cs, /slack/ai-cs) 수신 및 Kafka 발행
│
└── CschatApplication      # Spring Boot 메인 애플리케이션 클래스
```

## 🔄 카프카 메시지 흐름
```
[Slack /q LRU] 또는 [/ai-cs OS]
     ↓
SlackController: (POST /slack/cs 또는 /slack/ai-cs)
  - Request Body에서 keyword, response_url, user_id 추출
  - QuestionMessage DTO 생성:
    ```json
    {
      "userId": "U123ABC",
      "keyword": "LRU",
      "rawQuestion": null, // 현재는 keyword만 사용
      "responseUrl": "https://hooks.slack.com/commands/..."
    }
    ```
  - KafkaMessageProducer.sendQuestion(message) 호출 → 'cs-question' 토픽으로 발행
  - 또는 KafkaMessageProducer.sendAiQuestion(message) 호출 → 'cs-ai-question' 토픽으로 발행
     ↓
KafkaQuestionConsumer: (@KafkaListener(topics = "cs-question", ...)) 또는 (@KafkaListener(topics = "cs-ai-question", ...))
  - DbQaService.getAnswer(keyword) 호출 (cs-question의 경우)
  - 또는 AiAnswerService.sendQuestion(keyword) 호출 (cs-ai-question의 경우)
  - AnswerMessage DTO 생성:
    ```json
    {
      "userId": null, // 현재는 사용되지 않음
      "keyword": "LRU",
      "answer": "LRU는 가장 오랫동안 사용되지 않은 데이터를 제거하는 캐시 알고리즘입니다.",
      "responseUrl": "https://hooks.slack.com/commands/..."
    }
    ```
  - KafkaMessageProducer.sendAnswer(message) 호출 → 'cs-answer' 토픽으로 발행
  - 또는 KafkaMessageProducer.sendAiAnswer(message) 호출 → 'cs-ai-answer' 토픽으로 발행
     ↓
KafkaAnswerConsumer: (@KafkaListener(topics = "cs-answer", ...)) 또는 (@KafkaListener(topics = "cs-ai-answer", ...))
  - AnswerMessage 수신
  - KafkaSlackNotifier.sendToSlack(responseUrl, formatted_reply) 호출
  - Slack response_url로 최종 답변 메시지 전송: "*[LRU]* → LRU는 가장 오랫동안 사용되지 않은 데이터를 제거하는 캐시 알고리즘입니다."
```

## 🧪 API 예시
질문 테스트
```bash
GET /cs?q=lru
```
응답:
```
LRU는 가장 오랫동안 사용되지 않은 데이터를 제거하는 캐시 알고리즘입니다.
```
Slack 명령어
```
/q 엔알유
→ 자동으로 'LRU' 매핑 후 응답 반환: "*[LRU]* → LRU는 가장 오랫동안 사용되지 않은 데이터를 제거하는 캐시 알고리즘입니다."

/ai-cs 운영체제
→ AI가 생성한 답변 반환: "*[운영체제]* → 운영 체제는 컴퓨터 시스템의 하드웨어와 소프트웨어 리소스를 관리하고..."
```
---

## 🌱 기여 방법

이 프로젝트에 기여하고 싶으시다면 다음 가이드라인을 따라주세요.

1.  **이슈 확인 및 생성**:
    *   기존 [이슈 목록](https://github.com/your-repo/cs-chat-bot/issues) (주의: 이 링크는 실제 저장소 URL로 대체해야 합니다)을 확인하여 동일한 내용의 작업이 진행 중인지 확인합니다.
    *   새로운 기능 제안이나 버그 리포트는 새로운 이슈를 생성하여 상세히 작성합니다.

2.  **Fork 및 Branch 생성**:
    *   이 저장소를 자신의 계정으로 fork합니다.
    *   개발할 내용을 기반으로 `main` (또는 `develop` 브랜치가 있다면 해당 브랜치)에서 새로운 feature 브랜치를 생성합니다. (예: `git checkout -b feature/new-keyword-matching`)

3.  **코드 작성**:
    *   **코딩 스타일**: 프로젝트에 정의된 코딩 스타일 가이드가 있다면 따릅니다. 없다면, 기존 코드의 스타일을 일관성 있게 유지하고, 일반적인 Java 스타일 가이드(예: Google Java Style Guide)를 참고합니다.
    *   가독성 있고 명확한 코드를 작성합니다.
    *   필요한 경우 Javadoc 또는 주석을 추가합니다.

4.  **테스트 작성 및 실행**:
    *   새로운 기능이나 로직 변경 시에는 관련된 단위 테스트(Unit Tests) 또는 통합 테스트(Integration Tests)를 작성하는 것을 권장합니다.
    *   모든 테스트는 `./gradlew test` 명령어로 실행하여 통과하는지 확인합니다.

5.  **Commit 메시지**:
    *   Commit 메시지는 변경 내용을 명확하게 설명하도록 작성합니다. (예: `Feat: Add fuzzy matching for keywords`)
    *   [Conventional Commits](https://www.conventionalcommits.org/) 스타일을 따르는 것을 권장합니다. (예: `feat:`, `fix:`, `docs:`, `style:`, `refactor:`, `test:`, `chore:`)

6.  **Pull Request (PR) 생성**:
    *   작업이 완료되면, 자신의 feature 브랜치에서 원본 저장소의 `main` (또는 `develop`) 브랜치로 Pull Request를 생성합니다.
    *   PR 템플릿이 있다면 작성하고, 변경 사항, 테스트 결과 등을 상세히 설명합니다.
    *   PR은 최소 한 명 이상의 리뷰어에게 검토받는 것을 목표로 합니다.

7.  **코드 리뷰 및 Merge**:
    *   리뷰어의 피드백을 반영하여 코드를 수정합니다.
    *   모든 논의와 수정이 완료되면, 프로젝트 관리자가 PR을 merge합니다.

프로젝트에 관심을 가져주셔서 감사합니다!

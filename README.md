# 💻 WISE AI 개발 프로젝트 - wiseai-dev

WISE AI 개발 프로젝트 **wiseai-dev**는 회의실 예약 및 결제 시스템을 개발하는 프로젝트입니다.  
이 문서는 **프로젝트 실행 방법, 기술 스택, 주요 기능**에 대한 설명을 담고 있습니다.

---

## 📌 프로젝트 개요
본 프로젝트는 **MSA (Microservices Architecture)** 를 지향하며, 각 서비스는 **Spring Boot**와 **Kotlin**으로 구성되어 있습니다.

---

## ⚙️ 기술 스택

| 카테고리      | 기술 스택            | 버전      |
|---------------|----------------------|-----------|
| **백엔드**    | Spring Boot          | 3.2.0     |
|               | Kotlin               | 1.9.21    |
|               | Spring Data JPA      | 3.2.0     |
|               | Gradle               | 8.5       |
| **데이터베이스** | MySQL              | 8.0.35    |
| **컨테이너**  | Docker, Docker Compose | 최신 버전 |
| **API 문서화** | Swagger UI          | 3.0.0     |
| **테스트**    | JUnit 5, Mockito     | 5.10.1    |

---

## 🚀 핵심 기능

- **회의실 예약 및 관리**
  - 회의실 생성, 조회, 수정, 삭제
  - 사용자별 예약 및 예약 내역 관리

- **결제 시스템**
  - 카카오페이, 토스 등 다양한 결제사 연동을 위한 확장 가능 구조
  - 결제 상태 조회 API: `GET /payments/{paymentId}/status`
  - 결제사 웹훅 수신 및 처리 API: `POST /webhooks/payments/{provider}`

---

## 🏗 프로젝트 구조 및 설계

### 1. 레이어드 아키텍처 + DDD (Domain-Driven Design)

- **Presentation 계층**: 클라이언트 요청 수신 및 응답 반환 (Controller)
- **Application 계층**: 비즈니스 로직 조율 및 트랜잭션 관리 (Service)
- **Domain 계층**: 순수 비즈니스 로직 (도메인 모델, 도메인 서비스, 리포지토리 인터페이스)
- **Infrastructure 계층**: 영속성 및 외부 연동 (JPA 엔티티, JPA 리포지토리 구현체 등)

### 2. DTO 활용
- 클라이언트-서버 간 데이터 전송 객체
- 도메인 모델 노출 방지 및 API 스펙 변경 대응
- 안정적인 서비스 운영 보장

### 3. 결제 시스템 확장성
- `PaymentGateway` 인터페이스를 정의하여 새로운 결제사 추가 시 인터페이스 구현만으로 확장 가능
- `POST /webhooks/payments/{provider}` API를 통해 다양한 결제사의 실시간 상태 변경 알림을 처리

### 4. 컨테이너 기반 개발 환경
- **Docker Compose**를 활용하여 동일한 개발 환경 제공
- 환경 설정에 소요되는 시간을 최소화

---

## 🛠 프로젝트 시작하기

### 사전 준비
- Docker 및 Docker Compose 설치 필요

### 실행 방법
1. 프로젝트 루트 디렉토리로 이동
2. 터미널에서 실행:
   ```bash
   docker-compose up --build
컨테이너 실행 후, 백엔드 서비스는
http://localhost:8080 에서 동작

📖 API 문서 (Swagger UI)
프로젝트 실행 후 아래 URL에서 API 확인 가능:
👉 http://localhost:8080/swagger-ui

✅ 테스트 실행 방법
Gradle로 실행
bash
복사
편집
./gradlew test
IDE에서 실행
IntelliJ IDEA 등 IDE에서 테스트 클래스 또는 메서드 옆 ▶ 버튼 클릭

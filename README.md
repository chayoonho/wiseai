# 💻 WISE AI 개발 프로젝트 - wiseai-dev

WISE AI 개발 프로젝트 **wiseai-dev**는 회의실 예약 및 결제 시스템을 개발하는 프로젝트입니다.  
이 문서는 **프로젝트 실행 방법, 기술 스택, 주요 기능, API 명세, 아키텍처 설계**를 담고 있습니다.

---

## 📌 프로젝트 개요
본 프로젝트는 **Spring Boot + Java** 기반의 백엔드 애플리케이션으로,  
회의실 예약/결제 관리 및 확장 가능한 결제 연동 구조를 제공합니다.

---

## ⚙️ 기술 스택

| 카테고리      | 기술 스택            | 버전      |
|---------------|----------------------|-----------|
| **백엔드**    | Spring Boot          | 3.2.0     |
|               | Spring Data JPA      | 3.2.0     |
|               | Gradle               | 8.5       |
| **언어**      | Java                 | 17.0.12   |
| **데이터베이스** | MySQL              | 8.0.35    |
| **컨테이너**  | Docker, Docker Compose | 최신 버전 |
| **API 문서화** | Swagger UI          | 3.0.0     |
| **테스트**    | JUnit 5, Mockito     | 5.10.1    |

---

## 🚀 핵심 기능

### 1. 회의실 예약 및 관리
- 회의실 생성, 조회, 수정, 삭제
- 사용자별 예약 생성 및 예약 내역 관리
- 예약 취소 및 상태 변경 (`PENDING_PAYMENT → CONFIRMED / CANCELLED`)

### 2. 결제 시스템
- 다양한 결제사(카카오페이, 토스 등) 연동을 위한 확장 가능 구조
- **결제 상태 조회**: `GET /payments/{paymentId}/status`
- **결제사 웹훅 수신 및 처리**: `POST /webhooks/payments/{provider}`

### 3. 동시성 처리
- **낙관적 락**: 동시에 같은 예약을 결제할 경우 → 한 건만 성공, 나머지는 재시도 필요
- **비관적 락**: 동시에 같은 예약을 결제할 경우 → 한 건만 성공, 나머지는 대기/실패 처리
- 동시성 테스트 코드 포함 (`PaymentServiceConcurrencyTest`)

### 4. 공통 API 응답
- 모든 API 응답은 `ApiResponse<T>`로 감싸 일관된 구조 제공
- 성공/실패/에러 응답 표준화

---

## 📖 API 명세 (주요)

| 기능 | 메서드 | 엔드포인트 | 요청 DTO | 응답 DTO |
|------|--------|------------|----------|----------|
| 회의실 생성 | `POST` | `/meeting-rooms` | `MeetingRoomRequest` | `MeetingRoomResponse` |
| 회의실 목록 조회 | `GET` | `/meeting-rooms` | - | `List<MeetingRoomResponse>` |
| 회의실 단건 조회 | `GET` | `/meeting-rooms/{id}` | - | `MeetingRoomResponse` |
| 회의실 삭제 | `DELETE` | `/meeting-rooms/{id}` | - | `ApiResponse<Void>` |
| 예약 생성 | `POST` | `/reservations` | `ReservationRequest` | `ReservationResponse` |
| 예약 전체 조회 | `GET` | `/reservations` | - | `List<ReservationResponse>` |
| 예약 단건 조회 | `GET` | `/reservations/{id}` | - | `ReservationResponse` |
| 예약 수정 | `PUT` | `/reservations/{id}` | `ReservationUpdateRequest` | `ReservationResponse` |
| 예약 취소 | `PUT` | `/reservations/{id}/cancel` | - | `ReservationResponse` |
| 결제 처리 | `POST` | `/reservations/{id}/payment` | `PaymentRequest` | `PaymentResponse` |
| 결제 상태 조회 | `GET` | `/reservations/{id}/status` | - | `PaymentStatus` |
| 결제 웹훅 처리 | `POST` | `/webhooks/payments/{provider}` | Provider별 Payload | `ApiResponse<Void>` |

---

## 🏗 아키텍처 설계

### 1. 레이어드 아키텍처 (DDD 기반)
```mermaid
flowchart TB
    subgraph Presentation [Presentation Layer]
        C1[Controller]
    end

    subgraph Application [Application Layer]
        S1[ReservationService]
        S2[PaymentService]
    end

    subgraph Domain [Domain Layer]
        D1[Reservation]
        D2[Payment]
        D3[Repository Interfaces]
    end

    subgraph Infrastructure [Infrastructure Layer]
        JPA1[ReservationJpaRepository]
        JPA2[PaymentJpaRepository]
        Entity1[ReservationEntity]
        Entity2[PaymentEntity]
        Gateway[PaymentGateway 구현체]
    end

    C1 --> S1
    C1 --> S2
    S1 --> D1
    S1 --> D3
    S2 --> D2
    S2 --> D3
    D3 --> JPA1
    D3 --> JPA2
    JPA1 --> Entity1
    JPA2 --> Entity2
    S2 --> Gateway
2. 결제 프로세스 시퀀스 다이어그램
mermaid
복사
편집
sequenceDiagram
    participant U as User
    participant C as PaymentController
    participant S as PaymentService
    participant R as ReservationRepository
    participant P as PaymentRepository
    participant G as PaymentGateway

    U->>C: POST /reservations/{id}/payment
    C->>S: processReservationPayment(id, provider)
    S->>R: findById(id) (with lock)
    R-->>S: Reservation (PENDING_PAYMENT)
    S->>G: processPayment(Payment)
    G-->>S: PaymentResult (SUCCESS)
    S->>P: save(Payment)
    S->>R: update Reservation → CONFIRMED
    S-->>C: PaymentResponse
    C-->>U: 결제 성공 응답
🛠 프로젝트 시작하기
사전 준비
Docker 및 Docker Compose 설치 필요

실행 방법
bash
복사
편집
# 프로젝트 루트 디렉토리 이동
cd wiseai-dev

# 컨테이너 빌드 및 실행
docker-compose up --build
서버 실행 후 👉 http://localhost:8080

📖 API 문서 (Swagger UI)
👉 http://localhost:8080/swagger-ui/index.html

✅ 테스트 실행 방법
bash
복사
편집
./gradlew test
또는 IDE에서 실행

📝 브랜치 전략
dev → 제출용 안정 버전

master / dev2 → 추가 개발 및 확장 사항 포함

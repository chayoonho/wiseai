# 💻 WISE AI 개발 프로젝트 - wiseai-dev  

WISE AI 개발 프로젝트 **wiseai-dev**는 회의실 예약 및 결제 시스템을 개발하는 프로젝트입니다.  
이 문서는 **프로젝트 실행 방법, 기술 스택, 주요 기능, API 명세, 아키텍처 설계, 테스트 전략**을 담고 있습니다.  

---

## 📌 프로젝트 개요
본 프로젝트는 **Spring Boot + Java 기반 백엔드 애플리케이션**으로,  
회의실 예약/결제 관리 및 **확장 가능한 결제 연동 구조**를 제공합니다.  

---

## ⚙️ 기술 스택

| 카테고리   | 기술 스택                | 버전        |
|------------|--------------------------|-------------|
| **백엔드** | Spring Boot              | 3.2.5       |
|            | Spring Data JPA          | 3.2.5       |
|            | Gradle                   | 8.5         |
| **언어**   | Java                     | 17.0.12     |
| **DB**     | MySQL                    | 8.0.35      |
| **컨테이너** | Docker, Docker Compose  | 최신        |
| **API 문서화** | Swagger UI            | springdoc 2.3.0 |
| **테스트** | JUnit 5, Mockito, Testcontainers | 최신 |

---

## 🚀 핵심 기능

### 1. 회의실 예약 및 관리
- 회의실 생성, 조회, 삭제  
- 사용자별 예약 생성 및 예약 내역 관리  
- 예약 취소 및 상태 변경 (`PENDING_PAYMENT → CONFIRMED / CANCELLED`)  

### 2. 결제 시스템
- 다양한 결제사 연동을 고려한 **확장 가능한 Gateway 구조**  
- 결제 상태 조회: `GET /reservations/{id}/status`  
- 결제사 웹훅 수신 및 처리: `POST /webhooks/payments/{provider}`  

### 3. 동시성 처리
- **낙관적 락**: 예약 시 중복 결제 방지 (버전 충돌 → 1건 성공, 나머지는 예외)  
- **비관적 락**: 결제 시 중복 결제 차단 (하나 성공, 나머지는 대기/실패)  
- 동시성 테스트 코드 포함 (`PaymentServiceConcurrencyTest`)  

### 4. 공통 API 응답
- 모든 API 응답은 `ApiResponse<T>` 구조  
- 성공/실패/에러 응답 형식 통일  

---

## 📖 API 명세 (주요)

| 기능             | 메서드 | 엔드포인트                       | 요청 DTO                  | 응답 DTO                |
|------------------|--------|----------------------------------|---------------------------|-------------------------|
| 회의실 생성      | POST   | `/meeting-rooms`                 | MeetingRoomRequest        | MeetingRoomResponse     |
| 회의실 목록 조회 | GET    | `/meeting-rooms`                 | -                         | List<MeetingRoomResponse> |
| 회의실 단건 조회 | GET    | `/meeting-rooms/{id}`            | -                         | MeetingRoomResponse     |
| 회의실 삭제      | DELETE | `/meeting-rooms/{id}`            | -                         | ApiResponse<Void>       |
| 예약 생성        | POST   | `/reservations`                  | ReservationRequest        | ReservationResponse     |
| 예약 전체 조회   | GET    | `/reservations`                  | -                         | List<ReservationResponse> |
| 예약 단건 조회   | GET    | `/reservations/{id}`             | -                         | ReservationResponse     |
| 예약 수정        | PUT    | `/reservations/{id}`             | ReservationUpdateRequest  | ReservationResponse     |
| 예약 취소        | PUT    | `/reservations/{id}/cancel`      | -                         | ReservationResponse     |
| 결제 처리        | POST   | `/reservations/{id}/payment`     | PaymentRequest            | PaymentResponse         |
| 결제 상태 조회   | GET    | `/reservations/{id}/status`      | -                         | PaymentStatus           |
| 결제 웹훅 처리   | POST   | `/webhooks/payments/{provider}`  | Provider별 Payload        | ApiResponse<Void>       |

---

## 🏗 아키텍처 설계

### 1. 레이어드 아키텍처 (DDD 기반)
- **Presentation 계층**: Controller, `ApiResponse<T>` 응답 통일  
- **Application 계층**: Service, 비즈니스 로직 및 트랜잭션 관리  
- **Domain 계층**: 순수 도메인 모델 (Reservation, Payment, MeetingRoom)  
- **Infrastructure 계층**: JPA Entity, Repository 구현, 외부 PG 연동  

### 2. 결제 프로세스 흐름
1. 사용자가 `POST /reservations/{id}/payment` 호출  
2. `PaymentService`에서 예약 상태 확인 (`PENDING_PAYMENT`)  
3. 결제 Gateway(PG사 연동) 호출 → 결제 요청 처리  
4. 결제 성공 시: `Payment` 저장, 예약 상태 `CONFIRMED` 변경  
5. 결제 실패 시: 예약 상태 `CANCELLED` 변경  
6. 최종적으로 `PaymentResponse` 반환  

---

## 🧪 테스트 전략
- **단위 테스트**: Service 계층 검증 (`ReservationServiceTest`, `PaymentServiceTest`)  
- **통합 테스트**: Repository + Service → DB 연동 검증  
- **동시성 테스트**: `PaymentServiceConcurrencyTest`  
  - 낙관적 락 → 1건 성공, 나머지는 OptimisticLock 예외  
  - 비관적 락 → 1건 성공, 나머지는 “이미 결제됨” 예외  
- **Controller 테스트**: MockMvc 기반 API 호출 테스트  

---

## 🛠 프로젝트 실행하기

### 사전 준비
- Docker, Docker Compose 설치  

### 실행 방법
```bash
# 프로젝트 루트 디렉토리 이동
cd wiseai-dev

# 컨테이너 빌드 및 실행
docker-compose up --build
```

서버 실행 후 👉 http://localhost:8080

---

## 📖 API 문서 (Swagger UI)
👉 http://localhost:8080/swagger-ui/index.html

---

## ✅ 테스트 실행 방법
```bash
./gradlew test
```
또는 IDE에서 직접 실행

---

## 📝 브랜치 전략
- dev → 제출용 안정 버전
- master / dev2 → 추가 개발 및 확장 기능


# 🚚 hub-service

허브, 허브 재고, 허브 간 이동 경로를 관리하는 서비스입니다.  
MSA 환경에서 동작하며, Config Server / Eureka / PostgreSQL / Keycloak / Gateway 와 함께 사용됩니다.

---

## 📌 Overview

`hub-service`는 다음 3가지 도메인을 담당합니다.

- 🏢 **Hub**
  - 허브 생성 / 조회 / 수정 / 삭제
  - 허브 위치 정보(주소, 위도, 경도) 관리

- 📦 **Hub Inventory**
  - 허브 재고 생성 / 조회 / 수정 / 삭제
  - 주문 생성용 재고 차감 / 복원
  - `hubInventoryId` 기반 차감/복원 처리

- 🛣️ **Hub Route**
  - 허브 간 이동 경로 생성 / 조회 / 수정 / 삭제
  - 거리/시간을 직접 입력받지 않고 **카카오 길찾기 계산 결과**로 저장
  - 직행 / 릴레이 경로 조회 지원
  - **200km 기준 정책**으로 최종 배송 경로를 계산

---

## 🧱 Package Structure

```text
src/main/java/com/fhsh/daitda
├─ common
│  ├─ config.security
│  ├─ enums
│  ├─ exception
│  ├─ model
│  └─ util
└─ hubservice
   ├─ hub
   │  ├─ application
   │  ├─ domain
   │  └─ presentation
   ├─ hubinventory
   │  ├─ application
   │  ├─ domain
   │  └─ presentation
   ├─ hubroute
   │  ├─ application
   │  ├─ domain
   │  └─ presentation
   ├─ infrastructure.kakao
   │  ├─ client
   │  ├─ config
   │  ├─ dto
   │  └─ exception
   └─ HubServiceApplication
```
# hub-service

허브 정보, 허브 재고, 허브 간 이동 경로를 관리하는 서비스입니다.  
또한 카카오 길찾기 API를 활용해 허브 간 거리/시간을 계산하고, 200km 정책 기반 최종 배송 경로를 조회합니다.

---

## ⚙️ Tech Stack

-  Java 17
-  Spring Boot 3.5.13
-  Spring Cloud 2025.0.1
-  Spring Data JPA
-  PostgreSQL
-  Eureka Client
-  Spring Cloud Config Client
-  Kakao Mobility Directions API

---

## 🧩 Domain Details

### 🏢 Hub

허브의 기본 정보를 관리합니다.

#### 관리 항목
- 허브명
- 허브 주소
- 위도 / 경도
- 중앙 허브 여부 (`isCentral`)

#### 주요 기능
- 허브 생성
- 전체 허브 조회
- 허브 단건 조회
- 허브 수정
- 허브 논리 삭제

---

### 📦 Hub Inventory

허브별 재고를 관리합니다.

#### 관리 기준
- `hubId`
- `companyId`
- `productId`

#### 주요 기능
- 허브 재고 생성
- 전체 허브 재고 조회
- 허브 재고 단건 조회
- `hubId + companyId + productId` 기준 재고 조회
- 허브 재고 수정
- 허브 재고 논리 삭제
- 주문 생성용 재고 차감
- 재고 복원

#### 내부 정책
- 실제 주문 차감/복원 흐름에서는 **실제로 차감된 `hubInventoryId`** 를 기준으로 복원할 수 있도록 설계했습니다.
- 주문 생성용 차감 API는 `supplierCompanyId + productId` 기준으로 재고 row를 찾습니다.
- 실제로 차감된 `hubInventoryId` 목록을 반환합니다.

---

### 🛣️ Hub Route

허브 간 이동 경로를 관리합니다.

#### 주요 기능
- 허브 경로 생성
- 허브 경로 전체 조회
- 허브 경로 단건 조회
- `srcHubId + destHubId` 기준 경로 조회
- 허브 경로 수정
- 허브 경로 논리 삭제
- 최종 배송 경로(`path`) 조회

#### 경로 생성/수정 방식
허브 경로의 거리(`distance`)와 시간(`durationTime`)은 요청으로 직접 받지 않습니다.

대신 아래 정보를 사용합니다.

- 출발 허브의 좌표
- 도착 허브의 좌표

이 좌표를 기반으로 **Kakao Directions API**를 호출하고, 계산 결과를 기준으로 저장 및 재계산합니다.

#### 최종 배송 경로(`path`) 정책
`HubRouteQueryService`는 최종 배송 경로를 아래 정책으로 계산합니다.

1. 출발 허브와 도착 허브의 **직행 거리**를 먼저 계산
2. 직행 거리가 **200km 미만**이면
   - 직행 1건만 반환
3. 직행 거리가 **200km 이상**이면
   - 중간 경유 허브 1개를 선택
   - 2개 구간으로 나누어 반환

#### 릴레이 허브 선정 기준
- 활성 허브 중 출발/도착 허브를 제외한 후보를 순회
- `[출발 → 후보]`, `[후보 → 도착]` 두 구간 모두 **200km 미만**이어야 함
- 유효 후보 중 **총 거리 합이 가장 짧은 허브**를 릴레이 허브로 선택

---

## 🔐 Authentication Headers

외부 API는 Gateway를 통해 진입하며, 아래 인증 헤더를 사용합니다.

- `X-User-Id`
- `X-User-Email`
- `X-User-Role`

### 지원 Role
- `ADMIN`
- `HUB_ADMIN`
- `DELIVERY`
- `COMPANY`

### 권한 정책
#### MASTER 권한 필요 API
- 현재 인증 시스템에서는 `ADMIN` 으로 검증

#### ALL 권한 API
- 인증된 사용자 역할이면 모두 허용

---

## 🌐 External API

### Hub API

**Base Path:** `/api/v1/hubs`

| Method | Path | Description |
|---|---|---|
| POST | `/api/v1/hubs` | 허브 생성 |
| GET | `/api/v1/hubs` | 전체 허브 조회 |
| GET | `/api/v1/hubs/{hubId}` | 허브 단건 조회 |
| PATCH | `/api/v1/hubs/{hubId}` | 허브 수정 |
| DELETE | `/api/v1/hubs/{hubId}` | 허브 논리 삭제 |

---

### Hub Inventory API

**Base Path:** `/api/v1/hub-inventories`

| Method | Path | Description |
|---|---|---|
| POST | `/api/v1/hub-inventories` | 허브 재고 생성 |
| GET | `/api/v1/hub-inventories` | 전체 허브 재고 조회 |
| GET | `/api/v1/hub-inventories/search?hubId={hubId}&companyId={companyId}&productId={productId}` | 조건 조회 |
| GET | `/api/v1/hub-inventories/{hubInventoryId}` | 허브 재고 단건 조회 |
| PATCH | `/api/v1/hub-inventories/{hubInventoryId}` | 허브 재고 수정 |
| DELETE | `/api/v1/hub-inventories/{hubInventoryId}` | 허브 재고 논리 삭제 |

---

### Hub Route API

**Base Path:** `/api/v1/hub-routes`

| Method | Path | Description |
|---|---|---|
| POST | `/api/v1/hub-routes` | 허브 경로 생성 |
| GET | `/api/v1/hub-routes` | 전체 허브 경로 조회 |
| GET | `/api/v1/hub-routes/{hubRouteId}` | 허브 경로 단건 조회 |
| GET | `/api/v1/hub-routes/search?srcHubId={srcHubId}&destHubId={destHubId}` | 출발/도착 허브 기준 조회 |
| PATCH | `/api/v1/hub-routes/{hubRouteId}` | 허브 경로 재계산 |
| DELETE | `/api/v1/hub-routes/{hubRouteId}` | 허브 경로 논리 삭제 |

---

## 🔗 Internal API

### Hub Internal API

**Base Path:** `/internal/v1/hubs`

| Method | Path | Description |
|---|---|---|
| GET | `/internal/v1/hubs/{hubId}` | 허브 단건 조회 |

---

### Hub Inventory Internal API

**Base Path:** `/internal/v1/hub-inventories`

| Method | Path | Description |
|---|---|---|
| PATCH | `/internal/v1/hub-inventories/decrease` | 재고 차감 |
| PATCH | `/internal/v1/hub-inventories/decrease-by-product` | 주문 생성용 재고 차감 |
| PATCH | `/internal/v1/hub-inventories/restoration` | 재고 복원 |

---

### Hub Route Internal API

**Base Path:** `/internal/v1/hub-routes`

| Method | Path | Description |
|---|---|---|
| GET | `/internal/v1/hub-routes?srcHubId={srcHubId}&destHubId={destHubId}` | 허브 경로 조회 |
| GET | `/internal/v1/hub-routes/path?srcHubId={srcHubId}&destHubId={destHubId}` | 최종 배송 경로(`path`) 조회 |

---

## 📍 Kakao Mobility Integration

허브 경로 계산은 **Kakao Directions API**를 사용합니다.

### 사용 목적
- 허브 경로 생성 시 거리/시간 계산
- 허브 경로 수정 시 거리/시간 재계산
- 최종 배송 경로(`path`) 조회 시 직행/릴레이 여부 판단

### 설정 구조
`kakao.mobility` 설정을 통해 주입받습니다.

#### 예시
```yaml
kakao:
  mobility:
    rest-api-key: ${KAKAO_MOBILITY_REST_API_KEY}
    directions-base-url: https://apis-navi.kakaomobility.com
```

### 인증 헤더
```http
Authorization: KakaoAK {REST_API_KEY}
```

---

## 🧪 Test

테스트는 아래 영역을 포함합니다.

- ✅ `HubInventoryCommandServiceTest`
- ✅ `HubRouteServiceTest`
- ✅ `HubRouteQueryServiceTest`
- ✅ `HubServiceApplicationTests`

### 실행
```bash
./gradlew clean test
```

---

## 🛠️ Local Development

### 1. 필수 선행 서비스
아래 서비스가 먼저 실행되어 있어야 합니다.

- Config Server
- Eureka Server
- PostgreSQL
- Gateway Server
- Keycloak
- Zipkin (선택)

### 2. 환경 변수 준비
`.env.example` 기준으로 필요한 값입니다.

```env
SPRING_PROFILES_ACTIVE=local

DB_PASSWORD=
POSTGRES_HOST=localhost

EUREKA_HOST=localhost
ZIPKIN_HOST=localhost

GITHUB_USERNAME=
GITHUB_TOKEN=

KAKAO_MOBILITY_REST_API_KEY=your_kakao_mobility_rest_api_key
```

#### 환경 변수 설명

| 변수명 | 설명 |
|---|---|
| `DB_PASSWORD` | PostgreSQL 비밀번호 |
| `POSTGRES_HOST` | PostgreSQL 호스트 |
| `EUREKA_HOST` | Eureka Server 호스트 |
| `ZIPKIN_HOST` | Zipkin 호스트 |
| `GITHUB_USERNAME` | GitHub Packages 인증용 사용자명 |
| `GITHUB_TOKEN` | GitHub Packages 인증용 토큰 |
| `KAKAO_MOBILITY_REST_API_KEY` | 카카오 길찾기 API 호출용 REST API Key |

### 3. application 설정

#### `application.yaml`
- 서비스명: `hub-service`
- 기본 profile: `local`
- Config Server 사용
- Config Server import는 optional

#### `application-local.yaml`
- 로컬 DB 비밀번호 주입
- Hibernate SQL 로그 출력
- 애플리케이션 DEBUG 로그 출력

---

## ▶️ Run

```bash
./gradlew bootRun
```

또는 IntelliJ에서 `HubServiceApplication`을 실행합니다.

---

## 📦 Dependency Notes

주요 의존성은 아래와 같습니다.

- `spring-boot-starter-data-jpa`
- `spring-boot-starter-web`
- `spring-boot-starter-actuator`
- `spring-cloud-starter-netflix-eureka-client`
- `spring-cloud-starter-openfeign`
- `spring-cloud-starter-config`
- `spring-cloud-starter-circuitbreaker-reactor-resilience4j`
- `resilience4j-spring-boot3`
- `spring-boot-starter-validation`
- `common:0.1.4-SNAPSHOT`
- `postgresql`
- `h2 (test)`

---

## 🧠 Design Notes

### Soft Delete
모든 주요 엔티티는 논리 삭제를 사용합니다.

- `deletedAt`
- `deletedBy`

조회는 기본적으로 `deletedAt is null` 기준입니다.

### Hub Route Unique Constraint
허브 경로는 아래 조합이 유일해야 합니다.

- `srcHubId`
- `destHubId`

중복 생성 시 `HUB_ROUTE_CONFLICT` 예외를 반환합니다.

### Route Path 응답 특징
최종 배송 경로(`path`) 조회는 단순히 저장된 경로 조회만 의미하지 않습니다.

정책 기반으로 계산된 배송 구간도 응답에 포함할 수 있습니다.

- 저장된 구간이면 `hubRouteId` 포함
- 계산만 된 구간이면 `hubRouteId = null`

즉, `hubRouteId == null` 은 오류가 아니라,  
**계산은 되었지만 DB에 저장된 허브 경로 row는 없는 상태**를 의미합니다.

---

## 📂 Project Files

```text
.
├─ build.gradle
├─ settings.gradle
├─ .env.example
├─ src
│  ├─ main
│  │  ├─ java
│  │  └─ resources
│  └─ test
│     ├─ java
│     └─ resources
└─ gradle
```

---

## ✅ Summary

이 서비스는 다음을 책임집니다.

- 🏢 허브 관리
- 📦 허브 재고 관리
- 🛣️ 허브 간 이동 경로 관리
- 📍 카카오 길찾기 기반 거리/시간 계산
- 🚚 200km 정책 기반 직행/릴레이 최종 배송 경로 조회

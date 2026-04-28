# TamTam

Spring Boot 멀티모듈 프로젝트입니다.

- `tamtam-core`: 공통 도메인, repository, TourAPI 연동 client/dto
- `tamtam-api`: 사용자 인증/회원 관련 REST API
- `tamtam-batch`: 관광 데이터 적재용 Spring Batch

## 모듈

| 모듈 | 역할 |
| --- | --- |
| `tamtam-core` | 공통 엔티티, 공통 인프라, TourAPI Feign client |
| `tamtam-api` | 회원가입, 로그인, 토큰 재발급 API |
| `tamtam-batch` | TourAPI 기반 관광지/상세/이미지 적재 배치 |

## API 실행

```bash
./gradlew :tamtam-api:bootRun
```

Swagger UI:

- `http://localhost:8080/swagger-ui/index.html`

## Batch 개요

현재 배치는 한국관광공사 TourAPI를 사용해 충남(`lDongRegnCd=44`) 관광지 데이터를 적재합니다.

대상 API:

- `areaBasedList2`: 관광지 기본 목록 수집
- `detailCommon2`: 관광지 상세 설명 보강
- `detailImage2`: 관광지 이미지 목록 보강

배치는 chunk 기반으로 동작하며, 저장은 JPA가 아닌 JDBC writer를 사용합니다.

## Batch Job 구성

메인 Job 이름:

- `tourAreaBasedListJob`

Step 구성:

| 순서 | Step | 역할 |
| --- | --- | --- |
| 1 | `tourAreaBasedListStep` | `areaBasedList2` 호출 후 관광지 기본 정보 upsert |
| 2 | `detailCommonStep` | 상세 설명/홈페이지 보강 |
| 3 | `detailImageStep` | 이미지 테이블 동기화 |

보조 Job:

- `noopJob`

## Step 상세

### 1. `tourAreaBasedListStep`

구성:

- Reader: `AreaBasedListItemReader`
- Processor: `TourAttractionItemProcessor`
- Writer: `TourAttractionJdbcItemWriter`

동작:

- TourAPI `areaBasedList2`를 페이지 단위로 읽음
- `content_id` 기준으로 `tour_attractions` 테이블 upsert

### 2. `detailCommonStep`

구성:

- Reader: `JdbcCursorItemReader<DetailCommonTarget>`
- Processor: `DetailCommonItemProcessor`
- Writer: `DetailCommonJdbcItemWriter`

동작:

- `tour_attractions`에서 상세 보강이 필요한 대상만 조회
- `detailCommon2` 호출
- `homepage`, `overview` 및 동기화 메타데이터 갱신

대상 조건:

- `detail_common_synced_at IS NULL`
- 또는 `detail_common_source_modified_time <> modified_time`

### 3. `detailImageStep`

구성:

- Reader: `JdbcCursorItemReader<DetailImageTarget>`
- Processor: `DetailImageItemProcessor`
- Writer: `DetailImageJdbcItemWriter`

동작:

- `tour_attractions`에서 이미지 보강이 필요한 대상만 조회
- `detailImage2` 호출
- 해당 `content_id`의 기존 이미지를 삭제 후 재삽입
- 이미지 동기화 메타데이터 갱신

대상 조건:

- `detail_image_synced_at IS NULL`
- 또는 `detail_image_source_modified_time <> modified_time`

## 배치 테이블

### `tour_attractions`

관광지 기본 정보 + 상세/이미지 동기화 상태를 저장합니다.

주요 컬럼:

| 컬럼 | 설명 |
| --- | --- |
| `content_id` | TourAPI 관광지 고유 ID |
| `content_type_id` | 콘텐츠 타입 ID |
| `title` | 관광지명 |
| `addr1`, `addr2` | 주소 |
| `tel` | 전화번호 |
| `first_image`, `first_image2` | 대표 이미지 |
| `map_x`, `map_y` | 좌표 |
| `modified_time` | 원본 수정일 |
| `homepage` | 상세 홈페이지 |
| `overview` | 상세 소개 |
| `detail_common_source_modified_time` | 상세 설명 동기화 기준 원본 수정일 |
| `detail_common_synced_at` | 상세 설명 마지막 동기화 시각 |
| `detail_image_source_modified_time` | 이미지 동기화 기준 원본 수정일 |
| `detail_image_synced_at` | 이미지 마지막 동기화 시각 |

유니크 키:

- `uk_tour_attractions_content_id (content_id)`

### `tour_attraction_images`

관광지 상세 이미지 목록을 저장합니다.

주요 컬럼:

| 컬럼 | 설명 |
| --- | --- |
| `content_id` | 관광지 ID |
| `image_name` | 이미지 이름 |
| `origin_image_url` | 원본 이미지 URL |
| `small_image_url` | 썸네일 URL |
| `serial_num` | 이미지 순번 |

유니크 키:

- `uk_tour_attraction_images_content_origin (content_id, origin_image_url)`

## Batch 실행 방법

### IntelliJ에서 실행

실행 클래스:

- [BatchApplication.java](/C:/Project/Study/tamtam/tamtam-batch/src/main/java/com/tamtam/batch/BatchApplication.java)



Program arguments:

```text
--spring.batch.job.enabled=true --spring.batch.job.name=tourAreaBasedListJob
```



## 배치 정책

### 공통 예외

배치 외부 API 예외는 공통 `BatchApiException`으로 관리합니다.

처리 유형:

- `RETRYABLE`
- `SKIPPABLE`
- `FATAL`

### 재시도 / 스킵

현재 `detailImageStep`에는 fault-tolerant 설정이 들어가 있습니다.

- 429, 5xx: 재시도 대상
- 재시도 소진 후에도 처리 불가 시 skip 가능
- 잘못된 응답 파싱 등은 skippable 처리 가능

관련 설정 클래스:

- `DetailImageBatchProperties`
- `BatchApiRetryPolicy`
- `BatchApiStatusAwareBackOffPolicy`

## 로컬 개발 참고

- 배치 DB는 H2 파일 DB를 사용합니다.
- TourAPI 호출량이 많으면 `429 Too Many Requests`가 발생할 수 있습니다.
- `detailImageStep`은 외부 API quota 영향을 가장 많이 받습니다.


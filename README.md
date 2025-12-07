#  📆 Holiday-Keeper

Nager.Date API를 활용하여 전 세계 공휴일 정보를 관리하는 서비스입니다.


## 🛠 기술 스택

- **Language:** Java 21
- **Framework:** Spring Boot 3.4.12, QueryDSL 5.0.0
- **Test:** JUnit 5, Mockito 4.3.1
- **Build Tool:** Gradle
- **Database:** H2
- **External API:** Nager.Date API v3
- **Documentation:** Swagger/OpenAPI

## 📚 수행 목록

- [x] Nager.Date API를 활용한 국가·공휴일 데이터 적재
- 최근 5 년(2020 ~ 2025)의 공휴일을 외부 API에서 수집하여 저장
- 최초 실행시 시 5 년 × N 개 국가를 **일괄 적재**하는 기능 포함
- **⚠️ 데이터 적재는 대략 5초 정도의 시간이 소요됩니다.**
  
- [x] 공휴일 데이터 검색
- 연도별·국가,공휴일 타입 등의 필터 기반 공휴일 조회
- 검색 결과는 페이징 형태로 응답

- [x] 공휴일 데이터 재동기화(Refresh)
- 특정 연도·국가 데이터를 재호출하여 Upsert(덮어쓰기)

- [x] 공휴일 데이터 삭제
- 특정 연도·국가의 공휴일 레코드 전체 삭제
- 삭제는 soft-delete로 구성

- [x] 배치 자동화
- 매년 **1 월 2 일 01:00 KST** 에 전년도·금년도 데이터를 자동 동기화

- [x] 테스트 코드 작성
- JUnit 5와 Mockito를 활용한 테스트 코드


## 🚀 빌드 및 실행 방법

1️⃣ 프로젝트 클론

```bash
git clone https://github.com/kim0527/Holiday-Keeper.git
cd Holiday-Keeper
```

2️⃣ 빌드 & 실행

```bash
./gradlew clean build
./gradlew bootRun
```
## ✅ `./gradlew clean test` 스크린샷

<img height="800" alt="Image" src="https://github.com/user-attachments/assets/5b8473a9-b480-4ce6-8e9a-d962020c0c7d" />


## 📡 API 명세 요약

### 1. 공휴일 조회

```http
GET /api/v1/holidays?year=2025&countryCode=KR&holidayType=Public&sortType=date&sortOrder=ASC&page=0&size=10
```

**Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|:----:|--------|------|
| page | Integer | ❌ | 0 | 페이지 번호 (0부터 시작) |
| size | Integer | ❌ | 10 | 페이지 크기 |
| sortType | String | ❌ | date | 정렬 기준 |
| sortOrder | String | ❌ | DESC | 정렬 순서 (ASC/DESC) |
| year | Integer | ❌ | - | 조회할 연도 |
| countryCode | String | ❌ | - | 국가 코드 (예: KR, US) |
| holidayType | String | ❌ | - | 공휴일 타입 (예: Public) |



### 2. 공휴일 데이터 동기화

```http
POST /api/v1/holidays/{countryCode}/{year}
```

**Path Parameters**

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|:----:|------|
| countryCode | String | ✅ | 국가 코드 (예: KR) |
| year | Integer | ✅ | 대상 연도 (예: 2025) |


### 3. 공휴일 삭제

```http
DELETE /api/v1/holidays/{countryCode}/{year}
```

**Path Parameters**

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|:----:|------|
| countryCode | String | ✅ | 국가 코드 (예: KR) |
| year | Integer | ✅ | 대상 연도 (예: 2025) |



## 📚 API 문서

### Swagger UI 접속

애플리케이션 실행 후 다음 URL로 접속하면 API 문서를 확인할 수 있습니다:

```
http://localhost:19090/holiday-keeper-apis
```

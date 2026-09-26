# 블로그 검색 서비스

## 2026-09 개선 사항과 API 탐색기

이 저장소의 원본은 `SearchExtension` 의 Java 17 / Spring Boot 3.1.1 백엔드입니다. 2026-09 작업에서는 두 엔드포인트와 응답 형식·오류 코드를 유지하며 검색 처리와 실제 서버 연결을 보완했습니다. 운영 전환과 재현 방법은 [NATIVE-RUNBOOK.md](NATIVE-RUNBOOK.md)에 있습니다.

### Java 백엔드: 바뀐 것

| 파일 | 변경 |
|------|------|
| `ApiSearchController` | `PageRequest.of(page, pageSize)` 보다 먼저 `ExceptionHandlerUtil.isValidParameter` 를 호출합니다. 전에는 `page=-1`, `pageSize=0`, `pageSize=-5` 가 `PageRequest` 의 `IllegalArgumentException` 으로 500 이 됐고, 이제는 기존 `INVALID_PARAMETER_PAGE`(402)로 응답합니다. `page` 는 전과 같이 1부터 시작하는 외부 페이지 번호로 전달합니다. |
| `GlobalExceptionHandler` | `page`, `pageSize` 에 숫자가 아닌 값이 오면(`MethodArgumentTypeMismatchException`) 500 대신 `INVALID_PARAMETER_PAGE`(402)로 응답합니다. 일반 서버 오류는 내부 예외 내용을 노출하지 않습니다. |
| `build.gradle` | 기본 `test` 에서 `integration` 태그를 빼고, 실제 API 를 부르는 테스트용 `integrationTest` 태스크를 추가했습니다. |
| `TestKakaoApiSearchBlog`, `TestNaverApiSearchBlog` | 소스에 적혀 있던 키를 지우고 환경 변수로 받는 opt-in JUnit 테스트로 바꿨습니다. 기본 빌드에서는 실행하지 않습니다. |
| `SearchExtensionApplicationTests` | 테스트가 인메모리 H2 와 자리표시자 키만 쓰는지 확인하는 테스트를 추가했습니다. |

새 테스트: `ApiSearchControllerContractTest`(파라미터 계약 18개), `ApiBlogSearchServiceFallbackTest`(카카오 → 네이버 대체와 실패 코드), `BlogSearchProviderHttpTest`(로컬 HTTP 스텁으로 요청 매핑과 응답 가공 확인). 테스트 전용 설정 `src/test/resources/application.properties` 는 실제 키를 읽지 않고, 홈 디렉터리에 H2 파일을 만들지 않습니다.

### 검색 처리와 실행 구성

- 네이버 `start`는 `(page - 1) * pageSize + 1`로 계산해 페이지 중복을 고쳤습니다. 1000을 넘는 시작 위치는 기존 402 오류로 거절합니다.
- 공급자 요청에 시간 제한을 적용했습니다. 공급자 선택 후 검색 기록을 저장하므로 DB 실패가 다른 공급자 호출을 일으키지 않습니다.
- 인기 검색어 집계의 삭제·삽입을 하나의 거래로 묶고 스케줄러를 단일 작업으로 등록했습니다.
- 기본 설정은 격리된 H2와 loopback 주소를 사용합니다. 과거 실행 JAR와 DB 파일은 Git 및 새 실행 JAR에서 제외하고, 현재 소스에서 다시 빌드합니다.
- 검증: Java 40개, 탐색기 단위 54개, 데모 DOM 33개, 실제 연결 모드 DOM 11개. 실제 Spring HTTP 엔드포인트는 loopback 공급자와 H2로 검사했습니다. 브라우저 화면과 운영 공급자는 검증하지 않았습니다.

### API 탐색기 (`portfolio-demo/`)

원본에는 프론트엔드가 없어서 이 저장소에 정적 HTML/CSS/JS 로 된 API 탐색기를 추가했습니다.

- `scripts/extract-contract.mjs` 가 원본 컨트롤러, 서비스, DTO, `ErrorResponse`, `ApiConstants`, 테스트 fixture 에서 계약을 추출해 `src/generated/contract.js` 를 만듭니다. `application.properties` 는 읽지 않습니다.
- `src/mock-adapter.js` 가 컨트롤러 → 서비스 → 카카오/네이버 순서를 브라우저 안에서 재현합니다. 응답 봉투, HTTP 상태, 공급자별 결과 형식은 추출한 계약을 따릅니다.
- 모의 경계: 외부 검색 API 는 호출하지 않습니다. 검색 문서는 질의어로 만든 모의 데이터이고 링크 필드는 비워 둡니다. 인기 검색어는 이 브라우저의 localStorage 에만 저장합니다(초기화, 손상 값 복구 포함).
- Java 백엔드는 Vercel 에 배포하지 않습니다. 공개 정적 산출물은 `portfolio-demo/dist`의 허용 목록 10개 파일입니다. 실제 서버 빌드는 같은 탐색기의 `live.html`을 `/console/`에 포함해 원본 API로 연결합니다.
- `SearchExtension/src/test/resources/contract/search-parameter-cases.json` 은 Java 계약 테스트와 모의 어댑터 테스트가 함께 씁니다.

### 실행 방법

```bash
# 백엔드 (JDK 17)
cd SearchExtension
sh ./gradlew test            # 외부 API 호출 없음
sh ./gradlew build
SEARCH_API_LIVE_TESTS=true KAKAO_API_KEY=... NAVER_CLIENT_ID=... NAVER_CLIENT_SECRET=... sh ./gradlew integrationTest   # 실제 API (선택)

# API 탐색기 (Node 20+)
cd ../portfolio-demo
npm ci
npm run test:all
npm run build                # 계약 최신 여부 확인 후 dist/ 생성
npm run extract              # 원본 Java 를 바꾼 뒤 계약 다시 생성
```

---

로컬 실행은 소스에서 만든 `SearchExtension/build/libs/SearchExtension-0.0.1-SNAPSHOT.jar`를 사용합니다. 가상 공급자 실행과 환경 변수는 `NATIVE-RUNBOOK.md`의 명령을 따릅니다. API 경로는 `/search`와 `/popularKeyword`, 실제 연결 화면은 `/console/live.html`입니다.

====================================================

## 목차
1 기능 요구사항

2 개발환경

3 DB 모델링

4 API 정리
- 4.1 카카오 API
- 4.2 네이버 API

5 프로젝트 구조

6 참고 자료 

====================================================

## 1 기능 요구사항

	- JAVA 11 이상 또는 Kotlin 사용
	- Spring Boot 사용
	- Gradle 기반의 프로젝트
	- 블로그 검색 API는 서버(백엔드)에서 연동 처리
	- DB는 인메모리 DB(예: h2)를 사용하며 DB 컨트롤은 JPA로 구현
	- 외부 라이브러리 및 오픈소스 사용 가능 (단, README 파일에 사용한 오픈 소스와 사용 목적을 명시해 주세요)
	
	1. 블로그 검색
		- 키워드를 통해 블로그를 검색할 수 있어야 합니다.
		- 검색 결과에서 Sorting(정확도순, 최신순) 기능을 지원해야 합니다.
		- 검색 결과는 Pagination 형태로 제공해야 합니다.
		- 검색 소스는 카카오 API의 키워드로 블로그 검색(https://developers.kakao.com/docs/latest/ko/daum-search/dev-guide#search-blog)을 활용합니다.
		- 추후 카카오 API 이외에 새로운 검색 소스가 추가될 수 있음을 고려해야 합니다.
	2. 인기 검색어 목록
		- 사용자들이 많이 검색한 순서대로, 최대 10개의 검색 키워드를 제공합니다.
		- 검색어 별로 검색된 횟수도 함께 표기해 주세요.
		
	- 프로젝트 구성 추가 요건
		*멀티 모듈 구성 및 모듈간 의존성 제약 @TODO
	
	- Back-end 추가 요건
		*트래픽이 많고, 저장되어 있는 데이터가 많음을 염두에 둔 구현
		(검토 : 인덱스, 커넥션풀링(HikariCP), DB 파티션닝, 캐싱, 배치 업데이트, 비동기 처리(정확도 이슈 때문에 제외) , 로드 밸런싱, 성능 테스트)
		*동시성 이슈가 발생할 수 있는 부분을 염두에 둔 구현 (예시. 키워드 별로 검색된 횟수의 정확도)
		*카카오 블로그 검색 API에 장애가 발생한 경우, 네이버 블로그 검색 API를 통해 데이터 제공
		*네이버 블로그 검색 API: https://developers.naver.com/docs/serviceapi/search/blog/blog.md

====================================================

## 2 개발 환경 

- Language : **Java 17**
- FrameWork : **Spring Boot 3.1.1.RELEASE + Spring JPA**
- Database : **H2 2.1.214.RELEASE** 
- Connection Pooling : **HikariCP**
- Utility :
  	- **Spring Scheduling**  	(트래픽이 많고, 저장되어 있는 데이터가 많음을 염두에 두어 인기 키워드 생성은 배치로 5초마다 생성)
  	- **Resilience4j**  		(API 장애 발생 대응 사용)
  	- **QueryDSL** 			(복잡한 쿼리 구성을 위해서 사용)
  	- **Junit5**
  	- **Log4j2**

====================================================

## 3 DB 모델링 

| POPULAR_KEYWORD | (블로그 인기 검색어 목록)  |		  | 		
|-----------------|---------------------------|-----------|
| KEYWORD_ID      | NUMBER                    | PK        |
| KEYWORD         | VARCHAR                   | NOT NULL  | 
| COUNT           | NUMBER                    | DEFAULT 0 | 
| CREATED_TIME    | TIMESTAMP                 | NOT NULL  |


| SEARCH_KEYWORD_HISTORY | (블로그 검색 기록)  |           |
|------------------------|--------------------|-----------|
| KEYWORD_ID             | NUMBER             | PK        |
| KEYWORD                | VARCHAR            | NOT NULL  |
| COUNT                  | NUMBER             | DEFAULT 0 |
| API_SOURCE             | VARCHAR            | NOT NULL  |
| CREATED_TIME           | TIMESTAMP          | NOT NULL  |
	
====================================================	
	
## 4 API 정리

	4.1 카카오 API - https://developers.kakao.com/docs/latest/ko/daum-search/dev-guide#search-blog
		### 쿼리 파라미터
		이름	타입	설명	필수
		query	String	검색을 원하는 질의어	O
		sort	String	(accuracy,recency) 결과 문서 정렬 방식, accuracy(정확도순) 또는 recency(최신순), 기본 값 accuracy	X
		page	Integer	결과 페이지 번호, 1~50 사이의 값, 기본 값 1	X
		size	Integer	한 페이지에 보여질 문서 수, 1~50 사이의 값, 기본 값 10	X
		
	4.2 네이버 API - https://developers.naver.com/docs/serviceapi/search/blog/blog.md
		### 파라미터
		파라미터	타입	필수 여부	설명
		query	String	Y	검색어. UTF-8로 인코딩되어야 합니다.
		sort	String	N	(sim,date) 검색 결과 정렬 방법 (sim: 정확도순으로 내림차순 정렬(기본값), date: 날짜순으로 내림차순 정렬)
		start	Integer	N	검색 시작 위치(기본값: 1, 최댓값: 1000)
		display	Integer	N	한 번에 표시할 검색 결과 개수(기본값: 10, 최댓값: 100)
	
====================================================

## 5 프로젝트 구조

```md             
├─main
│  ├─generated				# dsl Q클래스 파일
│  │  └─com
│  │      └─search
│  │          └─extension
│  │              └─apiSearch
│  │                  └─domain
│  ├─java
│  │  └─com
│  │      └─search
│  │          └─extension
│  │              ├─apiSearch      	# 검색 모듈
│  │              │  ├─adapter
│  │              │  │  ├─persistence	 	 # Repository
│  │              │  │  └─web			 # Controller
│  │              │  ├─application
│  │              │  │  ├─exception		 # ExceptionHandlers	
│  │              │  │  ├─port			 # Interface
│  │              │  │  ├─service		 # Business Logic
│  │              │  │  └─utils			 # Common Utility
│  │              │  └─domain			 # Entity
│  │              │      └─model		 # DTO
│  │              ├─config	  	# 모듈 설정
│  │              └─scheduledTask 	# 배치 모듈
│  └─resources
│      ├─db
│      └─META-INF
└─test
    └─java
        └─com
            └─search
                └─extension	  	# 테스트 케이스 모듈
```

====================================================

## 6 참고 자료
- Search API

https://developers.kakao.com/docs/latest/ko/daum-search/dev-guide#search-blog

https://developers.naver.com/docs/serviceapi/search/blog/blog.md

- H2 Database

http://www.h2database.com/html/features.html#connection_modes

https://www.baeldung.com/spring-boot-h2-database
	
- Resilience4j - Circuit Breaker

https://resilience4j.readme.io/docs/circuitbreaker

- QueryDSL

https://velog.io/@juhyeon1114/Spring-QueryDsl-gradle-%EC%84%A4%EC%A0%95-Spring-boot-3.0-%EC%9D%B4%EC%83%81

- JPA

https://data-make.tistory.com/621

- Cache 검토

https://gosunaina.medium.com/cache-redis-ehcache-or-caffeine-45b383ae85ee

- ExceptionHandling

https://velog.io/@kiiiyeon/%EC%8A%A4%ED%94%84%EB%A7%81-ExceptionHandler%EB%A5%BC-%ED%86%B5%ED%95%9C-%EC%98%88%EC%99%B8%EC%B2%98%EB%A6%AC#controlleradvice-vs-restcontrolleradvice

- TaskSchedule

https://www.baeldung.com/spring-task-scheduler

- GPT

https://chat.openai.com/share/ffcbb912-6af9-41c0-9057-05b977545612

https://wrtn.ai/share/zf52CubFFq

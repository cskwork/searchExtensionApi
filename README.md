# 블로그 검색 서비스

1) 지원자 정보 : 2126-000513_채성국_서버 개발자
2) 프로젝트 설명 하단
3) 다운로드 링크
	
 	- https://github.com/cskwork/searchCustomizer/raw/main/SearchExtension/jar/SearchExtension-0.0.1-SNAPSHOT.jar
4) 빌드 실행 (JDK 17 이상 사용 (Spring Boot 3.0 이상 호환 - https://jdk.java.net/java-se-ri/17)
```bash
java -jar SearchExtension-0.0.1-SNAPSHOT.jar
```
6) 실행 URL
	- http://localhost:8080/search?query=abc&sort=accuracy&page=1&pageSize=10
	- http://localhost:8080/popularKeyword
	
====================================================

## 목차
1 과제 기능 요구사항

2 개발환경

3 DB 모델링

4 API 정리
- 4.1 카카오 API
- 4.2 네이버 API

5 프로젝트 구조

6 참고 자료 

====================================================

## 1 과제 기능 요구사항

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
		*멀티 모듈 구성 및 모듈간 의존성 제약
	
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

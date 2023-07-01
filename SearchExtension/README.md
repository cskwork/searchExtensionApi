# 블로그 검색 서비스

## 과제 기능 요구사항

### 환경설정

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
	(인덱스, 커넥션풀링(HikariCP), DB 파티션닝, 캐싱, 배치 업데이트, 비동기 처리(정확도 이슈 때문에 제외) , 로드 밸런싱, 성능 테스트)
	*동시성 이슈가 발생할 수 있는 부분을 염두에 둔 구현 (예시. 키워드 별로 검색된 횟수의 정확도)
	*카카오 블로그 검색 API에 장애가 발생한 경우, 네이버 블로그 검색 API를 통해 데이터 제공
	* 네이버 블로그 검색 API: https://developers.naver.com/docs/serviceapi/search/blog/blog.md

## 개발 환경 

- Language : **Java 11**
- FrameWork : **Spring Boot 2.3.6.RELEASE + Spring JPA + Junit 5**
- Database : **H2 1.4.199.RELEASE** 
- Connection Pooling : HikariCP
- Utility : Caffeine cache, TimedTask
	
## DB 모델링 

API_SEARCH_POPULAR_KEYWORD (블로그 인기 검색어 목록)
	KEYWORD_ID		NUMBER 		PK
	KEYWORD			VARCHAR		NOT NULL
	COUNT			NUMBER		DEFAULT 0
	API_SOURCE		VARCHAR		NULL
	CREATED_TIME	TIMESTAMP	NOT NULL
	UPDATED_TIME 	TIMESTAMP	NOT NULL
	
API_SEARCH_USER_REQUEST (블로그 검색 인입 사용자)
	USER_ID			VARCHAR 	PK
	KEYWORD_ID		NUMBER 		FK
	REQUEST_IP		VARCHAR		NULL
	REQUEST_COUNT	NUMBER		DEFAULT 0
	CREATED_TIME	TIMESTAMP	NOT NULL
	UPDATED_TIME 	TIMESTAMP	NOT NULL
		
## API 정리 


## 참고 자료 
https://velog.io/@bey1548/spring-boot-configuration-processor
https://data-make.tistory.com/621
https://gosunaina.medium.com/cache-redis-ehcache-or-caffeine-45b383ae85ee
https://chat.openai.com/share/ffcbb912-6af9-41c0-9057-05b977545612
https://wrtn.ai/share/zf52CubFFq

### 공통 오류 처리 

Code	Description
200		정상 응답
400		잘못된 요청
404		리소스를 찾을 수 없음
500		각종 오류 처리

```
// 정상 응답 예시
{
"code": "200",
"message": "정상 처리",
"body": {
"rcvAmt": 10000
   }
}

// 오류 응답 예시
{
"errorStatus": "INTERNAL_SERVER_ERROR",
"errorCode": 500,
"errorMessage": "뿌린 사용자만 조회가 가능합니다."
}
```


# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/3.1.1/gradle-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/3.1.1/gradle-plugin/reference/html/#build-image)
* [Spring Boot DevTools](https://docs.spring.io/spring-boot/docs/3.1.1/reference/htmlsingle/#using.devtools)
* [Spring Configuration Processor](https://docs.spring.io/spring-boot/docs/3.1.1/reference/htmlsingle/#appendix.configuration-metadata.annotation-processor)
* [Spring Data JPA](https://docs.spring.io/spring-boot/docs/3.1.1/reference/htmlsingle/#data.sql.jpa-and-spring-data)

### Guides
The following guides illustrate how to use some features concretely:

* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)

### Additional Links
These additional references should also help you:

* [Gradle Build Scans – insights for your project's build](https://scans.gradle.com#gradle)


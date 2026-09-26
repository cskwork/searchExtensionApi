// 자동 생성 파일입니다. 직접 수정하지 마세요.
// 생성: node scripts/extract-contract.mjs (원본: ../SearchExtension)
export const CONTRACT = Object.freeze({
  "source": {
    "repository": "cskwork/searchExtensionApi",
    "backend": "SearchExtension (Java 17, Spring Boot 3.1.1, Gradle)",
    "hosting": "Java 백엔드는 Vercel 에 배포되지 않습니다. 배포 대상은 이 API 탐색기와 브라우저 내 모의 어댑터뿐입니다.",
    "files": [
      {
        "key": "controller",
        "path": "SearchExtension/src/main/java/com/search/extension/apiSearch/adapter/web/ApiSearchController.java",
        "sha256": "e585ae6ced83e73b65feb18b2b90c246d8883fad7ba0f94f048cbf7ea7e28007"
      },
      {
        "key": "validation",
        "path": "SearchExtension/src/main/java/com/search/extension/apiSearch/application/utils/ExceptionHandlerUtil.java",
        "sha256": "b1344b82afe65a90a6422a624319de4461f89178c6f8023fb282f0d272f0e761"
      },
      {
        "key": "exceptionHandler",
        "path": "SearchExtension/src/main/java/com/search/extension/apiSearch/application/exception/GlobalExceptionHandler.java",
        "sha256": "b0b6ac2c0a8d7d2e601ce1ed0bcb595bf69597949de7e6f402fb3d9695e5045b"
      },
      {
        "key": "orchestration",
        "path": "SearchExtension/src/main/java/com/search/extension/apiSearch/application/service/ApiBlogSearchServiceImpl.java",
        "sha256": "92649013dfd898aa7384a1dacd3a5326c4a2df821142466aff0e84635c7498da"
      },
      {
        "key": "kakaoService",
        "path": "SearchExtension/src/main/java/com/search/extension/apiSearch/application/service/KakaoBlogSearchServiceImpl.java",
        "sha256": "9139f7a6ec79471b2a1df96626bfa394a87648906883898cee428fb2412a5f0a"
      },
      {
        "key": "naverService",
        "path": "SearchExtension/src/main/java/com/search/extension/apiSearch/application/service/NaverBlogSearchServiceImpl.java",
        "sha256": "4f29c57623ab9ae1618e983510f1dfc8dafc2a9df5a357641f617a09a8059d99"
      },
      {
        "key": "popularRepository",
        "path": "SearchExtension/src/main/java/com/search/extension/apiSearch/adapter/persistence/PopularKeywordQueryRepository.java",
        "sha256": "321c91d239fdd1785246bad84f1fa9e6e941f2dbb7852d8ea86d31031a2f37e2"
      },
      {
        "key": "scheduler",
        "path": "SearchExtension/src/main/java/com/search/extension/scheduledTask/TaskSchedulerConfig.java",
        "sha256": "8bf60a17c84966e14d646bcd3948a73c3cad6c721ca992d1f0d7ad781e5d10ad"
      },
      {
        "key": "errors",
        "path": "SearchExtension/src/main/java/com/search/extension/apiSearch/domain/model/ErrorResponse.java",
        "sha256": "35e253d834623ef33a18c0960bd62aa51162649226fd47a78318a3375c4064a1"
      },
      {
        "key": "constants",
        "path": "SearchExtension/src/main/java/com/search/extension/apiSearch/domain/model/ApiConstants.java",
        "sha256": "f3c4aab76b9dbbacca6b556d72c002034e03ff7261407f358ff7415f451408b0"
      },
      {
        "key": "responseDto",
        "path": "SearchExtension/src/main/java/com/search/extension/apiSearch/domain/model/ResponseDTO.java",
        "sha256": "1e647b51c93f52d01759f6f71a40e384cb4ca9c52ef11bdd413ba7003dfaa8bb"
      },
      {
        "key": "errorDto",
        "path": "SearchExtension/src/main/java/com/search/extension/apiSearch/domain/model/ErrorResponseDTO.java",
        "sha256": "b4e470c03db70be7b9809659d8469ed2c7916ed69fa030036b2daf56448847c8"
      },
      {
        "key": "popularDto",
        "path": "SearchExtension/src/main/java/com/search/extension/apiSearch/domain/model/PopularKeywordDTO.java",
        "sha256": "43c4e71a39a15b239c4d3d850bf327c9bd4bda1aa4c99d58fca274ddb96f5758"
      },
      {
        "key": "kakaoDto",
        "path": "SearchExtension/src/main/java/com/search/extension/apiSearch/domain/model/KakaoBlogSearchResultDTO.java",
        "sha256": "b0b378d0c41032caec8fdf5dbe86c29ad9cfaa66d42b5c5bcfb9b1943bf89856"
      },
      {
        "key": "naverDto",
        "path": "SearchExtension/src/main/java/com/search/extension/apiSearch/domain/model/NaverBlogSearchResultDTO.java",
        "sha256": "d5ecd3e13cb47eab08b1caf994f289afb3ba8b9dd56d07099a0a55c88e41c256"
      },
      {
        "key": "parameterCases",
        "path": "SearchExtension/src/test/resources/contract/search-parameter-cases.json",
        "sha256": "59b153f186483f5e2affd93fdba91d889af1f4faa5b2971541dba6df12ffa2e2"
      },
      {
        "key": "kakaoFixture",
        "path": "SearchExtension/src/test/resources/provider/kakao-blog-response.json",
        "sha256": "365a7193bd215651cd41cbd2669999e0f13d5291049250ee4e38fb50f7a56578"
      },
      {
        "key": "naverFixture",
        "path": "SearchExtension/src/test/resources/provider/naver-blog-response.json",
        "sha256": "b056c61699864ef6811d6b1022c99bd331035c30f30f60249b169a435db903b8"
      }
    ]
  },
  "endpoints": [
    "/search",
    "/popularKeyword"
  ],
  "search": {
    "path": "/search",
    "requestParams": [
      {
        "name": "query",
        "defaultValue": null,
        "required": false,
        "javaType": "String"
      },
      {
        "name": "sort",
        "defaultValue": "accuracy",
        "required": false,
        "javaType": "String"
      },
      {
        "name": "page",
        "defaultValue": "1",
        "required": false,
        "javaType": "int"
      },
      {
        "name": "pageSize",
        "defaultValue": "10",
        "required": false,
        "javaType": "int"
      }
    ],
    "controllerOrder": [
      "query 공백 검사",
      "isValidParameter",
      "PageRequest.of"
    ],
    "controllerValidatesBeforePageRequest": true,
    "sortValues": [
      "accuracy",
      "recency"
    ],
    "pageSize": {
      "min": 1,
      "max": 50
    },
    "page": {
      "min": 1,
      "max": 50
    },
    "typeMismatchParams": [
      "page",
      "pageSize"
    ],
    "responseDataKeyOrder": [
      "searchResult",
      "totalItems",
      "totalPages",
      "currentPage"
    ]
  },
  "orchestration": {
    "circuitBreakers": [
      "kakaoApi",
      "naverApi"
    ],
    "keywordSources": [
      {
        "increment": 1,
        "apiSource": "Kakao"
      },
      {
        "increment": 1,
        "apiSource": "Naver"
      }
    ],
    "failureWithoutMessage": "PAGE_OUT_OF_BOUNDS",
    "failureWithMessage": "API_CALL_FAIL"
  },
  "providers": {
    "kakao": {
      "queryParams": [
        {
          "name": "query",
          "source": "query"
        },
        {
          "name": "sort",
          "source": "sort"
        },
        {
          "name": "page",
          "source": "currentPage"
        },
        {
          "name": "size",
          "source": "pageSize"
        }
      ],
      "responseKeys": [
        "searchResult",
        "currentPage",
        "totalItems",
        "totalPages"
      ],
      "totalItemsCap": 2500,
      "totalPagesCap": 50
    },
    "naver": {
      "queryParams": [
        {
          "name": "query",
          "source": "query"
        },
        {
          "name": "sort",
          "source": "sort"
        },
        {
          "name": "start",
          "source": "pageable.getPageNumber()"
        },
        {
          "name": "display",
          "source": "pageSize"
        }
      ],
      "responseKeys": [
        "searchResult",
        "currentPage",
        "totalItems",
        "totalPages"
      ],
      "totalItemsCap": 2500,
      "totalPagesCap": 50,
      "sortMap": {
        "accuracy": "sim",
        "recency": "date"
      }
    }
  },
  "popularKeyword": {
    "path": "/popularKeyword",
    "limit": 10,
    "batchSeconds": 5
  },
  "errors": {
    "INVALID_NULL_PARAMETER": {
      "status": 400,
      "message": "필수 파라미터가 없습니다. 확인해주세요."
    },
    "INVALID_PARAMETER_SORT": {
      "status": 401,
      "message": "순서 정렬 파라미터 값을 확인해주세요. (accuracy, recency)"
    },
    "INVALID_PARAMETER_PAGE": {
      "status": 402,
      "message": "페이징 또는 페이지 범위 파라미터 값을 확인해주세요. (1-50)"
    },
    "PAGE_OUT_OF_BOUNDS": {
      "status": 402,
      "message": "전체 검색 대상 페이지 값을 초과했습니다."
    },
    "INTERNAL_SERVER_ERROR": {
      "status": 500,
      "message": "서버 에러입니다. 서버 팀 000에게 연락주세요!"
    },
    "API_CALL_FAIL": {
      "status": 501,
      "message": "서버 에러입니다. API 요청에 실패했습니다"
    }
  },
  "constants": {
    "KAKAO_NAME": "Kakao",
    "NAVER_NAME": "Naver",
    "SUCCESS": "Success",
    "FAILURE": "Failure"
  },
  "dto": {
    "ResponseDTO": [
      {
        "name": "message",
        "javaType": "String"
      },
      {
        "name": "status",
        "javaType": "int"
      },
      {
        "name": "data",
        "javaType": "T"
      }
    ],
    "ErrorResponseDTO": [
      {
        "name": "status",
        "javaType": "int"
      },
      {
        "name": "message",
        "javaType": "String"
      }
    ],
    "PopularKeywordDTO": [
      {
        "name": "keyword",
        "javaType": "String"
      },
      {
        "name": "count",
        "javaType": "int"
      }
    ],
    "KakaoBlogSearchResultDTO": {
      "KakaoBlogSearchResultDTO": [
        {
          "name": "meta",
          "javaType": "Meta"
        },
        {
          "name": "documents",
          "javaType": "List<Document>"
        }
      ],
      "Meta": [
        {
          "name": "total_count",
          "javaType": "int"
        },
        {
          "name": "pageable_count",
          "javaType": "int"
        },
        {
          "name": "is_end",
          "javaType": "String"
        }
      ],
      "Document": [
        {
          "name": "title",
          "javaType": "String"
        },
        {
          "name": "contents",
          "javaType": "String"
        },
        {
          "name": "url",
          "javaType": "String"
        },
        {
          "name": "blogname",
          "javaType": "String"
        },
        {
          "name": "thumbnail",
          "javaType": "String"
        },
        {
          "name": "datetime",
          "javaType": "String"
        }
      ]
    },
    "NaverBlogSearchResultDTO": {
      "NaverBlogSearchResultDTO": [
        {
          "name": "lastBuildDate",
          "javaType": "String"
        },
        {
          "name": "total",
          "javaType": "int"
        },
        {
          "name": "start",
          "javaType": "int"
        },
        {
          "name": "display",
          "javaType": "int"
        },
        {
          "name": "items",
          "javaType": "List<Item>"
        }
      ],
      "Item": [
        {
          "name": "title",
          "javaType": "String"
        },
        {
          "name": "link",
          "javaType": "String"
        },
        {
          "name": "description",
          "javaType": "String"
        },
        {
          "name": "bloggername",
          "javaType": "String"
        },
        {
          "name": "bloggerlink",
          "javaType": "String"
        },
        {
          "name": "postdate",
          "javaType": "String"
        }
      ]
    }
  },
  "fixtures": {
    "kakao": {
      "meta": {
        "total_count": 3200,
        "pageable_count": 800,
        "is_end": false
      },
      "documents": [
        {
          "title": "<b>spring</b> 페이징 정리 (모의 문서)",
          "contents": "모의 응답 본문입니다. 외부 문서가 아닙니다.",
          "url": "",
          "blogname": "모의 블로그",
          "thumbnail": "",
          "datetime": "2026-09-20T10:00:00.000+09:00"
        }
      ]
    },
    "naver": {
      "lastBuildDate": "Sat, 26 Sep 2026 09:00:00 +0900",
      "total": 42,
      "start": 3,
      "display": 10,
      "items": [
        {
          "title": "<b>spring</b> 대체 검색 결과 (모의 문서)",
          "link": "",
          "description": "모의 응답 요약입니다. 외부 문서가 아닙니다.",
          "bloggername": "모의 블로거",
          "bloggerlink": "",
          "postdate": "20260920"
        }
      ]
    }
  },
  "parameterCases": [
    {
      "name": "default-page-and-size",
      "params": {
        "query": "spring"
      },
      "status": 200,
      "sort": "accuracy",
      "page": 1,
      "pageSize": 10
    },
    {
      "name": "recency-sort",
      "params": {
        "query": "spring",
        "sort": "recency"
      },
      "status": 200,
      "sort": "recency",
      "page": 1,
      "pageSize": 10
    },
    {
      "name": "lower-boundary",
      "params": {
        "query": "spring",
        "page": "1",
        "pageSize": "1"
      },
      "status": 200,
      "sort": "accuracy",
      "page": 1,
      "pageSize": 1
    },
    {
      "name": "upper-boundary",
      "params": {
        "query": "spring",
        "page": "50",
        "pageSize": "50"
      },
      "status": 200,
      "sort": "accuracy",
      "page": 50,
      "pageSize": 50
    },
    {
      "name": "empty-page-uses-default",
      "params": {
        "query": "spring",
        "page": "",
        "pageSize": ""
      },
      "status": 200,
      "sort": "accuracy",
      "page": 1,
      "pageSize": 10
    },
    {
      "name": "missing-query",
      "params": {},
      "status": 400,
      "error": "INVALID_NULL_PARAMETER"
    },
    {
      "name": "blank-query",
      "params": {
        "query": "   "
      },
      "status": 400,
      "error": "INVALID_NULL_PARAMETER"
    },
    {
      "name": "invalid-sort",
      "params": {
        "query": "spring",
        "sort": "popular"
      },
      "status": 401,
      "error": "INVALID_PARAMETER_SORT"
    },
    {
      "name": "invalid-sort-before-page",
      "params": {
        "query": "spring",
        "sort": "popular",
        "page": "-1"
      },
      "status": 401,
      "error": "INVALID_PARAMETER_SORT",
      "previousStatus": 500
    },
    {
      "name": "page-zero",
      "params": {
        "query": "spring",
        "page": "0"
      },
      "status": 402,
      "error": "INVALID_PARAMETER_PAGE"
    },
    {
      "name": "page-above-max",
      "params": {
        "query": "spring",
        "page": "51"
      },
      "status": 402,
      "error": "INVALID_PARAMETER_PAGE"
    },
    {
      "name": "negative-page",
      "params": {
        "query": "spring",
        "page": "-1"
      },
      "status": 402,
      "error": "INVALID_PARAMETER_PAGE",
      "previousStatus": 500
    },
    {
      "name": "page-size-zero",
      "params": {
        "query": "spring",
        "pageSize": "0"
      },
      "status": 402,
      "error": "INVALID_PARAMETER_PAGE",
      "previousStatus": 500
    },
    {
      "name": "negative-page-size",
      "params": {
        "query": "spring",
        "pageSize": "-5"
      },
      "status": 402,
      "error": "INVALID_PARAMETER_PAGE",
      "previousStatus": 500
    },
    {
      "name": "page-size-above-max",
      "params": {
        "query": "spring",
        "pageSize": "51"
      },
      "status": 402,
      "error": "INVALID_PARAMETER_PAGE"
    },
    {
      "name": "non-numeric-page",
      "params": {
        "query": "spring",
        "page": "two"
      },
      "status": 402,
      "error": "INVALID_PARAMETER_PAGE",
      "previousStatus": 500
    },
    {
      "name": "non-numeric-page-size",
      "params": {
        "query": "spring",
        "pageSize": "1.5"
      },
      "status": 402,
      "error": "INVALID_PARAMETER_PAGE",
      "previousStatus": 500
    },
    {
      "name": "type-error-before-missing-query",
      "params": {
        "page": "two"
      },
      "status": 402,
      "error": "INVALID_PARAMETER_PAGE",
      "previousStatus": 500
    }
  ]
});

// 원본 Spring Boot 백엔드의 요청 처리 흐름을 브라우저 안에서 재현하는 모의 어댑터입니다.
// ApiSearchController → ApiBlogSearchServiceImpl(카카오 → 네이버 대체) → 공급자 서비스 순서와
// 응답/오류 봉투, HTTP 상태 코드는 원본 소스에서 추출한 CONTRACT 를 그대로 따릅니다.
// 외부 검색 API 는 호출하지 않으며 검색 문서는 결정적으로 생성한 모의 데이터입니다.
import { CONTRACT } from './generated/contract.js';

const INT_MIN = -2147483648;
const INT_MAX = 2147483647;

// 원본의 ApiRequestsFailedException. 메시지가 없다는 점(getMessage() == null)이 오케스트레이션 분기에 쓰입니다.
export class ApiRequestsFailed extends Error {
  constructor(errorName) {
    super();
    this.errorName = errorName;
    this.javaMessage = null;
  }
}

class ProviderOutage extends Error {
  constructor(provider) {
    super(`503 Service Unavailable (${provider} 모의 장애)`);
    this.javaMessage = this.message;
  }
}

function withFieldOrder(fields, values) {
  return Object.fromEntries(fields.map(({ name }) => [name, values[name]]));
}

export function successBody(data) {
  return withFieldOrder(CONTRACT.dto.ResponseDTO, { message: CONTRACT.constants.SUCCESS, status: 200, data });
}

export function errorBody(errorName) {
  const error = CONTRACT.errors[errorName];
  if (!error) throw new Error(`알 수 없는 ErrorResponse: ${errorName}`);
  return { httpStatus: error.status, body: withFieldOrder(CONTRACT.dto.ErrorResponseDTO, error) };
}

// Spring 의 @RequestParam int 변환: 빈 문자열은 기본값, 공백 제거 후 10진/16진 정수만 허용
function bindInt(raw, param) {
  if (raw === undefined || raw === null || raw === '') return Number(param.defaultValue);
  const text = String(raw).replace(/\s/g, '');
  let value = Number.NaN;
  const hex = text.match(/^([+-]?)(?:0x|0X|#)([0-9a-fA-F]+)$/);
  if (hex) value = (hex[1] === '-' ? -1 : 1) * parseInt(hex[2], 16);
  else if (/^[+-]?\d+$/.test(text)) value = Number(text);
  if (!Number.isSafeInteger(value) || value < INT_MIN || value > INT_MAX) {
    throw Object.assign(new Error('MethodArgumentTypeMismatchException'), { param: param.name });
  }
  return value;
}

function bindString(raw, param) {
  if ((raw === undefined || raw === null || raw === '') && param.defaultValue !== null) return param.defaultValue;
  return raw ?? null;
}

// io.micrometer.common.util.StringUtils.isBlank 와 같은 판정 (Java trim: U+0020 이하 문자 제거)
function isBlank(text) {
  return text === null || text === undefined || /^[\u0000- ]*$/.test(text);
}

// ExceptionHandlerUtil.isValidParameter
function assertValidParameter(sort, size, page) {
  const { sortValues, pageSize, page: pageRange } = CONTRACT.search;
  if (!sortValues.includes(sort)) throw new ApiRequestsFailed('INVALID_PARAMETER_SORT');
  if (size > pageSize.max || size < pageSize.min || page > pageRange.max || page < pageRange.min) {
    throw new ApiRequestsFailed('INVALID_PARAMETER_PAGE');
  }
}

// ---------- 결정적 모의 문서 ----------

const FIXTURE_TOTALS = new Map([
  ['spring boot', 3200],
  ['서킷브레이커', 42],
  ['h2 database', 7],
]);
const TOPICS = ['설정 정리', '장애 대응 기록', '페이징 설계', '테스트 작성기', '성능 점검', '입문 노트', '리팩터링 회고', '운영 체크리스트'];
const BLOGS = ['모의 블로그 가', '모의 블로그 나', '모의 블로그 다', '모의 블로그 라', '모의 블로그 마'];
const BASE_TIME = Date.UTC(2026, 8, 25, 1, 0, 0); // 2026-09-25T10:00+09:00 고정 기준 시각

function hash(text) {
  let h = 2166136261;
  for (const ch of text) h = Math.imul(h ^ ch.codePointAt(0), 16777619) >>> 0;
  return h;
}

export function corpusTotal(query, provider) {
  const key = query.trim().toLowerCase();
  const base = FIXTURE_TOTALS.get(key) ?? [0, 18, 260, 1280, 4800, 91000][hash(key) % 6];
  // 네이버는 같은 질의에서도 결과 수가 다르다는 점을 흉내 냅니다 (카카오 페이지 초과 → 네이버 대체 확인용).
  return provider === 'naver' ? Math.round(base * 1.5) + 3 : base;
}

function kstIso(time) {
  const d = new Date(time + 9 * 3600 * 1000);
  return `${d.toISOString().slice(0, 19)}.000+09:00`;
}

// index: 정렬 기준 0부터 시작하는 전역 순번
function mockDocument(query, index, sort) {
  const seed = hash(`${query}#${index}`);
  const hours = sort === 'recency' ? index * 7 : (seed % 4000) + index;
  const time = BASE_TIME - hours * 3600 * 1000;
  return {
    title: `<b>${query}</b> ${TOPICS[seed % TOPICS.length]} (모의 문서 ${index + 1})`,
    body: `${query} 검색어로 생성한 모의 본문입니다. 실제 블로그 글이 아니며 외부 링크가 없습니다. (정렬: ${sort}, 순번 ${index + 1})`,
    blog: BLOGS[seed % BLOGS.length],
    time,
  };
}

function calculatePagesCount(pageSize, totalCount) {
  return totalCount < pageSize ? 1 : Math.ceil(totalCount / pageSize);
}

function providerResult(provider, searchResult, currentPage, total, pageSize) {
  const cfg = CONTRACT.providers[provider];
  const pageCount = calculatePagesCount(pageSize, total);
  if (currentPage > pageCount) throw new ApiRequestsFailed('PAGE_OUT_OF_BOUNDS');
  const values = {
    searchResult,
    currentPage,
    totalItems: Math.min(total, cfg.totalItemsCap),
    totalPages: Math.min(pageCount, cfg.totalPagesCap),
  };
  return Object.fromEntries(CONTRACT.search.responseDataKeyOrder.map((key) => [key, values[key]]));
}

function providerQuery(provider, values) {
  return CONTRACT.providers[provider].queryParams.map(({ name }) => `${name}=${encodeURIComponent(values[name])}`).join('&');
}

// KakaoBlogSearchServiceImpl
function kakaoSearch(query, sort, pageable, state, trace) {
  const { pageNumber: currentPage, pageSize } = pageable;
  trace.push(`KakaoBlogSearchService 요청(모의): ${providerQuery('kakao', { query, sort, page: currentPage, size: pageSize })}`);
  if (state.kakao === 'down') throw new ProviderOutage('Kakao');
  const total = corpusTotal(query, 'kakao');
  const start = (currentPage - 1) * pageSize;
  const count = Math.max(0, Math.min(pageSize, total - start));
  const pageable_count = Math.min(total, CONTRACT.providers.kakao.totalItemsCap);
  const documents = Array.from({ length: count }, (_, i) => {
    const doc = mockDocument(query, start + i, sort);
    return { title: doc.title, contents: doc.body, url: '', blogname: doc.blog, thumbnail: '', datetime: kstIso(doc.time) };
  });
  const searchResult = {
    meta: { total_count: total, pageable_count, is_end: String(start + count >= pageable_count) },
    documents,
  };
  return providerResult('kakao', searchResult, currentPage, total, pageSize);
}

// NaverBlogSearchServiceImpl — corrected one-based document offset.
function naverSearch(query, sort, pageable, state, trace) {
  const { pageNumber: currentPage, pageSize } = pageable;
  const naverSort = CONTRACT.providers.naver.sortMap[sort] ?? sort;
  const start = (currentPage - 1) * pageSize + 1;
  if(start>1000)throw new ApiRequestsFailed('PAGE_OUT_OF_BOUNDS');
  trace.push(`NaverBlogSearchService 요청(모의): ${providerQuery('naver', { query, sort: naverSort, start, display: pageSize })}`);
  if (state.naver === 'down') throw new ProviderOutage('Naver');
  const total = corpusTotal(query, 'naver');
  const count = Math.max(0, Math.min(pageSize, total - (start - 1)));
  const items = Array.from({ length: count }, (_, i) => {
    const doc = mockDocument(query, start - 1 + i, sort);
    return {
      title: doc.title,
      link: '',
      description: doc.body,
      bloggername: doc.blog,
      bloggerlink: '',
      postdate: kstIso(doc.time).slice(0, 10).replaceAll('-', ''),
    };
  });
  const searchResult = { lastBuildDate: 'Sat, 26 Sep 2026 09:00:00 +0900', total, start, display: count, items };
  const result = providerResult('naver', searchResult, currentPage, total, pageSize);
  result.totalPages=Math.min(result.totalPages,Math.floor(999/pageSize)+1);
  return result;
}

const PROVIDERS = [
  { breaker: CONTRACT.orchestration.circuitBreakers[0], call: kakaoSearch, source: CONTRACT.orchestration.keywordSources[0] },
  { breaker: CONTRACT.orchestration.circuitBreakers[1], call: naverSearch, source: CONTRACT.orchestration.keywordSources[1] },
];

function toQueryString(params) {
  return Object.entries(params)
    .filter(([, value]) => value !== undefined && value !== null)
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
    .join('&');
}

/**
 * @param {{ keywordStore: { increment(keyword: string, count: number): void, top(limit: number): {keyword: string, count: number}[] } }} deps
 */
export function createSearchApi({ keywordStore }) {
  // ApiBlogSearchServiceImpl.getApiSearchResults
  function serviceSearch(query, sort, pageable, providerState, trace) {
    assertValidParameter(sort, pageable.pageSize, pageable.pageNumber);
    let lastError = null;
    for (const [index, provider] of PROVIDERS.entries()) {
      if (index > 0) trace.push(`recoverWith → CircuitBreaker "${provider.breaker}"`);
      else trace.push(`CircuitBreaker "${provider.breaker}"`);
      let result;
      try {
        result = provider.call(query, sort, pageable, providerState, trace);
      } catch (error) {
        lastError = error;
        const reason = error instanceof ApiRequestsFailed ? `ApiRequestsFailedException(${error.errorName}), message=null` : error.message;
        trace.push(`${provider.source.apiSource} 실패: ${reason}`);
        continue;
      }
      try { keywordStore.increment(query, provider.source.increment); }
      catch { trace.push('검색 기록 저장 실패 — 공급자를 다시 호출하지 않습니다.'); throw new ApiRequestsFailed('INTERNAL_SERVER_ERROR'); }
      trace.push(`addPopularKeyword(query, ${provider.source.increment}, "${provider.source.apiSource}")`);
      return { result, provider: provider.source.apiSource };
    }
    const errorName = lastError.javaMessage === null ? CONTRACT.orchestration.failureWithoutMessage : CONTRACT.orchestration.failureWithMessage;
    throw new ApiRequestsFailed(errorName);
  }

  /**
   * GET /search 요청을 모의 처리합니다.
   * @param {Record<string, string|undefined>} params 쿼리스트링 원문 값
   * @param {{ kakao?: 'up'|'down', naver?: 'up'|'down' }} providerState 공급자 장애 시뮬레이션
   */
  function search(params = {}, providerState = {}) {
    const state = { kakao: providerState.kakao ?? 'up', naver: providerState.naver ?? 'up' };
    const qs = toQueryString(params);
    const request = `GET ${CONTRACT.search.path}${qs ? `?${qs}` : ''}`;
    const trace = [request];
    try {
      // ApiSearchController.getApiSearchResults: 파라미터 바인딩(타입 변환)은 메서드 본문보다 먼저 일어납니다.
      const bound = {};
      for (const param of CONTRACT.search.requestParams) {
        bound[param.name] = param.javaType === 'int' ? bindInt(params[param.name], param) : bindString(params[param.name], param);
      }
      trace.push(`바인딩: query=${JSON.stringify(bound.query)}, sort=${bound.sort}, page=${bound.page}, pageSize=${bound.pageSize}`);
      if (isBlank(bound.query)) throw new ApiRequestsFailed('INVALID_NULL_PARAMETER');
      assertValidParameter(bound.sort, bound.pageSize, bound.page);
      trace.push('ExceptionHandlerUtil.isValidParameter 통과');
      const pageable = { pageNumber: bound.page, pageSize: bound.pageSize };
      trace.push(`PageRequest.of(${bound.page}, ${bound.pageSize}) — 외부 page 번호(1부터)를 그대로 전달`);
      const { result, provider } = serviceSearch(bound.query, bound.sort, pageable, state, trace);
      trace.push(`200 ${CONTRACT.constants.SUCCESS}`);
      return { request, httpStatus: 200, body: successBody(result), provider, trace };
    } catch (error) {
      let errorName;
      if (error instanceof ApiRequestsFailed) errorName = error.errorName;
      else if (error.param && CONTRACT.search.typeMismatchParams.includes(error.param)) {
        trace.push(`MethodArgumentTypeMismatchException(${error.param})`);
        errorName = 'INVALID_PARAMETER_PAGE';
      } else throw error;
      const { httpStatus, body } = errorBody(errorName);
      trace.push(`${httpStatus} ${errorName}`);
      return { request, httpStatus, body, provider: null, trace };
    }
  }

  // GET /popularKeyword
  function popularKeyword() {
    const request = `GET ${CONTRACT.popularKeyword.path}`;
    const data = keywordStore
      .top(CONTRACT.popularKeyword.limit)
      .map((entry) => withFieldOrder(CONTRACT.dto.PopularKeywordDTO, entry));
    return { request, httpStatus: 200, body: successBody(data), trace: [request, `상위 ${CONTRACT.popularKeyword.limit}개 조회`, '200 Success'] };
  }

  return { search, popularKeyword };
}

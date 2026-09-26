import assert from 'node:assert/strict';
import { test } from 'node:test';

import { CONTRACT } from '../src/generated/contract.js';
import { corpusTotal, createSearchApi } from '../src/mock-adapter.js';

function memoryKeywords() {
  const counts = new Map();
  return {
    counts,
    increment(keyword, count) {
      counts.set(keyword, (counts.get(keyword) ?? 0) + count);
    },
    top(limit) {
      return [...counts.entries()]
        .sort((a, b) => b[1] - a[1] || (a[0] < b[0] ? -1 : 1))
        .slice(0, limit)
        .map(([keyword, count]) => ({ keyword, count }));
    },
  };
}

const fieldNames = (fields) => fields.map((f) => f.name);

test('공유 계약 케이스(Java ApiSearchControllerContractTest 와 같은 파일)를 모두 만족한다', async (t) => {
  assert.ok(CONTRACT.parameterCases.length >= 15);
  for (const c of CONTRACT.parameterCases) {
    await t.test(c.name, () => {
      const api = createSearchApi({ keywordStore: memoryKeywords() });
      const response = api.search(c.params);
      assert.equal(response.httpStatus, c.status);
      assert.equal(response.body.status, c.status);
      if (c.status === 200) {
        assert.equal(response.body.message, CONTRACT.constants.SUCCESS);
        assert.equal(response.body.data.currentPage, c.page);
        assert.ok(response.trace.includes(`PageRequest.of(${c.page}, ${c.pageSize}) — 외부 page 번호(1부터)를 그대로 전달`));
        assert.ok(response.trace.some((line) => line.includes(`sort=${c.sort}`)));
      } else {
        assert.equal(response.body.message, CONTRACT.errors[c.error].message);
      }
    });
  }
});

test('성공/오류 응답 봉투의 필드 순서가 ResponseDTO / ErrorResponseDTO 와 같다', () => {
  const api = createSearchApi({ keywordStore: memoryKeywords() });
  const ok = api.search({ query: 'spring boot' });
  assert.deepEqual(Object.keys(ok.body), fieldNames(CONTRACT.dto.ResponseDTO));
  assert.deepEqual(Object.keys(ok.body.data), CONTRACT.search.responseDataKeyOrder);
  const error = api.search({ query: 'spring boot', sort: 'popular' });
  assert.deepEqual(Object.keys(error.body), fieldNames(CONTRACT.dto.ErrorResponseDTO));
});

test('카카오 응답 형식이 KakaoBlogSearchResultDTO 와 같고 상한(2500건/50페이지)을 적용한다', () => {
  const api = createSearchApi({ keywordStore: memoryKeywords() });
  const { body, provider } = api.search({ query: 'spring boot', page: '2', pageSize: '10' });
  const dto = CONTRACT.dto.KakaoBlogSearchResultDTO;
  assert.equal(provider, 'Kakao');
  assert.deepEqual(Object.keys(body.data.searchResult), fieldNames(dto.KakaoBlogSearchResultDTO));
  assert.deepEqual(Object.keys(body.data.searchResult.meta), fieldNames(dto.Meta));
  assert.equal(typeof body.data.searchResult.meta.is_end, 'string');
  assert.equal(body.data.searchResult.documents.length, 10);
  for (const doc of body.data.searchResult.documents) assert.deepEqual(Object.keys(doc), fieldNames(dto.Document));
  assert.equal(body.data.searchResult.meta.total_count, 3200);
  assert.equal(body.data.totalItems, 2500);
  assert.equal(body.data.totalPages, 50);
});

test('정렬: recency 는 최신순, 네이버는 accuracy→sim, recency→date 로 요청한다', () => {
  const api = createSearchApi({ keywordStore: memoryKeywords() });
  const recency = api.search({ query: 'spring boot', sort: 'recency' });
  const times = recency.body.data.searchResult.documents.map((d) => d.datetime);
  assert.deepEqual(times, [...times].sort().reverse());

  const naver = api.search({ query: 'spring boot', sort: 'recency' }, { kakao: 'down' });
  assert.ok(naver.trace.some((line) => line.includes('NaverBlogSearchService') && line.includes('sort=date')));
  const naverAccuracy = api.search({ query: 'spring boot' }, { kakao: 'down' });
  assert.ok(naverAccuracy.trace.some((line) => line.includes('NaverBlogSearchService') && line.includes('sort=sim')));
});

test('카카오 장애 → 네이버 대체: NaverBlogSearchResultDTO 형식, start 에 page 값을 그대로 전달', () => {
  const keywords = memoryKeywords();
  const api = createSearchApi({ keywordStore: keywords });
  const response = api.search({ query: 'spring boot', page: '3', pageSize: '10' }, { kakao: 'down' });
  const dto = CONTRACT.dto.NaverBlogSearchResultDTO;
  assert.equal(response.httpStatus, 200);
  assert.equal(response.provider, 'Naver');
  assert.deepEqual(Object.keys(response.body.data.searchResult), fieldNames(dto.NaverBlogSearchResultDTO));
  for (const item of response.body.data.searchResult.items) assert.deepEqual(Object.keys(item), fieldNames(dto.Item));
  assert.equal(response.body.data.searchResult.start, 3);
  assert.equal(response.body.data.currentPage, 3);
  assert.ok(response.trace.includes('recoverWith → CircuitBreaker "naverApi"'));
  assert.ok(response.trace.includes('addPopularKeyword(query, 1, "Naver")'));
  assert.equal(keywords.counts.get('spring boot'), 1);
});

test('카카오 페이지 초과(PAGE_OUT_OF_BOUNDS)도 네이버로 대체한다', () => {
  const api = createSearchApi({ keywordStore: memoryKeywords() });
  assert.equal(corpusTotal('서킷브레이커', 'kakao'), 42);
  const response = api.search({ query: '서킷브레이커', page: '6', pageSize: '10' });
  assert.equal(response.httpStatus, 200);
  assert.equal(response.provider, 'Naver');
  assert.ok(response.trace.some((line) => line.includes('PAGE_OUT_OF_BOUNDS')));
});

test('두 공급자 모두 실패하면 원본과 같은 오류로 끝난다', () => {
  const keywords = memoryKeywords();
  const api = createSearchApi({ keywordStore: keywords });

  const bothDown = api.search({ query: 'spring boot' }, { kakao: 'down', naver: 'down' });
  assert.equal(bothDown.httpStatus, 501);
  assert.deepEqual(bothDown.body, { status: 501, message: CONTRACT.errors.API_CALL_FAIL.message });

  const bothOutOfBounds = api.search({ query: 'h2 database', page: '3', pageSize: '10' });
  assert.equal(bothOutOfBounds.httpStatus, 402);
  assert.equal(bothOutOfBounds.body.message, CONTRACT.errors.PAGE_OUT_OF_BOUNDS.message);

  // 마지막 실패(네이버 장애)는 메시지가 있으므로 API_CALL_FAIL
  const outOfBoundsThenDown = api.search({ query: 'h2 database', page: '3', pageSize: '10' }, { naver: 'down' });
  assert.equal(outOfBoundsThenDown.httpStatus, 501);

  assert.equal(keywords.counts.size, 0, '실패한 검색은 인기 검색어에 기록하지 않는다');
});

test('검증 오류는 공급자를 호출하지 않고 인기 검색어에도 기록하지 않는다', () => {
  const keywords = memoryKeywords();
  const api = createSearchApi({ keywordStore: keywords });
  const response = api.search({ query: 'spring boot', pageSize: '-5' });
  assert.equal(response.httpStatus, 402);
  assert.ok(!response.trace.some((line) => line.includes('BlogSearchService')));
  assert.equal(keywords.counts.size, 0);
});

test('GET /popularKeyword 는 횟수 내림차순 최대 10개를 PopularKeywordDTO 형식으로 반환한다', () => {
  const keywords = memoryKeywords();
  const api = createSearchApi({ keywordStore: keywords });
  for (let i = 0; i < 12; i += 1) {
    for (let n = 0; n <= i; n += 1) api.search({ query: `키워드${i}` });
  }
  const { httpStatus, body } = api.popularKeyword();
  assert.equal(httpStatus, 200);
  assert.deepEqual(Object.keys(body), fieldNames(CONTRACT.dto.ResponseDTO));
  assert.equal(body.data.length, 10);
  assert.deepEqual(Object.keys(body.data[0]), fieldNames(CONTRACT.dto.PopularKeywordDTO));
  assert.deepEqual(body.data[0], { keyword: '키워드11', count: 12 });
  assert.deepEqual(body.data.map((d) => d.count), [12, 11, 10, 9, 8, 7, 6, 5, 4, 3]);
});

test('모의 문서에는 외부 URL 이 없고 같은 요청은 같은 응답을 만든다', () => {
  const api = createSearchApi({ keywordStore: memoryKeywords() });
  const kakao = api.search({ query: 'spring boot', pageSize: '50' });
  const naver = api.search({ query: 'spring boot', pageSize: '50' }, { kakao: 'down' });
  for (const response of [kakao, naver]) assert.doesNotMatch(JSON.stringify(response.body), /https?:|www\./);
  for (const doc of kakao.body.data.searchResult.documents) assert.equal(doc.url + doc.thumbnail, '');
  for (const item of naver.body.data.searchResult.items) assert.equal(item.link + item.bloggerlink, '');
  assert.deepEqual(api.search({ query: 'spring boot', pageSize: '50' }).body, kakao.body);
});

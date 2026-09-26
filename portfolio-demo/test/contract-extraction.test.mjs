import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { test } from 'node:test';

import {
  OUTPUT,
  SOURCE_FILES,
  extractContract,
  javaHashMapOrder,
  parseDtoFields,
  readBackendFile,
  renderContractModule,
} from '../scripts/extract-contract.mjs';

const fresh = extractContract(readBackendFile);

test('생성된 contract.js 가 현재 원본 Java 소스와 일치한다', () => {
  assert.equal(readFileSync(OUTPUT, 'utf8'), renderContractModule(fresh));
});

test('컨트롤러 엔드포인트와 파라미터 기본값을 추출한다', () => {
  assert.deepEqual(fresh.endpoints, ['/search', '/popularKeyword']);
  assert.deepEqual(
    fresh.search.requestParams.map((p) => [p.name, p.javaType, p.defaultValue, p.required]),
    [
      ['query', 'String', null, false],
      ['sort', 'String', 'accuracy', false],
      ['page', 'int', '1', false],
      ['pageSize', 'int', '10', false],
    ],
  );
  assert.equal(fresh.search.controllerValidatesBeforePageRequest, true);
  assert.deepEqual(fresh.search.typeMismatchParams, ['page', 'pageSize']);
});

test('검증 규칙, 오류 코드, 상수를 추출한다', () => {
  assert.deepEqual(fresh.search.sortValues, ['accuracy', 'recency']);
  assert.deepEqual(fresh.search.page, { min: 1, max: 50 });
  assert.deepEqual(fresh.search.pageSize, { min: 1, max: 50 });
  assert.deepEqual(
    Object.fromEntries(Object.entries(fresh.errors).map(([k, v]) => [k, v.status])),
    { INVALID_NULL_PARAMETER: 400, INVALID_PARAMETER_SORT: 401, INVALID_PARAMETER_PAGE: 402, PAGE_OUT_OF_BOUNDS: 402, INTERNAL_SERVER_ERROR: 500, API_CALL_FAIL: 501 },
  );
  assert.equal(fresh.constants.SUCCESS, 'Success');
});

test('카카오 → 네이버 오케스트레이션과 공급자 매핑을 추출한다', () => {
  assert.deepEqual(fresh.orchestration.circuitBreakers, ['kakaoApi', 'naverApi']);
  assert.deepEqual(fresh.orchestration.keywordSources.map((s) => s.apiSource), ['Kakao', 'Naver']);
  assert.equal(fresh.orchestration.failureWithoutMessage, 'PAGE_OUT_OF_BOUNDS');
  assert.equal(fresh.orchestration.failureWithMessage, 'API_CALL_FAIL');
  assert.deepEqual(fresh.providers.kakao.queryParams.map((p) => p.name), ['query', 'sort', 'page', 'size']);
  assert.deepEqual(fresh.providers.naver.queryParams.map((p) => p.name), ['query', 'sort', 'start', 'display']);
  assert.deepEqual(fresh.providers.naver.sortMap, { accuracy: 'sim', recency: 'date' });
  for (const provider of ['kakao', 'naver']) {
    assert.equal(fresh.providers[provider].totalItemsCap, 2500);
    assert.equal(fresh.providers[provider].totalPagesCap, 50);
  }
  assert.deepEqual(fresh.popularKeyword, { path: '/popularKeyword', limit: 10, batchSeconds: 5 });
});

test('DTO 필드를 선언 순서대로 추출한다 (중첩 클래스 포함)', () => {
  assert.deepEqual(fresh.dto.ResponseDTO.map((f) => f.name), ['message', 'status', 'data']);
  assert.deepEqual(fresh.dto.ErrorResponseDTO.map((f) => f.name), ['status', 'message']);
  assert.deepEqual(fresh.dto.KakaoBlogSearchResultDTO.Meta.map((f) => f.name), ['total_count', 'pageable_count', 'is_end']);
  assert.deepEqual(fresh.dto.NaverBlogSearchResultDTO.Item.map((f) => f.name), ['title', 'link', 'description', 'bloggername', 'bloggerlink', 'postdate']);
  const nested = parseDtoFields('public class A {\n private int x;\n public static class B {\n  private String y;\n }\n private long z;\n}');
  assert.deepEqual(nested, { A: [{ name: 'x', javaType: 'int' }, { name: 'z', javaType: 'long' }], B: [{ name: 'y', javaType: 'String' }] });
});

test('HashMap 순회 순서는 Java 테스트(BlogSearchProviderHttpTest)에서 확인한 순서와 같다', () => {
  assert.deepEqual(javaHashMapOrder(['searchResult', 'currentPage', 'totalItems', 'totalPages']), ['searchResult', 'totalItems', 'totalPages', 'currentPage']);
});

test('비밀 값이 있는 파일과 IDE 메타데이터는 읽지 않는다', () => {
  for (const path of Object.values(SOURCE_FILES)) {
    assert.match(path, /^src\/(main\/java|test\/resources)\//);
    assert.doesNotMatch(path, /application\.properties|\.metadata|\.jar$/);
  }
  assert.ok(fresh.source.files.every((f) => /^[0-9a-f]{64}$/.test(f.sha256)));
});

test('원본에서 패턴을 찾지 못하면 명시적으로 실패한다', () => {
  const withoutControllerValidation = (path) =>
    path === SOURCE_FILES.controller
      ? readBackendFile(path).replace('ExceptionHandlerUtil.isValidParameter(sort, pageSize, page);', '')
      : readBackendFile(path);
  assert.throws(() => extractContract(withoutControllerValidation), /계약 추출 실패/);

  const withoutErrors = (path) => (path === SOURCE_FILES.errors ? 'public enum ErrorResponse {}' : readBackendFile(path));
  assert.throws(() => extractContract(withoutErrors), /계약 추출 실패: ErrorResponse enum/);
});

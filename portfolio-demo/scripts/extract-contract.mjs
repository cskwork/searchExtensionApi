// 원본 Java 소스(SearchExtension)에서 API 계약 메타데이터를 추출해 src/generated/contract.js 로 씁니다.
// - application.properties 는 API 키가 들어 있으므로 읽지 않습니다.
// - 패턴을 찾지 못하면 조용히 넘어가지 않고 실패합니다.
// 사용: node scripts/extract-contract.mjs [--check]
import { createHash } from 'node:crypto';
import { existsSync, readFileSync, writeFileSync, mkdirSync } from 'node:fs';
import { dirname, join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const here = dirname(fileURLToPath(import.meta.url));
export const DEMO_ROOT = resolve(here, '..');
export const BACKEND_ROOT = resolve(DEMO_ROOT, '../SearchExtension');
export const OUTPUT = join(DEMO_ROOT, 'src/generated/contract.js');

const PKG = 'src/main/java/com/search/extension';
export const SOURCE_FILES = {
  controller: `${PKG}/apiSearch/adapter/web/ApiSearchController.java`,
  validation: `${PKG}/apiSearch/application/utils/ExceptionHandlerUtil.java`,
  exceptionHandler: `${PKG}/apiSearch/application/exception/GlobalExceptionHandler.java`,
  orchestration: `${PKG}/apiSearch/application/service/ApiBlogSearchServiceImpl.java`,
  kakaoService: `${PKG}/apiSearch/application/service/KakaoBlogSearchServiceImpl.java`,
  naverService: `${PKG}/apiSearch/application/service/NaverBlogSearchServiceImpl.java`,
  popularRepository: `${PKG}/apiSearch/adapter/persistence/PopularKeywordQueryRepository.java`,
  scheduler: `${PKG}/scheduledTask/TaskSchedulerConfig.java`,
  errors: `${PKG}/apiSearch/domain/model/ErrorResponse.java`,
  constants: `${PKG}/apiSearch/domain/model/ApiConstants.java`,
  responseDto: `${PKG}/apiSearch/domain/model/ResponseDTO.java`,
  errorDto: `${PKG}/apiSearch/domain/model/ErrorResponseDTO.java`,
  popularDto: `${PKG}/apiSearch/domain/model/PopularKeywordDTO.java`,
  kakaoDto: `${PKG}/apiSearch/domain/model/KakaoBlogSearchResultDTO.java`,
  naverDto: `${PKG}/apiSearch/domain/model/NaverBlogSearchResultDTO.java`,
  parameterCases: 'src/test/resources/contract/search-parameter-cases.json',
  kakaoFixture: 'src/test/resources/provider/kakao-blog-response.json',
  naverFixture: 'src/test/resources/provider/naver-blog-response.json',
};

function must(match, what) {
  if (!match) throw new Error(`계약 추출 실패: ${what} 패턴을 원본 소스에서 찾지 못했습니다.`);
  return match;
}

function all(regex, text, what) {
  const found = [...text.matchAll(regex)];
  if (found.length === 0) must(null, what);
  return found;
}

function stripComments(java) {
  return java.replace(/\/\*[\s\S]*?\*\//g, '').replace(/\/\/[^\n]*/g, '');
}

// 클래스(중첩 static 클래스 포함)별 private 필드를 선언 순서대로 반환
export function parseDtoFields(java) {
  const classes = {};
  const stack = [];
  let depth = 0;
  for (const line of stripComments(java).split('\n')) {
    const cls = line.match(/\bclass\s+(\w+)/);
    if (cls) {
      stack.push({ name: cls[1], depth: depth + 1 });
      classes[cls[1]] = [];
    }
    const field = line.match(/^\s*private\s+(?:final\s+)?([\w<>, ]+?)\s+(\w+)\s*;/);
    if (field && stack.length && depth === stack.at(-1).depth) {
      classes[stack.at(-1).name].push({ name: field[2], javaType: field[1].replace(/\s+/g, '') });
    }
    for (const ch of line) {
      if (ch === '{') depth += 1;
      if (ch === '}') {
        depth -= 1;
        if (stack.length && depth < stack.at(-1).depth) stack.pop();
      }
    }
  }
  return classes;
}

function javaStringHash(text) {
  let h = 0;
  for (const ch of text) h = (Math.imul(31, h) + ch.charCodeAt(0)) | 0;
  return h;
}

// java.util.HashMap(기본 용량 16) 의 순회 순서. 원본은 검색 결과 data 를 HashMap 으로 반환합니다.
export function javaHashMapOrder(keys) {
  const buckets = new Map();
  for (const key of keys) {
    const h = javaStringHash(key);
    const index = (h ^ (h >>> 16)) & 15;
    if (!buckets.has(index)) buckets.set(index, []);
    buckets.get(index).push(key);
  }
  return [...buckets.keys()].sort((a, b) => a - b).flatMap((index) => buckets.get(index));
}

function providerMapping(java, label) {
  const params = all(/\.queryParam\("(\w+)",\s*([\w.()]+)\)/g, java, `${label} queryParam`).map((m) => ({
    name: m[1],
    source: m[2],
  }));
  const responseKeys = all(/response\.put\("(\w+)"/g, java, `${label} response.put`).map((m) => m[1]);
  const totalCap = Number(must(java.match(/if\s*\(\s*totalItemCount\s*>\s*(\d+)/), `${label} totalItems 상한`)[1]);
  const pageCap = Number(must(java.match(/if\s*\(\s*(?:pageCount|totalPages)\s*>\s*(\d+)/), `${label} totalPages 상한`)[1]);
  must(java.match(/currentPage\s*>\s*(?:pageCount|totalPages)[\s\S]*?ErrorResponse\.PAGE_OUT_OF_BOUNDS/), `${label} 페이지 초과 검사`);
  must(java.match(/totalCount\s*<\s*pageSize\s*\?\s*1\s*:\s*\(int\)\s*Math\.ceil/), `${label} calculatePagesCount`);
  return { queryParams: params, responseKeys, totalItemsCap: totalCap, totalPagesCap: pageCap };
}

export function extractContract(read) {
  const src = Object.fromEntries(Object.entries(SOURCE_FILES).map(([key, path]) => [key, read(path)]));
  const code = Object.fromEntries(
    Object.entries(src).map(([key, text]) => [key, key.endsWith('Cases') || key.endsWith('Fixture') ? text : stripComments(text)]),
  );

  const endpoints = all(/@GetMapping\("([^"]+)"\)/g, code.controller, 'GetMapping').map((m) => m[1]);
  const requestParams = all(
    /@RequestParam\s*\(\s*value\s*=\s*"(\w+)"\s*(?:,\s*defaultValue\s*=\s*"([^"]*)"\s*)?,\s*required\s*=\s*(true|false)\s*\)\s*(\w+)\s+(\w+)/g,
    code.controller,
    'RequestParam',
  ).map((m) => ({ name: m[1], defaultValue: m[2] ?? null, required: m[3] === 'true', javaType: m[4] }));

  const body = code.controller;
  const order = ['StringUtils.isBlank(query)', 'ExceptionHandlerUtil.isValidParameter(sort, pageSize, page)', 'PageRequest.of(page, pageSize)'].map(
    (needle) => must(body.indexOf(needle) >= 0 ? body.indexOf(needle) : null, `컨트롤러 ${needle}`),
  );

  const errors = Object.fromEntries(
    all(/(\w+)\((\d+),\s*"([^"]*)"\)/g, code.errors, 'ErrorResponse enum').map((m) => [m[1], { status: Number(m[2]), message: m[3] }]),
  );
  const constants = Object.fromEntries(
    all(/static\s+final\s+String\s+(\w+)\s*=\s*"([^"]*)"/g, code.constants, 'ApiConstants').map((m) => [m[1], m[2]]),
  );

  const sortValues = all(/sort\.equals\("(\w+)"\)/g, code.validation, 'sort 허용값').map((m) => m[1]);
  const sizeBounds = must(code.validation.match(/size\s*>\s*(\d+)\s*\|\|\s*size\s*<\s*(\d+)/), 'pageSize 범위');
  const pageBounds = must(code.validation.match(/page\s*>\s*(\d+)\s*\|\|\s*page\s*<\s*(\d+)/), 'page 범위');
  must(code.validation.match(/INVALID_PARAMETER_SORT[\s\S]*INVALID_PARAMETER_PAGE/), '정렬 → 페이지 검증 순서');

  const typeMismatchParams = all(/"(\w+)"\.equals\(ex\.getName\(\)\)/g, code.exceptionHandler, '타입 불일치 파라미터').map((m) => m[1]);
  must(code.exceptionHandler.match(/MethodArgumentTypeMismatchException[\s\S]*INVALID_PARAMETER_PAGE/), '타입 불일치 → INVALID_PARAMETER_PAGE');

  const circuitBreakers = all(/circuitBreaker\("(\w+)"\)/g, code.orchestration, 'CircuitBreaker 이름').map((m) => m[1]);
  const keywordSources = all(/addPopularKeyword\(query,\s*(\d+),\s*ApiConstants\.(\w+)\)/g, code.orchestration, 'addPopularKeyword').map((m) => ({
    increment: Number(m[1]),
    apiSource: constants[m[2]],
  }));
  must(code.orchestration.match(/\.recoverWith\(/), 'recoverWith 대체 호출');
  const nullMessage = must(code.orchestration.match(/getMessage\(\)\s*==\s*null\)\s*\{\s*throw new ApiRequestsFailedException\(ErrorResponse\.(\w+)\)/), 'onFailure 메시지 없음 분기');
  const withMessage = must(code.orchestration.match(/\}\s*throw new ApiRequestsFailedException\(ErrorResponse\.(\w+)\);\s*\}\)\.getOrElse/), 'onFailure 기본 분기');
  must(code.orchestration.match(/isValidParameter\(sort,\s*pageable\.getPageSize\(\),\s*pageable\.getPageNumber\(\)\)/), '서비스 재검증');

  const naverSortMap = Object.fromEntries(
    all(/sort\.equals\("(\w+)"\)\)\s*\{\s*sort\s*=\s*"(\w+)"/g, code.naverService, '네이버 정렬 매핑').map((m) => [m[1], m[2]]),
  );

  const dto = {
    ResponseDTO: parseDtoFields(src.responseDto).ResponseDTO,
    ErrorResponseDTO: parseDtoFields(src.errorDto).ErrorResponseDTO,
    PopularKeywordDTO: parseDtoFields(src.popularDto).PopularKeywordDTO,
    KakaoBlogSearchResultDTO: parseDtoFields(src.kakaoDto),
    NaverBlogSearchResultDTO: parseDtoFields(src.naverDto),
  };
  for (const [name, fields] of Object.entries(dto)) must(fields && Object.keys(fields).length ? fields : null, `${name} 필드`);

  const kakao = providerMapping(code.kakaoService, 'Kakao');
  const naver = { ...providerMapping(code.naverService, 'Naver'), sortMap: naverSortMap };

  return {
    source: {
      repository: 'cskwork/searchExtensionApi',
      backend: 'SearchExtension (Java 17, Spring Boot 3.1.1, Gradle)',
      hosting: 'Java 백엔드는 Vercel 에 배포되지 않습니다. 배포 대상은 이 API 탐색기와 브라우저 내 모의 어댑터뿐입니다.',
      files: Object.entries(SOURCE_FILES).map(([key, path]) => ({
        key,
        path: `SearchExtension/${path}`,
        sha256: createHash('sha256').update(src[key]).digest('hex'),
      })),
    },
    endpoints,
    search: {
      path: '/search',
      requestParams,
      controllerOrder: ['query 공백 검사', 'isValidParameter', 'PageRequest.of'],
      controllerValidatesBeforePageRequest: order[0] < order[1] && order[1] < order[2],
      sortValues,
      pageSize: { min: Number(sizeBounds[2]), max: Number(sizeBounds[1]) },
      page: { min: Number(pageBounds[2]), max: Number(pageBounds[1]) },
      typeMismatchParams,
      responseDataKeyOrder: javaHashMapOrder(kakao.responseKeys),
    },
    orchestration: {
      circuitBreakers,
      keywordSources,
      failureWithoutMessage: nullMessage[1],
      failureWithMessage: withMessage[1],
    },
    providers: { kakao, naver },
    popularKeyword: {
      path: '/popularKeyword',
      limit: Number(must(code.popularRepository.match(/\.limit\((\d+)\)/), '인기 검색어 limit')[1]),
      batchSeconds: Number(must(code.scheduler.match(/Duration\.ofSeconds\((\d+)\)/), '배치 주기')[1]),
    },
    errors,
    constants,
    dto,
    fixtures: {
      kakao: JSON.parse(src.kakaoFixture),
      naver: JSON.parse(src.naverFixture),
    },
    parameterCases: JSON.parse(src.parameterCases).cases,
  };
}

export function renderContractModule(contract) {
  return [
    '// 자동 생성 파일입니다. 직접 수정하지 마세요.',
    '// 생성: node scripts/extract-contract.mjs (원본: ../SearchExtension)',
    `export const CONTRACT = Object.freeze(${JSON.stringify(contract, null, 2)});`,
    '',
  ].join('\n');
}

export function readBackendFile(path) {
  const full = join(BACKEND_ROOT, path);
  if (!existsSync(full)) throw new Error(`원본 소스가 없습니다: SearchExtension/${path}`);
  return readFileSync(full, 'utf8');
}

if (process.argv[1] === fileURLToPath(import.meta.url)) {
  const rendered = renderContractModule(extractContract(readBackendFile));
  if (process.argv.includes('--check')) {
    const current = existsSync(OUTPUT) ? readFileSync(OUTPUT, 'utf8') : '';
    if (current !== rendered) {
      console.error('src/generated/contract.js 가 원본 소스와 다릅니다. npm run extract 를 실행하세요.');
      process.exit(1);
    }
    console.log('contract.js 가 원본 소스와 일치합니다.');
  } else {
    mkdirSync(dirname(OUTPUT), { recursive: true });
    writeFileSync(OUTPUT, rendered);
    console.log(`생성: ${OUTPUT}`);
  }
}

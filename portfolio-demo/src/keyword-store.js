// 인기 검색어 카운트를 브라우저 localStorage 에 보관합니다.
// 원본은 SEARCH_KEYWORD_HISTORY 에 검색 기록을 쌓고 5초 배치로 POPULAR_KEYWORD 를 갱신하지만,
// 체험판은 서버가 없으므로 같은 집계(키워드별 합계, 내림차순, 최대 N개)를 이 브라우저 안에서만 계산합니다.
export const STORAGE_KEY = 'searchExtensionApi.demo.keywordCounts.v1';
const MAX_KEYWORDS = 500;

function memoryStorage() {
  const data = new Map();
  return {
    getItem: (key) => (data.has(key) ? data.get(key) : null),
    setItem: (key, value) => data.set(key, String(value)),
    removeItem: (key) => data.delete(key),
  };
}

function parse(raw) {
  const parsed = JSON.parse(raw);
  if (!parsed || parsed.version !== 1 || !Array.isArray(parsed.entries)) throw new Error('형식 불일치');
  const counts = new Map();
  for (const entry of parsed.entries) {
    if (!Array.isArray(entry) || entry.length !== 2) throw new Error('항목 형식 불일치');
    const [keyword, count] = entry;
    if (typeof keyword !== 'string' || !Number.isSafeInteger(count) || count < 1) throw new Error('항목 값 불일치');
    counts.set(keyword, (counts.get(keyword) ?? 0) + count);
  }
  return counts;
}

/**
 * @param {Storage | undefined} storage 기본값은 window.localStorage. 사용할 수 없으면 메모리에만 저장합니다.
 */
export function createKeywordStore(storage) {
  let backend = storage;
  let persistent = true;
  try {
    backend ??= globalThis.localStorage;
    backend.getItem(STORAGE_KEY);
  } catch {
    backend = memoryStorage();
    persistent = false;
  }

  let recovered = false;
  let counts = new Map();
  const raw = backend.getItem(STORAGE_KEY);
  if (raw !== null) {
    try {
      counts = parse(raw);
    } catch {
      // 손상된 값은 버리고 빈 집계로 다시 시작합니다.
      recovered = true;
      backend.removeItem(STORAGE_KEY);
    }
  }

  function save() {
    const entries = [...counts.entries()];
    try {
      backend.setItem(STORAGE_KEY, JSON.stringify({ version: 1, entries }));
    } catch {
      persistent = false;
    }
  }

  return {
    get recovered() {
      return recovered;
    },
    get persistent() {
      return persistent;
    },
    increment(keyword, count = 1) {
      if (!counts.has(keyword) && counts.size >= MAX_KEYWORDS) {
        // 저장 공간 보호: 가장 적게 검색된 키워드를 밀어냅니다.
        const [least] = [...counts.entries()].sort((a, b) => a[1] - b[1] || (a[0] < b[0] ? 1 : -1))[0];
        counts.delete(least);
      }
      counts.set(keyword, (counts.get(keyword) ?? 0) + count);
      save();
    },
    top(limit) {
      return [...counts.entries()]
        .sort((a, b) => b[1] - a[1] || (a[0] < b[0] ? -1 : a[0] > b[0] ? 1 : 0))
        .slice(0, limit)
        .map(([keyword, count]) => ({ keyword, count }));
    },
    reset() {
      counts = new Map();
      recovered = false;
      backend.removeItem(STORAGE_KEY);
    },
  };
}

// 탐색기 설정/최근 요청은 API 집계와 별도로 보관한다.
export const EXPLORER_KEY = 'searchExtensionApi.demo.explorer.v1';
export const DEFAULT_DRAFT = { params: { query: 'spring boot', sort: 'accuracy', page: '1', pageSize: '10' }, providers: { kakao: 'up', naver: 'up' } };
const clone = (v) => JSON.parse(JSON.stringify(v));
const fields = Object.keys(DEFAULT_DRAFT.params);
function validDraft(v) {
  return v && v.params && v.providers && fields.every(k => typeof v.params[k] === 'string' && v.params[k].length <= 1000) && ['accuracy', 'recency'].includes(v.params.sort) && ['kakao', 'naver'].every(k => ['up', 'down'].includes(v.providers[k]));
}
function cleanDraft(v) {
  return { params: Object.fromEntries(fields.map(k => [k, v.params[k]])), providers: { kakao: v.providers.kakao, naver: v.providers.naver } };
}
function validEntry(e) {
  return e && validDraft(e.draft) && typeof e.at === 'string' && Number.isFinite(Date.parse(e.at)) && Number.isInteger(e.status) && e.status >= 100 && e.status <= 599 && typeof e.message === 'string' && e.message.length <= 1000 && typeof e.provider === 'string' && e.provider.length <= 100;
}
export function createExplorerState(storage, { scope = 'demo' } = {}) {
  const key = scope === 'demo' ? EXPLORER_KEY : EXPLORER_KEY + ':' + encodeURIComponent(scope);
  let backend, persistent = true, recovered = false;
  let value = { version: 1, draft: clone(DEFAULT_DRAFT), history: [] };
  try {
    backend = storage ?? globalThis.localStorage;
    const raw = backend.getItem(key);
    if (raw !== null) {
      try {
        if (raw.length > 40000) throw new Error('too large');
        const p = JSON.parse(raw);
        if (p?.version !== 1 || !validDraft(p.draft) || !Array.isArray(p.history) || p.history.length > 5 || !p.history.every(validEntry)) throw new Error('invalid');
        value = { version: 1, draft: cleanDraft(p.draft), history: p.history.map(e => ({ draft: cleanDraft(e.draft), at: e.at, status: e.status, message: e.message, provider: e.provider })) };
      } catch { recovered = true; }
    }
  } catch { persistent = false; }
  const save = () => { if (persistent) { try { backend.setItem(key, JSON.stringify(value)); } catch { persistent = false; } } };
  if (recovered) save();
  return {
    get persistent() { return persistent; }, get recovered() { return recovered; },
    snapshot() { return clone(value); },
    updateDraft(draft) { if (!validDraft(draft)) return; value.draft = cleanDraft(draft); save(); },
    record(draft, response, at = new Date().toISOString()) {
      if (!validDraft(draft)) return;
      const e = { draft: cleanDraft(draft), at, status: response.httpStatus, message: response.body.message, provider: response.provider || '응답 없음' };
      if (!validEntry(e)) return;
      value.draft = e.draft; value.history = [e, ...value.history].slice(0, 5); save();
    },
    clearHistory() { value.history = []; save(); },
  };
}

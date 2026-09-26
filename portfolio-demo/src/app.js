// API 탐색기 화면. 모든 응답은 mock-adapter.js 가 브라우저 안에서 만듭니다.
import { CONTRACT } from './generated/contract.js';
import { createSearchApi } from './mock-adapter.js';
import { createKeywordStore } from './keyword-store.js';

const $ = (id) => document.getElementById(id);
const SORT_LABELS = { accuracy: '정확도순', recency: '최신순' };

const store = createKeywordStore();
const api = createSearchApi({ keywordStore: store });
let lastSearch = null;

function el(tag, props = {}, children = []) {
  const node = document.createElement(tag);
  for (const [key, value] of Object.entries(props)) {
    if (key === 'text') node.textContent = value;
    else if (key === 'className') node.className = value;
    else node.setAttribute(key, value);
  }
  for (const child of [].concat(children)) node.append(child);
  return node;
}

const stripTags = (html) => html.replace(/<[^>]*>/g, '');

function showJson(body) {
  $('json-view').firstElementChild.textContent = JSON.stringify(body, null, 2);
}

function formParams() {
  const params = {};
  for (const name of ['query', 'sort', 'page', 'pageSize']) {
    const value = $(name).value;
    if (value !== '') params[name] = value;
  }
  return params;
}

function providerState() {
  const form = $('search-form');
  return { kakao: form.elements.kakao.value, naver: form.elements.naver.value };
}

// 공급자별 응답 형식(카카오 documents / 네이버 items)을 화면용 항목으로 맞춥니다.
function displayItems(data) {
  const result = data.searchResult;
  if (Array.isArray(result.documents)) {
    return result.documents.map((d) => ({ title: d.title, body: d.contents, source: d.blogname, date: d.datetime, fields: d }));
  }
  return result.items.map((d) => ({ title: d.title, body: d.description, source: d.bloggername, date: d.postdate, fields: d }));
}

function renderTrace(trace) {
  $('trace').replaceChildren(...trace.map((line) => el('li', { text: line })));
}

function renderResults(response) {
  const box = $('results');
  if (response.httpStatus !== 200) {
    box.hidden = true;
    return;
  }
  const data = response.body.data;
  const items = displayItems(data);
  $('result-summary').textContent = `· ${response.provider} 형식 · 전체 ${data.totalItems.toLocaleString('ko-KR')}건`;
  $('result-list').replaceChildren(
    ...(items.length
      ? items.map((item, index) => {
          const detailId = `detail-${index}`;
          const toggle = el('button', { type: 'button', className: 'link-button', 'aria-expanded': 'false', 'aria-controls': detailId, text: '자세히 보기' });
          const emptyLinks = Object.entries(item.fields)
            .filter(([, value]) => value === '')
            .map(([key]) => key)
            .join(', ');
          const detail = el('div', { id: detailId, className: 'detail', hidden: '' }, [
            el('p', { text: stripTags(item.body) }),
            el('p', { className: 'hint', text: `비어 있는 필드: ${emptyLinks || '없음'} — 모의 응답은 외부 링크를 만들지 않습니다.` }),
          ]);
          toggle.addEventListener('click', () => {
            const open = toggle.getAttribute('aria-expanded') === 'true';
            toggle.setAttribute('aria-expanded', String(!open));
            toggle.textContent = open ? '자세히 보기' : '접기';
            detail.hidden = open;
          });
          return el('li', { className: 'result' }, [
            el('h4', { text: stripTags(item.title) }),
            el('p', { className: 'meta', text: `${item.source} · ${item.date}` }),
            toggle,
            detail,
          ]);
        })
      : [el('li', { className: 'empty', text: '이 페이지에 문서가 없습니다.' })]),
  );
  $('page-status').textContent = `${data.currentPage} / ${data.totalPages} 페이지`;
  $('prev-page').disabled = data.currentPage <= CONTRACT.search.page.min;
  $('next-page').disabled = data.currentPage >= Math.min(data.totalPages, CONTRACT.search.page.max);
  box.hidden = false;
}

function renderStatus(response) {
  const status = $('status');
  const ok = response.httpStatus === 200;
  status.className = `status ${ok ? 'ok' : 'error'}`;
  status.textContent = ok
    ? `HTTP 200 · ${response.body.message} · 응답 공급자 ${response.provider}`
    : `HTTP ${response.httpStatus} · ${response.body.message}`;
}

function renderPopular() {
  const popular = api.popularKeyword();
  const data = popular.body.data;
  $('popular-list').replaceChildren(
    ...data.map((entry) => el('li', {}, [el('span', { className: 'keyword', text: entry.keyword }), el('span', { className: 'count', text: `${entry.count}회` })])),
  );
  $('popular-empty').hidden = data.length > 0;
  return popular;
}

function runSearch(params) {
  const response = api.search(params, providerState());
  lastSearch = params;
  renderStatus(response);
  $('request-line').textContent = response.request;
  renderTrace(response.trace);
  renderResults(response);
  showJson(response.body);
  renderPopular();
}

function goToPage(delta) {
  if (!lastSearch) return;
  const next = Number(lastSearch.page ?? CONTRACT.search.requestParams.find((p) => p.name === 'page').defaultValue) + delta;
  $('page').value = String(next);
  runSearch({ ...lastSearch, page: String(next) });
}

function runContractCases() {
  const isolated = createSearchApi({ keywordStore: { increment() {}, top: () => [] } });
  let passed = 0;
  const rows = CONTRACT.parameterCases.map((c) => {
    const response = isolated.search(c.params);
    const expected = c.status === 200 ? '200 Success' : `${c.status} ${c.error}`;
    const actualName = response.httpStatus === 200 ? 'Success' : Object.entries(CONTRACT.errors).find(([, e]) => e.message === response.body.message)?.[0];
    const actual = `${response.httpStatus} ${actualName}`;
    const ok = actual === expected;
    if (ok) passed += 1;
    return el('tr', { className: ok ? 'pass' : 'fail' }, [
      el('th', { scope: 'row', text: c.name }),
      el('td', {}, [el('code', { text: response.request.replace('GET ', '') })]),
      el('td', { text: expected }),
      el('td', { text: `${ok ? '일치' : '불일치'} · ${actual}` }),
    ]);
  });
  $('case-rows').replaceChildren(...rows);
  $('case-table').hidden = false;
  $('case-summary').textContent = `${CONTRACT.parameterCases.length}개 중 ${passed}개 일치`;
}

function renderContract() {
  const s = CONTRACT.search;
  $('sort').replaceChildren(...s.sortValues.map((v) => el('option', { value: v, text: `${v} (${SORT_LABELS[v] ?? v})` })));
  $('page-hint').textContent = `${s.page.min}–${s.page.max}`;
  $('size-hint').textContent = `${s.pageSize.min}–${s.pageSize.max}`;
  const range = { sort: s.sortValues.join(', '), page: `${s.page.min}–${s.page.max}`, pageSize: `${s.pageSize.min}–${s.pageSize.max}`, query: '공백 불가' };
  $('param-rows').replaceChildren(
    ...s.requestParams.map((p) =>
      el('tr', {}, [el('th', { scope: 'row' }, [el('code', { text: p.name })]), el('td', { text: p.javaType }), el('td', { text: p.defaultValue ?? '없음' }), el('td', { text: range[p.name] ?? '' })]),
    ),
  );
  $('error-rows').replaceChildren(
    ...Object.entries(CONTRACT.errors).map(([name, e]) =>
      el('tr', {}, [el('th', { scope: 'row' }, [el('code', { text: name })]), el('td', { text: String(e.status) }), el('td', { text: e.message })]),
    ),
  );
  $('repo-name').textContent = CONTRACT.source.repository;
  $('backend-name').textContent = CONTRACT.source.backend;
  $('hosting').textContent = CONTRACT.source.hosting;
  $('batch-seconds').textContent = String(CONTRACT.popularKeyword.batchSeconds);
  $('popular-note').textContent = `키워드별 검색 횟수 합계 상위 ${CONTRACT.popularKeyword.limit}개 · 이 브라우저에만 저장`;
  $('source-files').replaceChildren(
    ...CONTRACT.source.files.map((f) => el('li', {}, [el('code', { text: f.path }), el('span', { className: 'hash', text: f.sha256.slice(0, 12) })])),
  );
}

function renderStorageAlert() {
  const alert = $('storage-alert');
  if (store.recovered) {
    alert.textContent = '저장된 인기 검색어 데이터가 손상되어 비우고 다시 시작했습니다.';
    alert.hidden = false;
  } else if (!store.persistent) {
    alert.textContent = '이 브라우저에서는 저장소를 쓸 수 없어 새로고침하면 인기 검색어가 초기화됩니다.';
    alert.hidden = false;
  } else {
    alert.hidden = true;
  }
}

$('search-form').addEventListener('submit', (event) => {
  event.preventDefault();
  runSearch(formParams());
});
for (const chip of document.querySelectorAll('[data-example]')) {
  chip.addEventListener('click', () => {
    $('query').value = chip.dataset.example;
    $('page').value = '1';
    runSearch(formParams());
  });
}
$('prev-page').addEventListener('click', () => goToPage(-1));
$('next-page').addEventListener('click', () => goToPage(1));
$('popular-json').addEventListener('click', () => {
  const popular = renderPopular();
  renderStatus({ ...popular, provider: '브라우저 저장소' });
  $('request-line').textContent = popular.request;
  renderTrace(popular.trace);
  $('results').hidden = true;
  showJson(popular.body);
});
$('popular-reset').addEventListener('click', () => {
  store.reset();
  renderStorageAlert();
  renderPopular();
  $('popular-note').textContent = '인기 검색어 기록을 초기화했습니다.';
});
$('copy-json').addEventListener('click', async () => {
  const button = $('copy-json');
  try {
    await navigator.clipboard.writeText($('json-view').textContent);
    button.textContent = '복사됨';
  } catch {
    button.textContent = '복사 실패';
  }
  setTimeout(() => (button.textContent = '복사'), 1500);
});
$('run-cases').addEventListener('click', runContractCases);

renderContract();
renderStorageAlert();
renderPopular();

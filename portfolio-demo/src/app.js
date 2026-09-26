// API 탐색기 화면. 모든 응답은 mock-adapter.js 가 브라우저 안에서 만듭니다.
import { createHttpApi } from './http-adapter.js';
import { CONTRACT } from './generated/contract.js';
import { createSearchApi } from './mock-adapter.js';
import { createKeywordStore } from './keyword-store.js';
import { createExplorerState, DEFAULT_DRAFT } from './explorer-state.js';

const $ = (id) => document.getElementById(id);
const SORT_LABELS = { accuracy: '정확도순', recency: '최신순' };

const mode=document.body.dataset.mode==='live'?'live':'demo';
const store = mode==='demo'?createKeywordStore():{persistent:true,recovered:false};
const api = mode==='live'?createHttpApi({origin:location.origin}):createSearchApi({ keywordStore: store });
const explorer = createExplorerState(undefined,{scope:mode==='demo'?'demo':'live:'+location.origin});
let pending=null,requestSequence=0;
function busy(value){$('cancel-request').hidden=!value;$('search-form').setAttribute('aria-busy',String(value));}
function transportFailure(error){renderRoute({httpStatus:0,provider:null},null);$('status').className='status error';$('status').textContent='연결 오류 · '+error.message;$('results').hidden=true;$('result-empty').hidden=false;$('result-empty').textContent='서버 연결 오류이며 API의 401/402/501 응답이 아닙니다. 모의 데이터로 바꾸지 않았습니다.';$('recover-request').hidden=false;renderTrace([error.kind||'network',error.message]);showJson({transportError:error.kind||'network',message:error.message});lastSearch=null;focusResponse();}


// Request route: provider states come from the simulated settings (demo) or only from the response format (live).
// No latency, retry count or failover timing is shown because no response contains them.
function providerKey(value) { const v = String(value || '').toLowerCase(); return v.includes('kakao') ? 'kakao' : v.includes('naver') ? 'naver' : null; }
function renderRoute(response = null, providers = mode === 'live' ? null : providerState()) {
  const served = response ? providerKey(response.provider) : null;
  const step = (name, detail, tone) => el('li', { className: `route-step ${tone}` }, [el('span', { className: 'route-name', text: name }), el('span', { className: 'route-state', text: detail })]);
  const provider = (key, name) => {
    if (served === key) return step(name, mode === 'live' ? '응답 제공 · 문서 형식 기준' : '응답 제공', 'served');
    if (!providers) return step(name, response ? '응답에 기록 없음' : '서버가 결정', 'unknown');
    if (providers[key] === 'down') return step(name, '장애 설정', 'down');
    return step(name, response ? '호출하지 않음' : '정상 설정', response ? 'idle' : 'ready');
  };
  const outcome = !response ? step('응답', '요청 대기', 'ready') : response.httpStatus === 200 ? step('응답', `HTTP 200 · ${response.provider}`, 'served') : step('응답', response.httpStatus ? `HTTP ${response.httpStatus}` : '연결 오류', 'down');
  $('route-flow').replaceChildren(step('요청', 'GET /search', 'request'), provider('kakao', '카카오 · 1순위'), provider('naver', '네이버 · 대체'), outcome);
}
let lastSearch = null;
const TAB_NAMES = ['results', 'json', 'trace'];

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

const stripTags = (html) => String(html??'').replace(/<[^>]*>/g, '');

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
            el('p', { className: 'hint', text: mode==='demo'?`비어 있는 필드: ${emptyLinks || '없음'} — 모의 응답은 외부 링크를 만들지 않습니다.`:'서버가 반환한 검색 문서입니다. 외부 링크는 직접 선택해야 열립니다.' }),
          ]);
          if(mode==='live') {const href=item.fields.url||item.fields.link;try{const url=new URL(href);if(['http:','https:'].includes(url.protocol)&&!url.username&&!url.password)detail.append(el('a',{href:url.href,target:'_blank',rel:'noopener noreferrer',text:'원문 열기'}));}catch{}}
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
  if(mode==='live')return null;
  const popular = api.popularKeyword();
  const data = popular.body.data;
  $('popular-list').replaceChildren(
    ...data.map((entry) => el('li', {}, [el('span', { className: 'keyword', text: entry.keyword }), el('span', { className: 'count', text: `${entry.count}회` })])),
  );
  $('popular-empty').hidden = data.length > 0;
  return popular;
}

function readDraft() {
  return { params: Object.fromEntries(['query', 'sort', 'page', 'pageSize'].map(name => [name, $(name).value])), providers: providerState() };
}
function applyDraft(draft) {
  for (const [name, value] of Object.entries(draft.params)) $(name).value = value;
  for (const [name, value] of Object.entries(draft.providers)) $('search-form').elements[name].value = value;
  explorer.updateDraft(readDraft()); renderHistoryAlert();
}
function activateTab(name, focus = false) {
  for (const item of TAB_NAMES) {
    const active = item === name;
    $(`tab-${item}`).setAttribute('aria-selected', String(active));
    $(`tab-${item}`).tabIndex = active ? 0 : -1;
    $(`panel-${item}`).hidden = !active;
  }
  if (focus) $(`tab-${name}`).focus();
}
function focusResponse() {
  $('response-title').focus({ preventScroll: true });
  if (window.matchMedia?.('(max-width: 860px)').matches) $('response-title').scrollIntoView?.({ block: 'start' });
}
function clearResponse() {
  lastSearch = null;
  $('status').className = 'status';
  $('status').textContent = '설정을 불러왔습니다. 요청을 보내면 새 응답이 표시됩니다.';
  $('request-line').textContent = ''; $('response-context').textContent = '';
  $('results').hidden = true; $('result-empty').hidden = false;
  $('result-empty').textContent = '설정만 불러왔습니다. 아직 요청을 실행하지 않았습니다.';
  $('recover-request').hidden = true;
  renderTrace([]); showJson({}); activateTab('results'); renderRoute();
}
function renderHistoryAlert() {
  const messages = [];
  if (explorer.recovered) messages.push('저장된 탐색기 설정이 손상되어 기본값으로 복구했습니다. 인기 검색어는 그대로 유지됩니다.');
  if (!explorer.persistent) messages.push('저장소를 쓸 수 없어 설정과 최근 요청을 이 화면에서만 유지합니다.');
  $('history-alert').textContent = messages.join(' '); $('history-alert').hidden = !messages.length;
}
function renderHistory() {
  const entries = explorer.snapshot().history;
  $('history-list').replaceChildren(...entries.map(entry => {
    const load = el('button', { type: 'button', text: '설정 불러오기' });
    const replay = el('button', { type: 'button', text: '다시 실행' });
    load.addEventListener('click', () => { applyDraft(entry.draft); clearResponse(); $('query').focus(); });
    replay.addEventListener('click', () => { applyDraft(entry.draft); runSearch(formParams()); });
    return el('li', {}, [
      el('p', { className: 'history-request', text: `${entry.draft.params.query || '(검색어 없음)'} · page ${entry.draft.params.page || '기본값'} · size ${entry.draft.params.pageSize || '기본값'}` }),
      el('p', { className: 'hint', text: `HTTP ${entry.status} · ${entry.provider} · ${entry.message}` }),
      el('p', { className: 'hint', text: mode==='live'?`${new Date(entry.at).toLocaleTimeString('ko-KR')} · 실서버 ${location.origin}`:`${new Date(entry.at).toLocaleTimeString('ko-KR')} · 카카오 ${entry.draft.providers.kakao === 'up' ? '정상' : '장애'} / 네이버 ${entry.draft.providers.naver === 'up' ? '정상' : '장애'}` }),
      el('div', { className: 'actions' }, [load, replay]),
    ]);
  }));
  const counts = entries.reduce((m, e) => m.set(e.provider, (m.get(e.provider) || 0) + 1), new Map());
  const tone = (name) => providerKey(name) || 'none';
  // SVG from numeric widths and fixed class names only; parsed in HTML so no namespace URL is needed.
  let x = 0; const bar = el('div', { className: 'source-bar-wrap' });
  bar.innerHTML = '<svg class="source-bar" viewBox="0 0 100 8" preserveAspectRatio="none" aria-hidden="true">' + [...counts].map(([name, n]) => { const w = n / entries.length * 100, r = `<rect x="${x}" y="0" height="8" width="${w}" class="src-${tone(name)}"></rect>`; x += w; return r; }).join('') + '</svg>';
  $('history-sources').replaceChildren(...(entries.length ? [el('p', { className: 'hint', text: `최근 ${entries.length}건의 응답 공급자` }), bar, el('ul', { className: 'source-legend' }, [...counts].map(([name, n]) => el('li', { className: 'src-' + tone(name), text: `${name} ${n}건` })))] : []));
  $('history-sources').hidden = !entries.length;
  $('history-empty').hidden = !!entries.length; $('history-clear').disabled = !entries.length;
  renderHistoryAlert();
}
function runSearch(params, providers = providerState()) {
  if(mode==='live') {pending?.abort();pending=new AbortController();const sequence=++requestSequence;busy(true);$('status').textContent='실서버 응답을 기다리고 있습니다.';api.search(params,pending.signal).then(response=>{if(sequence!==requestSequence)return;displaySearch(params,{kakao:'up',naver:'up'},response);}).catch(error=>{if(sequence===requestSequence)transportFailure(error);}).finally(()=>{if(sequence===requestSequence)busy(false);});return;}
  const response = api.search(params, providers);displaySearch(params,providers,response);
}
function displaySearch(params,providers,response) {
  lastSearch = { params: { ...params }, providers: { ...providers }, response };
  renderStatus(response); $('request-line').textContent = response.request;
  $('response-context').textContent = mode==='live'?'실서버 · '+location.origin:`실행 설정 · 카카오 ${providers.kakao === 'up' ? '정상' : '장애'} / 네이버 ${providers.naver === 'up' ? '정상' : '장애'}`;
  renderTrace(response.trace); renderResults(response); showJson(response.body); renderRoute(response, mode === 'live' ? null : providers);
  $('result-empty').hidden = response.httpStatus === 200;
  $('result-empty').textContent = response.httpStatus === 200 ? '' : '요청이 실패했습니다. JSON 탭에서 원본 오류 응답을, 처리 흐름 탭에서 실패 단계를 확인하세요.';
  $('recover-request').hidden = response.httpStatus === 200;
  explorer.record({params:{query:'',sort:'accuracy',page:'',pageSize:'',...params},providers}, response);
  explorer.updateDraft(readDraft());
  renderHistory(); renderStorageAlert(); renderPopular(); $('draft-note').hidden = true;
  activateTab('results'); focusResponse();
}
function goToPage(delta) {
  if (!lastSearch || lastSearch.response.httpStatus !== 200) return;
  const next = lastSearch.response.body.data.currentPage + delta;
  if (next < CONTRACT.search.page.min || next > Math.min(lastSearch.response.body.data.totalPages, CONTRACT.search.page.max)) return;
  const params = { ...lastSearch.params, page: String(next) }, providers = { ...lastSearch.providers };
  // Paging belongs to the displayed response, not subsequently edited controls.
  applyDraft({ params: { query: '', sort: 'accuracy', page: '', pageSize: '', ...params }, providers });
  runSearch(params, providers);
}

function runContractCases() {
  if(mode!=='demo')return;
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
    if(mode!=='demo')return;
    $('query').value = chip.dataset.example;
    $('page').value = '1';
    runSearch(formParams());
  });
}
$('prev-page').addEventListener('click', () => goToPage(-1));
$('next-page').addEventListener('click', () => goToPage(1));
$('popular-json').addEventListener('click', () => {
  if(mode==='live'){pending?.abort();pending=new AbortController();const sequence=++requestSequence;busy(true);api.popularKeyword(pending.signal).then(popular=>{if(sequence!==requestSequence)return;showPopularResponse(popular);$('popular-list').replaceChildren(...popular.body.data.map(entry=>el('li',{},[el('span',{text:entry.keyword}),el('span',{text:entry.count+'회'})])));$('popular-empty').hidden=popular.body.data.length>0;}).catch(error=>{if(sequence===requestSequence)transportFailure(error);}).finally(()=>{if(sequence===requestSequence)busy(false);});return;}showPopularResponse(renderPopular());
});
function showPopularResponse(popular){
  lastSearch=null;$('results').hidden=true;
  if(popular.httpStatus!==200){renderStatus(popular);showJson(popular.body);renderTrace(popular.trace);activateTab('json');return;}
  renderStatus({ ...popular, provider: mode==='demo'?'브라우저 저장소':'서버 집계' });
  $('request-line').textContent = popular.request;
  renderTrace(popular.trace);
  lastSearch = null;
  $('results').hidden = true;
  $('result-empty').hidden = false;
  $('result-empty').textContent = '인기 검색어 응답은 JSON 탭에서 확인합니다.';
  $('recover-request').hidden = true;
  $('response-context').textContent = '조회만 수행했습니다. 검색 횟수는 바뀌지 않습니다.';
  showJson(popular.body); activateTab('json'); focusResponse();
}
$('popular-reset').addEventListener('click', () => {
  if(mode!=='demo')return;
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

for (const name of TAB_NAMES) {
  $(`tab-${name}`).addEventListener('click', () => activateTab(name));
  $(`tab-${name}`).addEventListener('keydown', event => {
    const i = TAB_NAMES.indexOf(name);
    const target = { ArrowRight: (i + 1) % 3, ArrowLeft: (i + 2) % 3, Home: 0, End: 2 }[event.key];
    if (target !== undefined) { event.preventDefault(); activateTab(TAB_NAMES[target], true); }
  });
}
for (const button of document.querySelectorAll('[data-scenario]')) {
  button.addEventListener('click', () => {
    if(mode!=='demo')return;
    const name = button.dataset.scenario;
    applyDraft({ params: { ...DEFAULT_DRAFT.params, page: name === 'invalid' ? '0' : '1' }, providers: { kakao: ['fallback', 'failure'].includes(name) ? 'down' : 'up', naver: name === 'failure' ? 'down' : 'up' } });
    runSearch(formParams());
  });
}
$('recover-request').addEventListener('click', () => {
  if(mode==='live'){runSearch(formParams());return;}
  const query = lastSearch?.params.query?.trim() || DEFAULT_DRAFT.params.query;
  applyDraft({ params: { ...DEFAULT_DRAFT.params, query }, providers: { kakao: 'up', naver: 'up' } });
  runSearch(formParams());
});
$('history-clear').addEventListener('click', () => { explorer.clearHistory(); renderHistory(); });
for (const name of ['input', 'change']) $('search-form').addEventListener(name, () => { explorer.updateDraft(readDraft()); renderHistoryAlert(); });
$('search-form').addEventListener('change', event => { if (!lastSearch && ['kakao', 'naver'].includes(event.target.name)) renderRoute(); });
renderContract();
const restored = explorer.snapshot();
applyDraft(restored.draft);
if (restored.history.length) { $('draft-note').textContent = '마지막 입력과 공급자 설정을 복원했습니다. 자동 실행하지 않았습니다.'; $('draft-note').hidden = false; }
renderHistory(); renderStorageAlert(); renderPopular(); renderRoute();

$('cancel-request').addEventListener('click',()=>pending?.abort());
if(mode==='live'){
 $('demo-notice').textContent='실서버 연결 · '+location.origin+' · GET /search · GET /popularKeyword';
 document.title='검색 API 진단 콘솔 · 실서버';
 document.querySelector('.lede').textContent='입력한 요청을 현재 서버로 전송하고 원본 응답을 확인합니다. 공급자 장애 시뮬레이션은 이 환경에서 실행하지 않습니다.';
 for(const node of document.querySelectorAll('.scenarios,.providers,.examples,#popular-reset,#run-cases'))node.hidden=true;
 $('search-form').querySelector('.providers').disabled=true;
 $('popular-note').textContent='서버 집계 결과 · 최대 '+CONTRACT.popularKeyword.batchSeconds+'초 이상 갱신 지연이 있을 수 있습니다. JSON 보기로 조회하세요.';
 $('recover-request').textContent='같은 환경에서 다시 요청';
 $('environment-mode').textContent='실서버 · '+location.origin;
 $('environment-link').href='index.html';$('environment-link').textContent='모의 환경으로 이동';
 $('result-empty').textContent='요청을 보내면 현재 서버의 응답을 표시합니다.';
}

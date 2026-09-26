import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { join } from 'node:path';
import { test } from 'node:test';

import { DIST, PUBLIC_FILES, build, listFiles } from '../scripts/build.mjs';

const NOTICE = 'searchExtensionApi 체험 · 원본 API 계약 기반 · 모의 응답 · 외부 검색 API 호출 없음';

test('dist 에는 허용 목록 파일만 들어간다', () => {
  build();
  const files = listFiles(DIST).sort();
  assert.deepEqual(files, [...PUBLIC_FILES].sort());
  for (const file of files) assert.doesNotMatch(file, /application\.properties|\.metadata|\.jar$|\.java$|\.mv\.db$/);
});

test('공개 파일에 외부 URL 이나 키 헤더가 없다', () => {
  build();
  for (const file of PUBLIC_FILES) {
    const text = readFileSync(join(DIST, file), 'utf8');
    assert.doesNotMatch(text, /https?:\/\//, `${file} 에 외부 URL 이 있습니다`);
    assert.doesNotMatch(text, /KakaoAK|X-Naver-Client|client-secret|api\.key/i, `${file} 에 인증 관련 문자열이 있습니다`);
  }
});

test('페이지에 고정 안내 문구, 출처 영역, 외부 연결 차단 정책이 있다', () => {
  build();
  const html = readFileSync(join(DIST, 'index.html'), 'utf8');
  assert.ok(html.includes(NOTICE));
  assert.match(html, /connect-src 'none'/);
  assert.match(html, /<html lang="ko">/);
  const referenced = [...html.matchAll(/(?:href|src)="([^"#]+)"/g)].map((m) => m[1]);
  for (const ref of referenced) assert.ok(PUBLIC_FILES.includes(ref), `허용 목록에 없는 참조: ${ref}`);
  const js = readFileSync(join(DIST, 'app.js'), 'utf8');
  const usedIds = new Set([...js.matchAll(/\$\('([\w-]+)'\)/g)].map((m) => m[1]));
  assert.ok(usedIds.size > 20);
  for (const id of usedIds) assert.match(html, new RegExp(`id="${id}"`), `index.html 에 #${id} 가 없습니다`);
  const css = readFileSync(join(DIST, 'styles.css'), 'utf8');
  assert.match(css, /\.notice\s*\{[^}]*position:\s*sticky/);
});

import assert from 'node:assert/strict';
import { test } from 'node:test';

import { STORAGE_KEY, createKeywordStore } from '../src/keyword-store.js';

function fakeStorage(initial = {}) {
  const data = new Map(Object.entries(initial));
  return {
    data,
    getItem: (key) => (data.has(key) ? data.get(key) : null),
    setItem: (key, value) => data.set(key, String(value)),
    removeItem: (key) => data.delete(key),
  };
}

test('검색 횟수를 저장하고 새 인스턴스에서도 읽는다', () => {
  const storage = fakeStorage();
  const store = createKeywordStore(storage);
  store.increment('spring', 1);
  store.increment('spring', 1);
  store.increment('java', 1);
  store.increment('__proto__', 1);
  const reloaded = createKeywordStore(storage);
  assert.equal(reloaded.recovered, false);
  assert.deepEqual(reloaded.top(10), [
    { keyword: 'spring', count: 2 },
    { keyword: '__proto__', count: 1 },
    { keyword: 'java', count: 1 },
  ]);
});

test('초기화하면 저장소에서 지운다', () => {
  const storage = fakeStorage();
  const store = createKeywordStore(storage);
  store.increment('spring', 1);
  store.reset();
  assert.deepEqual(store.top(10), []);
  assert.equal(storage.data.has(STORAGE_KEY), false);
});

for (const [name, raw] of [
  ['JSON 이 아님', '{not json'],
  ['버전 없음', '{"entries":[]}'],
  ['항목 형식 오류', '{"version":1,"entries":[["spring"]]}'],
  ['음수 횟수', '{"version":1,"entries":[["spring",-3]]}'],
  ['숫자가 아닌 횟수', '{"version":1,"entries":[["spring","7"]]}'],
  ['null', 'null'],
]) {
  test(`손상된 저장 값(${name})은 비우고 복구 상태를 알린다`, () => {
    const storage = fakeStorage({ [STORAGE_KEY]: raw });
    const store = createKeywordStore(storage);
    assert.equal(store.recovered, true);
    assert.deepEqual(store.top(10), []);
    assert.equal(storage.data.has(STORAGE_KEY), false);
    store.increment('spring', 1);
    assert.equal(createKeywordStore(storage).recovered, false);
  });
}

test('저장소를 쓸 수 없으면 메모리로 동작한다', () => {
  const blocked = {
    getItem() {
      throw new Error('SecurityError');
    },
  };
  const store = createKeywordStore(blocked);
  assert.equal(store.persistent, false);
  store.increment('spring', 1);
  assert.deepEqual(store.top(1), [{ keyword: 'spring', count: 1 }]);
});

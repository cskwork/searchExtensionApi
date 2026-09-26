import test from 'node:test';
import assert from 'node:assert/strict';
import { createExplorerState, DEFAULT_DRAFT, EXPLORER_KEY } from '../src/explorer-state.js';
const storage = () => { const m = new Map(); return { getItem:k=>m.get(k)??null, setItem:(k,v)=>m.set(k,String(v)) }; };
const response = {httpStatus:200,provider:'kakao',body:{message:'Success'}};
test('settings and newest five success/failure summaries persist without API work', () => {
  const backend=storage(), state=createExplorerState(backend);
  for(let i=0;i<7;i++) state.record({...DEFAULT_DRAFT,params:{...DEFAULT_DRAFT.params,query:`query ${i}`}},i===6?{httpStatus:501,body:{message:'failure'}}:response);
  const reloaded=createExplorerState(backend).snapshot();
  assert.equal(reloaded.history.length,5); assert.equal(reloaded.history[0].status,501); assert.equal(reloaded.history[0].draft.params.query,'query 6');
  const copy=state.snapshot(); copy.draft.params.query='mutated'; assert.equal(state.snapshot().draft.params.query,'query 6');
  state.clearHistory(); assert.deepEqual(state.snapshot().history,[]); assert.equal(state.snapshot().draft.params.query,'query 6');
});
test('malformed settings recover; denied reads/writes retain an explicit memory-only state', () => {
  for(const bad of ['{',JSON.stringify({version:9}),JSON.stringify({version:1,draft:DEFAULT_DRAFT,history:[{status:200}]})]) {
    const backend=storage();backend.setItem(EXPLORER_KEY,bad);const state=createExplorerState(backend);
    assert.equal(state.recovered,true);assert.deepEqual(state.snapshot().draft,DEFAULT_DRAFT);
  }
  for(const backend of [{getItem(){throw Error('denied')},setItem(){}},{getItem(){return null},setItem(){throw Error('quota')}}]) {
    const state=createExplorerState(backend);state.record(DEFAULT_DRAFT,response);
    assert.equal(state.persistent,false);assert.equal(state.snapshot().history.length,1);
  }
});

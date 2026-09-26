// Native endpoints only. No fixture import and no provider keys in browser code.
export class TransportError extends Error { constructor(kind,message){super(message);this.kind=kind;} }
export function createHttpApi({origin=globalThis.location?.origin,basePath='',request=globalThis.fetch,timeoutMs=8000}={}){
 const root=new URL(origin);if(!['http:','https:'].includes(root.protocol))throw new TransportError('configuration','HTTP 환경이 필요합니다.');
 if(!/^\/[\w/-]*$/.test(basePath||'/'))throw new TransportError('configuration','API 경로를 확인해주세요.');
 const base=basePath.replace(/\/$/,'');
 async function call(path,params={},signal){const url=new URL(base+path,root);for(const name of ['query','sort','page','pageSize'])if(params[name]!==undefined&&params[name]!==null)url.searchParams.set(name,String(params[name]));
  const controller=new AbortController();let timedOut=false;const abort=()=>controller.abort();if(signal?.aborted)abort();signal?.addEventListener('abort',abort,{once:true});const timer=setTimeout(()=>{timedOut=true;controller.abort();},timeoutMs);
  try{const result=await request(url,{method:'GET',credentials:'same-origin',redirect:'error',signal:controller.signal,headers:{Accept:'application/json'}});if(!String(result.headers.get('content-type')).includes('application/json'))throw new TransportError('malformed','서버가 JSON 응답을 반환하지 않았습니다.');let body;try{body=await result.json();}catch{throw new TransportError('malformed','서버 JSON 응답을 읽을 수 없습니다.');}if(!body||typeof body.message!=='string'||(result.status===200&&!Object.hasOwn(body,'data')))throw new TransportError('malformed','원본 응답 계약과 일치하지 않습니다.');
  if(result.status===200){if(path==='/popularKeyword'&&!Array.isArray(body.data))throw new TransportError('malformed','인기 검색어 응답 형식을 확인해주세요.');if(path==='/search'&&(!body.data||!body.data.searchResult||!Number.isFinite(body.data.currentPage)||!Number.isFinite(body.data.totalPages)||!Number.isFinite(body.data.totalItems)||!(Array.isArray(body.data.searchResult.documents)||Array.isArray(body.data.searchResult.items))))throw new TransportError('malformed','검색 응답 형식을 확인해주세요.');}
  const provider=path==='/search'&&result.status===200?(Array.isArray(body.data.searchResult.documents)?'Kakao':'Naver'):'서버';return {httpStatus:result.status,body,provider,request:'GET '+url.pathname+url.search,trace:['실서버 HTTP 응답을 그대로 표시합니다.','내부 공급자 시도 순서와 시간은 서버 응답에 포함되지 않아 추정하지 않습니다.']};
  }catch(e){if(e instanceof TransportError)throw e;if(controller.signal.aborted)throw new TransportError(timedOut?'timeout':'aborted',timedOut?'응답 제한 시간을 초과했습니다. 다시 시도해주세요.':'요청을 취소했습니다.');throw new TransportError('network','서버에 연결하지 못했습니다. 서버 주소와 연결 상태를 확인해주세요.');}finally{clearTimeout(timer);signal?.removeEventListener('abort',abort);}
 }
 return {search:(params,signal)=>call('/search',params,signal),popularKeyword:signal=>call('/popularKeyword',{},signal)};
}

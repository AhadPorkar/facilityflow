import {createPreviewClient} from './preview-data.js';

// GitHub Pages exposes the interactive synthetic showcase; server mode uses real PostgreSQL APIs.
export const isPreview = new URLSearchParams(location.search).get('preview') === '1'
  || location.hostname.endsWith('.github.io');
const preview=isPreview?createPreviewClient(localStorage):null;
let csrf=null;
function cookieValue(name) {
  const entry=document.cookie.split('; ').find(c=>c.startsWith(name+'='));
  return entry?decodeURIComponent(entry.substring(name.length+1)):null;
}
async function getCsrf() {
  if(isPreview)return;
  const r=await fetch('/api/auth/csrf',{credentials:'same-origin'});
  if(!r.ok)throw new Error('Could not initialize secure session');
  const data=await r.json();csrf=data.token;
}
export async function api(path, options={}) {
  if(isPreview)return preview.handle('/api'+path,options);
  const method=(options.method||'GET').toUpperCase();
  if(method!=='GET' && !csrf)await getCsrf();
  const headers={'Accept':'application/json',...options.headers};
  if(options.body!=null)headers['Content-Type']='application/json';
  if(method!=='GET')headers['X-XSRF-TOKEN']=cookieValue('XSRF-TOKEN')||csrf;
  const r=await fetch('/api'+path,{...options,method,headers,credentials:'same-origin'});
  if(!r.ok) {
    let message=`HTTP ${r.status}`;
    try {const detail=await r.json();message=detail.message||detail.detail||message;}
    catch { /* do not leak HTML exception pages */ }
    const ex=new Error(message);ex.status=r.status;throw ex;
  }
  if(r.status===204)return null;
  return r.json();
}
export async function initialize() {
  if(isPreview)return api('/auth/me');
  await getCsrf();
  try{return await api('/auth/me');}catch(ex){if(ex.status===401||ex.status===403)return null;throw ex;}
}
export async function login(email,password) {
  if(isPreview)return api('/auth/me');
  const user=await api('/auth/login',{method:'POST',body:JSON.stringify({email,password})});
  await getCsrf();return user;
}
export async function logout() {
  if(isPreview){location.href=location.pathname+'?preview=1';return;}
  await api('/auth/logout',{method:'POST'});csrf=null;await getCsrf();
}
export async function downloadPdf(path,lang='en') {
  if(isPreview){window.print();return 'preview';}
  const r=await fetch(`/api${path}${path.includes('?')?'&':'?'}lang=${encodeURIComponent(lang)}`,
    {credentials:'same-origin',headers:{'Accept':'application/pdf'}});
  if(!r.ok)throw new Error(`Report request failed (${r.status})`);
  const blob=await r.blob();const url=URL.createObjectURL(blob);
  const link=document.createElement('a');link.href=url;link.download=path.split('/').slice(1,-1).join('-')+'-'+Date.now()+'.pdf';
  document.body.append(link);link.click();link.remove();setTimeout(()=>URL.revokeObjectURL(url),1500);
  return true;
}
export function resetPreview(){preview?.reset();}

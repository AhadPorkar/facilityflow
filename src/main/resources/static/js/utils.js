export function escapeHtml(value) {
  return String(value ?? '').replace(/[&<>"']/g, char => ({
    '&':'&amp;', '<':'&lt;', '>':'&gt;', '"':'&quot;', "'":'&#39;'
  })[char]);
}
export const e = escapeHtml;
export function initials(name) {
  return String(name || 'FF').trim().split(/\s+/).slice(0,2).map(v=>v[0]?.toUpperCase()||'').join('');
}
export function niceDate(date, locale='en') {
  if (!date) return '—';
  const parsed = new Date(String(date).length === 10 ? `${date}T12:00:00Z` : date);
  if (Number.isNaN(parsed.getTime())) return '—';
  return new Intl.DateTimeFormat(locale==='de'?'de-DE':'en-GB', {year:'numeric',month:'short',day:'numeric',timeZone:'UTC'}).format(parsed);
}
export const isoDate = (days=0) => {
  const d=new Date();d.setUTCHours(12,0,0,0);d.setUTCDate(d.getUTCDate()+days);
  return d.toISOString().slice(0,10);
};
export function isOverdue(due,status, now=isoDate()) {
  return Boolean(due && due<now && !['APPROVED','CANCELLED'].includes(status));
}
export function shortId(value) { return String(value || '').slice(0,8).toUpperCase(); }
export const enumOptions=(values, labeler)=>values.map(value=>({value,label:labeler(value)}));

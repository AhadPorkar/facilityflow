import test from 'node:test';
import assert from 'node:assert/strict';
import {messages,t,locale,setLocale} from '../src/main/resources/static/js/i18n.js';
import {escapeHtml,initials,isoDate,isOverdue,niceDate} from '../src/main/resources/static/js/utils.js';

globalThis.localStorage = {
  getItem(k){return this[k]??null;},
  setItem(k,v){this[k]=String(v);}
};
globalThis.document={documentElement:{lang:'en'}};

test('English is the default even without preferences',()=>{
  assert.equal(locale(),'en');assert.equal(t('navDashboard'),'Overview');
});
test('German and English dictionaries have identical keys',()=>{
  const english=Object.keys(messages.en).sort();const german=Object.keys(messages.de).sort();
  assert.deepEqual(german,english);
});
test('Changing locale updates labels and HTML language',()=>{
  setLocale('de');assert.equal(t('navAssets'),'Anlagen');assert.equal(document.documentElement.lang,'de');
  setLocale('en');assert.equal(t('navAssets'),'Equipment');
});
test('Interpolation supports German and English templates',()=>{
  setLocale('de');assert.equal(t('createdCount',{count:5}),'Arbeitsaufträge erzeugt: 5');
  setLocale('en');assert.equal(t('createdCount',{count:2}),'Work orders generated: 2');
});
test('Escapes potentially malicious equipment, site and defect labels',()=>{
  assert.equal(escapeHtml('<img src=x onerror="alert(1)">'),
    '&lt;img src=x onerror=&quot;alert(1)&quot;&gt;');
  assert.equal(escapeHtml("O'Reilly & Sons"),'O&#39;Reilly &amp; Sons');
});
test('Initials, dates and overdue classification',()=>{
  assert.equal(initials('Olivia Weber'),'OW');
  assert.match(niceDate('2026-10-13','de'),/2026/);
  assert.equal(isOverdue('2020-01-01','OPEN','2026-10-13'),true);
  assert.equal(isOverdue('2020-01-01','APPROVED','2026-10-13'),false);
  assert.equal(isOverdue('2027-01-01','OPEN','2026-10-13'),false);
  assert.match(isoDate(),/^\d{4}-\d\d-\d\d$/);
});

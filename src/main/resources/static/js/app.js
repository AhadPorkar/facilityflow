import {t,locale,setLocale,statusLabel,priorityLabel,typeLabel,roleLabel} from './i18n.js';
import {icon} from './icons.js';
import {e,initials,niceDate,isOverdue,isoDate} from './utils.js';
import {api,initialize,login,logout,isPreview,downloadPdf,resetPreview} from './api.js';

const app=document.getElementById('app');
const toastLayer=document.getElementById('toast-layer');
const sections=[
  {label:'sectionOperations',items:[['/dashboard','navDashboard','grid'],['/orders','navOrders','orders'],
    ['/sites','navSites','buildings'],['/assets','navAssets','assets'],['/plans','navPlans','calendar'],
    ['/inspections','navInspections','shield'],['/templates','navTemplates','checklist'],['/defects','navDefects','alert']]},
  {label:'sectionIntelligence',items:[['/reports','navReports','document'],['/activity','navActivity','activity']]},
  {label:'sectionWorkspace',items:[['/team','navTeam','users'],['/settings','navSettings','settings']]}
];
const state={user:null,data:{},loading:true,route:'/dashboard',drawer:null,modal:null,mobile:false,search:''};
const workStatuses=['OPEN','ASSIGNED','IN_PROGRESS','COMPLETED','APPROVED','CANCELLED'];
const assetTypes=['HVAC','ELECTRICAL','FIRE_SAFETY','PLUMBING','ELEVATOR','OTHER'];
const priorities=['LOW','MEDIUM','HIGH','CRITICAL'];
const canManage=()=>['ADMIN','MANAGER'].includes(state.user?.role);
const canPerform=()=>canManage()||state.user?.role==='TECHNICIAN';
const canAdmin=()=>state.user?.role==='ADMIN';
const byId=(kind,id)=>(state.data[kind]||[]).find(x=>x.id===id);
const siteName=id=>byId('sites',id)?.name||'—';
const assetName=id=>byId('assets',id)?.name||'—';
const personName=id=>byId('team',id)?.displayName||t('unassigned');
const d=date=>niceDate(date,locale());
const route=()=>{const hash=location.hash.replace(/^#\/?/,'/').split('?')[0];
  return sections.flatMap(x=>x.items).some(x=>x[0]===hash)?hash:'/dashboard';};
const chip=(value,kind='status')=>`<span class="chip chip-${kind}-${String(value).toLowerCase()}"><span class="chip-dot"></span>${e(kind==='priority'?priorityLabel(value):kind==='type'?typeLabel(value):statusLabel(value))}</span>`;
const button=(action,text,ic='plus',variant='primary',attrs='')=>
  `<button type="button" class="btn btn-${variant}" data-action="${e(action)}" ${attrs}>${ic?icon(ic,17):''}<span>${e(text)}</span></button>`;
const initialsBadge=(name,size='')=>`<span class="avatar ${size}">${e(initials(name))}</span>`;
const fmt=(v)=>e(v||'—');
const stat=(number,label,ic,accent,sub='')=>`<div class="stat-card stat-${accent}">
  <div class="stat-top"><span class="stat-label">${e(label)}</span><span class="stat-icon">${icon(ic,20)}</span></div>
  <div class="stat-value">${number}</div><div class="stat-bottom">${e(sub)}</div></div>`;
function notify(message,kind='success') {
  const node=document.createElement('div');node.className=`toast toast-${kind}`;
  node.innerHTML=`${icon(kind==='success'?'check':'alert',18)}<span>${e(message)}</span>`;
  toastLayer.append(node);setTimeout(()=>node.remove(),4200);
}
async function load() {
  state.loading=true;
  const endpoints=[['sites','/sites'],['assets','/assets'],['orders','/work-orders'],
    ['plans','/maintenance-plans'],['inspections','/inspections'],['templates','/inspection-templates'],
    ['defects','/defects'],['team','/team'],['dashboard','/dashboard']];
  const responses=await Promise.all(endpoints.map(async ([key,path])=>[key,await api(path)]));
  state.data=Object.fromEntries(responses);
  if(canManage())state.data.activity=await api('/audit-events');
  state.loading=false;
}
function brandMark(){return `<span class="brand-logo"><span class="brand-angle"></span>${icon('bolt',16)}</span>`;}
function sidebar() {
  const nav=sections.map(group=>`<section class="sidebar-section"><div class="sidebar-label">${t(group.label)}</div>${group.items
    .filter(([path])=>path!=='/activity'||canManage())
    .map(([path,label,ic])=>`<button data-nav="${path}" class="side-link ${state.route===path?'active':''}" type="button">
      ${icon(ic,19)}<span>${t(label)}</span>${path==='/defects'&&state.data.dashboard?.openDefects?`<b class="nav-counter">${state.data.dashboard.openDefects}</b>`:''}</button>`).join('')}</section>`).join('');
  return `<aside class="sidebar ${state.mobile?'sidebar-open':''}" id="sidebar">
    <div class="brand-row">${brandMark()}<div><div class="brand-name">Facility<span>Flow</span></div><div class="brand-caption">OPERATIONS OS</div></div>
       <button class="mobile-close" data-action="close-menu" aria-label="Close menu">${icon('close',20)}</button></div>
    <div class="workspace-pill"><span class="workspace-monogram">N</span><span class="workspace-content"><strong>${e(state.user.organization)}</strong><small>${isPreview?t('previewLabel'):t('liveMode')}</small></span>${icon('arrow-down',15)}</div>
    <nav class="sidebar-nav" aria-label="Primary navigation">${nav}</nav>
    <div class="sidebar-bottom"><div class="sidebar-assist"><span class="assist-symbol">${icon('shield',19)}</span>
        <div><strong>${t('niceWork')}</strong><p>${t('subtitle')}</p></div></div>
      <button class="profile-button" data-nav="/settings">${initialsBadge(state.user.name)}<span><strong>${e(state.user.name)}</strong><small>${roleLabel(state.user.role)}</small></span>${icon('settings',17)}</button>
    </div>
  </aside>${state.mobile?'<div class="sidebar-scrim" data-action="close-menu"></div>':''}`;
}
function header(){
  const title=t(({
    '/dashboard':'navDashboard','/orders':'navOrders','/sites':'navSites','/assets':'navAssets','/plans':'navPlans',
    '/inspections':'navInspections','/templates':'navTemplates','/defects':'navDefects','/reports':'navReports',
    '/activity':'navActivity','/team':'navTeam','/settings':'navSettings'})[state.route]);
  return `<header class="topbar"><div class="topbar-left"><button class="mobile-menu icon-btn" data-action="menu" aria-label="Open menu">${icon('menu',21)}</button>
       <div class="crumb"><span>FacilityFlow</span>${icon('arrow',14)}<strong>${title}</strong></div></div>
    <div class="topbar-right"><div class="header-search">${icon('search',18)}<input type="search" id="global-search" placeholder="${t('searchShort')}" aria-label="${t('searchShort')}" value="${e(state.search)}" /></div>
      <div class="language-switch" role="group" aria-label="Language"><button data-lang="en" class="${locale()==='en'?'selected':''}">EN</button><button data-lang="de" class="${locale()==='de'?'selected':''}">DE</button></div>
      <button class="notification-button" title="${t('focusDefects')}" data-nav="/defects">${icon('alert',19)}${state.data.dashboard?.openDefects?'<i></i>':''}</button>
      <span class="avatar avatar-header">${e(initials(state.user.name))}</span></div></header>`;
}
function shell(){
  state.route=route();document.title=`${t(({
    '/dashboard':'navDashboard','/orders':'navOrders','/sites':'navSites','/assets':'navAssets','/plans':'navPlans',
    '/inspections':'navInspections','/templates':'navTemplates','/defects':'navDefects','/reports':'navReports',
    '/activity':'navActivity','/team':'navTeam','/settings':'navSettings'})[state.route])} · FacilityFlow`;
  return `<div class="app-shell">${sidebar()}<div class="main-panel">${header()}
    ${isPreview?`<div class="preview-bar">${icon('info',16)}<span>${t('previewMode')} — ${t('showcaseNote')}</span>
      <button data-action="reset-demo">${icon('refresh',13)} Reset</button></div>`:''}
    <main class="content">${page()}</main><footer class="footer"><span>${t('footer')}</span><span>${t('betaLabel')}</span></footer></div>
    ${state.drawer?drawer():''}${state.modal?state.modal:''}</div>`;
}
function pageHead(title,subtitle,actions=''){
  return `<div class="page-header"><div><span class="eyebrow">${t('platformTag')} / ${t('liveOverview')}</span>
    <h1>${t(title)}</h1><p>${t(subtitle)}</p></div><div class="page-actions">${actions}</div></div>`;
}
function empty() {return `<div class="empty-state">${icon('layers',30)}<h3>${t('emptyTitle')}</h3><p>${t('emptyBody')}</p></div>`;}
function table(headers,rows){
  if(!rows.length)return empty();
  return `<div class="table-wrap"><table class="data-table"><thead><tr>${headers.map(x=>`<th>${t(x)}</th>`).join('')}</tr></thead><tbody>${rows.join('')}</tbody></table></div>
    <div class="table-foot">${icon('info',14)}<span>${t('clickToManage')}</span><span>${rows.length} ${t('all').toLowerCase()}</span></div>`;
}
function row(kind,item,cols) {
  return `<tr tabindex="0" role="button" data-detail="${kind}" data-id="${e(item.id)}" data-searchtext="${e((cols.map(x=>String(x).replace(/<[^>]*>/g,' ')).join(' ')+' '+JSON.stringify(item)).toLowerCase())}">
    ${cols.map((c,i)=>`<td class="${i===0?'first-col':''}">${c}</td>`).join('')}</tr>`;
}
function pipeline(statuses){
  const active=['OPEN','ASSIGNED','IN_PROGRESS','COMPLETED','APPROVED'];
  const total=Math.max(1,Object.values(statuses||{}).reduce((a,b)=>a+Number(b),0));
  const tones=['#91a9ff','#7194ed','#3f6cdf','#84c6aa','#c8f58d'];
  let cumulative=0;
  const stops=active.map((s,i)=>{const start=cumulative;cumulative+=Number(statuses?.[s]||0)/total*100;
     return `${tones[i]} ${start}% ${cumulative}%`;});
  return `<div class="pipeline-shell"><div class="ring" style="background:conic-gradient(${stops.join(',')}${cumulative<100?',#26364b '+cumulative+'% 100%':''})">
     <div class="ring-middle"><strong>${Object.values(statuses||{}).reduce((a,b)=>a+b,0)}</strong><span>${t('allOrders')}</span></div></div>
    <div class="pipeline-legend">${active.map((s,i)=>`<div><span class="legend-label"><i style="background:${tones[i]}"></i>${statusLabel(s)}</span><strong>${statuses?.[s]||0}</strong></div>`).join('')}</div></div>`;
}
function dashboard(){
  const summary=state.data.dashboard||{};
  const open=['OPEN','ASSIGNED','IN_PROGRESS','COMPLETED'].reduce((sum,k)=>sum+(summary.workOrderStatuses?.[k]||0),0);
  const orders=summary.recentOrders||[];
  const activity=summary.activity||[];
  return `<section class="dashboard">
    ${pageHead('dashboardTitle','dashboardSubtitle',button('open-form',t('createOrder'),'plus','primary','data-kind="order"'))}
    <div class="hero"><div class="hero-copy"><span class="hero-kicker"><span class="kicker-dot"></span>${t('liveOverview')}</span>
      <h2>${t('commandCenter')}</h2><p>${t('commandText')}</p>
      <div class="hero-actions">${button('navigate',t('viewOrders'),'arrow','light','data-to="/orders"')}
        ${button('open-form',t('runInspection'),'shield','ghost-light','data-kind="inspection"')}</div></div>
      <div class="hero-graphic" aria-hidden="true"><span class="orbit orbit-1"></span><span class="orbit orbit-2"></span>
        <span class="hero-building building-one"><i></i><i></i><i></i><i></i><i></i><i></i></span>
        <span class="hero-building building-two"><i></i><i></i><i></i><i></i></span>
        <span class="hero-building building-three"><i></i><i></i><i></i></span><span class="signal signal-a"></span><span class="signal signal-b"></span></div>
    </div>
    <div class="stats-grid">${stat(summary.activeAssets||0,t('assetsInService'),'assets','violet',t('activeSites')+`: ${summary.sites||0}`)}
      ${stat(open,t('openWork'),'orders','blue',t('workPulseDesc'))}
      ${stat(summary.overdue||0,t('overdue'),'clock','orange',t('focusOverdue'))}
      ${stat(summary.openDefects||0,t('openDefects'),'alert','red',t('focusDefects'))}</div>
    <div class="dashboard-grid"><div class="panel panel-pipeline"><div class="panel-head"><div><h3>${t('workPulse')}</h3><p>${t('workPulseDesc')}</p></div>${icon('dots',19)}</div>
      ${pipeline(summary.workOrderStatuses)}</div>
      <div class="panel panel-focus"><div class="panel-head"><div><h3>${t('nextSteps')}</h3><p>${t('commandText')}</p></div>${icon('bolt',18)}</div>
      <button class="focus-row" data-nav="/orders"><span class="focus-icon focus-warn">${icon('clock',20)}</span><span><strong>${t('focusOverdue')}</strong><small>${summary.overdue||0} ${t('overdue').toLowerCase()}</small></span>${icon('arrow',18)}</button>
      <button class="focus-row" data-nav="/defects"><span class="focus-icon focus-red">${icon('alert',20)}</span><span><strong>${t('focusDefects')}</strong><small>${summary.openDefects||0} ${t('openDefects').toLowerCase()}</small></span>${icon('arrow',18)}</button>
      <button class="focus-row" data-nav="/plans"><span class="focus-icon focus-green">${icon('calendar',20)}</span><span><strong>${t('focusPlans')}</strong><small>${summary.activePlans||0} ${t('activePlans').toLowerCase()}</small></span>${icon('arrow',18)}</button></div></div>
    <div class="dashboard-grid lower-grid"><div class="panel recent-panel"><div class="panel-head"><h3>${t('recentOrders')}</h3><button class="text-link" data-nav="/orders">${t('viewAll')} ${icon('arrow',16)}</button></div>
      ${orders.length?orders.map(o=>`<button class="recent-row" data-detail="order" data-id="${o.id}"><span class="recent-icon">${icon('wrench',19)}</span>
         <span class="recent-main"><strong>${e(o.title)}</strong><small>${e(o.number)} · ${e(siteName(o.siteId))}</small></span>${chip(o.status)}${icon('arrow',16)}</button>`).join(''):empty()}</div>
      <div class="panel activity-panel"><div class="panel-head"><h3>${t('recentActivity')}</h3>${canManage()?`<button class="text-link" data-nav="/activity">${t('viewAll')} ${icon('arrow',16)}</button>`:''}</div>
       ${activity.map(ev=>`<div class="activity-row"><span class="activity-node"></span><div><strong>${e(ev.action.replaceAll('_',' '))}</strong>
       <p>${e(ev.description||ev.entityType)}</p><small>${d(ev.createdAt)}</small></div></div>`).join('')||empty()}</div></div>
  </section>`;
}
function orderTable(orders) {return table(['title','site','assignee','priority','status','deadline'],orders.map(w=>row('order',w,[
    `<span class="table-title">${e(w.title)}</span><span class="table-subtitle">${e(w.number)}</span>`,
    `<span class="cell-with-icon">${icon('buildings',16)}${e(siteName(w.siteId))}</span>`,
    `<span class="person-cell">${initialsBadge(personName(w.assignedToUserId),'mini')} ${e(personName(w.assignedToUserId))}</span>`,
    chip(w.priority,'priority'),chip(w.status),
    `<span class="date-cell ${isOverdue(w.dueDate,w.status)?'overdue':''}">${d(w.dueDate)}</span>`
  ])));}
function ordersPage(){
  const arr=state.data.orders||[];
  return `${pageHead('ordersTitle','ordersSubtitle',canManage()?button('open-form',t('createOrder'),'plus','primary','data-kind="order"'):'')}
    <div class="section-summary"><div class="tiny-stat"><strong>${arr.length}</strong><span>${t('allOrders')}</span></div>
     <div class="tiny-stat"><strong>${arr.filter(w=>w.status==='IN_PROGRESS').length}</strong><span>${statusLabel('IN_PROGRESS')}</span></div>
     <div class="tiny-stat"><strong>${arr.filter(w=>w.status==='COMPLETED').length}</strong><span>${t('readyForApproval')}</span></div></div>
    <div class="panel list-panel"><div class="list-toolbar"><div><h3>${t('allOrders')}</h3><p>${t('ordersSubtitle')}</p></div>
     <select aria-label="${t('filterAll')}" class="select-filter" id="order-filter"><option value="">${t('filterAll')}</option>${workStatuses.map(s=>`<option value="${s}">${statusLabel(s)}</option>`).join('')}</select></div>
     ${orderTable(arr)}</div>`;
}
function sitesPage() {
  const sites=state.data.sites||[];
  return `${pageHead('sitesTitle','sitesSubtitle',canManage()?button('open-form',t('addSite'),'plus','primary','data-kind="site"'):'')}
    <div class="site-grid">${sites.map(s=>{const as=state.data.assets.filter(a=>a.siteId===s.id);
      const os=state.data.orders.filter(o=>o.siteId===s.id&&!['APPROVED','CANCELLED'].includes(o.status));
      return `<article class="site-card" data-searchtext="${e((s.name+' '+s.city+' '+s.code).toLowerCase())}">
        <div class="site-card-top"><span class="building-symbol">${icon('buildings',23)}</span>
          ${canManage()?`<button data-action="open-form" data-kind="site" data-id="${s.id}" class="card-menu" aria-label="${t('edit')}">${icon('dots',19)}</button>`:''}</div>
        <span class="overline">${e(s.code)}</span><h3>${e(s.name)}</h3><p>${icon('location',16)}${e(s.address||'')} · ${e(s.city||'')}</p>
        <div class="site-card-stats"><div><strong>${as.length}</strong><small>${t('navAssets')}</small></div><div><strong>${os.length}</strong><small>${t('openWork')}</small></div>
           <button class="round-arrow" data-action="navigate" data-to="/assets" title="${t('navAssets')}">${icon('arrow',19)}</button></div>
      </article>`;}).join('')||empty()}</div>`;
}
function assetsPage(){return `${pageHead('assetsTitle','assetsSubtitle',canManage()?button('open-form',t('addAsset'),'plus','primary','data-kind="asset"'):'')}
  <div class="panel list-panel"><div class="list-toolbar"><div><h3>${t('navAssets')}</h3><p>${state.data.assets.length} ${t('navAssets').toLowerCase()}</p></div>
    <span class="quiet-label">${icon('shield',16)} ${t('safetyNote')}</span></div>
  ${table(['code','name','site','category','location','status'],(state.data.assets||[]).map(a=>row('asset',a,[
    `<span class="mono-id">${e(a.code)}</span>`, `<span class="table-title">${e(a.name)}</span>` , e(siteName(a.siteId)),
    `<span class="type-cell">${typeLabel(a.type)}</span>`, e(a.location),chip(a.status)
  ])))}</div>`;}
function plansPage(){return `${pageHead('plansTitle','plansSubtitle',canManage()?`${button('generate-due',t('generateDue'),'bolt','outline')} ${button('open-form',t('addPlan'),'plus','primary','data-kind="plan"')}`:'')}
    <div class="panel list-panel"><div class="list-toolbar"><div><h3>${t('navPlans')}</h3><p>${t('plansSubtitle')}</p></div><span class="quiet-label">${icon('clock',17)} UTC</span></div>
    ${table(['title','asset','interval','nextDue','assignee','status'],(state.data.plans||[]).map(p=>row('plan',p,[
      `<span class="table-title">${e(p.title)}</span>`,e(assetName(p.assetId)),`${p.intervalDays} d`,
      `<span class="date-cell ${p.nextDueDate<isoDate()?'overdue':''}">${d(p.nextDueDate)}</span>`,e(personName(p.assignedToUserId)),
      chip(p.active?'ACTIVE':'OUT_OF_SERVICE')
    ])))}</div>`;}
function inspectionsPage(){return `${pageHead('inspectionsTitle','inspectionsSubtitle',canPerform()?button('open-form',t('runInspection'),'plus','primary','data-kind="inspection"'):'')}
   <div class="panel list-panel"><div class="list-toolbar"><div><h3>${t('inspectionHistory')}</h3><p>${t('inspectionGenerated')}</p></div></div>
   ${table(['reportNo','asset','inspector','result','date','report'],(state.data.inspections||[]).map(i=>row('inspection',i,[
    `<span class="mono-id">${e(i.number)}</span>`,e(assetName(i.assetId)),e(personName(i.inspectorUserId)),chip(i.status),
    d(i.performedAt),`<button class="table-action" data-action="download-report" data-kind="inspection" data-id="${i.id}">${icon('download',16)}</button>`
   ])))}</div>`;}
function templatesPage(){return `${pageHead('templatesTitle','templatesSubtitle',canManage()?button('open-form',t('addTemplate'),'plus','primary','data-kind="template"'):'')}
   <div class="template-grid">${(state.data.templates||[]).map(x=>`<div class="template-card" data-searchtext="${e((x.name+' '+x.description).toLowerCase())}">
     <span class="template-icon">${icon('checklist',22)}</span><div class="template-copy"><h3>${e(x.name)}</h3>
     <p>${e(x.description||t('templatesSubtitle'))}</p></div><div class="template-bottom"><span>${x.assetType?typeLabel(x.assetType):t('all')} · ${t('navAssets')}</span>
       ${canPerform()?`<button data-action="open-form" data-kind="inspection" data-template="${x.id}" class="text-link">${t('runInspection')} ${icon('arrow',15)}</button>`:''}</div></div>`).join('')||empty()}</div>`;}
function defectsPage(){return `${pageHead('defectsTitle','defectsSubtitle')}
  <div class="panel list-panel"><div class="list-toolbar"><div><h3>${t('navDefects')}</h3><p>${t('inspectionGenerated')}</p></div>
    <span class="quiet-label">${state.data.defects.filter(x=>x.status==='OPEN').length} ${t('openDefects')}</span></div>
  ${table(['title','asset','priority','status','created','actions'],(state.data.defects||[]).map(dft=>row('defect',dft,[
    `<span class="table-title">${e(dft.title)}</span><span class="table-subtitle">${e(dft.details)}</span>`,e(assetName(dft.assetId)),
    chip(dft.severity,'priority'),chip(dft.status),d(dft.createdAt),
    dft.status==='OPEN'&&canManage()?`<button class="table-action" data-action="resolve" data-id="${dft.id}" title="${t('resolve')}">${icon('check',16)}</button>`:'—'
  ])))}</div>`;}
function reportsPage(){
  const list=(items,kind)=>items.length?items.map(item=>`<div class="report-row" data-searchtext="${e((item.title||item.number||'').toLowerCase())}">
    <span class="report-icon">${icon('document',20)}</span><div class="report-text"><strong>${e(kind==='inspection'?item.number:item.title)}</strong>
    <small>${e(kind==='inspection'?assetName(item.assetId):item.number)} · ${d(kind==='inspection'?item.performedAt:item.createdAt)}</small></div>
    <button class="btn btn-outline" data-action="download-report" data-kind="${kind}" data-id="${item.id}">${icon('download',16)} ${t('download')}</button></div>`).join(''):empty();
  return `${pageHead('reportsTitle','reportsSubtitle')}
    <div class="report-banner">${icon('info',20)}<div><strong>${t('reportsDesc')}</strong><p>${t('reportsNote')}</p></div></div>
    <div class="reports-layout"><div class="panel report-panel"><div class="panel-head"><h3>${t('reportInspections')}</h3></div>${list(state.data.inspections,'inspection')}</div>
    <div class="panel report-panel"><div class="panel-head"><h3>${t('reportOrders')}</h3></div>${list(state.data.orders.filter(w=>['APPROVED','COMPLETED'].includes(w.status)),'order')}</div></div>`;
}
function activityPage(){return `${pageHead('activityTitle','activitySubtitle')}
  <div class="panel list-panel"><div class="list-toolbar"><div><h3>${t('navActivity')}</h3><p>${t('tooltipHistory')}</p></div></div>
   ${table(['date','name','actions','description'],(state.data.activity||[]).map(ev=>row('event',ev,[
    d(ev.createdAt),`<span class="mono-id">${e(ev.entityType)}</span>`,`<b>${e(ev.action)}</b>`,e(ev.description)
   ])))}</div>`;}
function teamPage(){return `${pageHead('teamTitle','teamSubtitle',canAdmin()?button('open-form',t('addUser'),'plus','primary','data-kind="user"'):'')}
  <div class="panel list-panel"><div class="list-toolbar"><div><h3>${t('navTeam')}</h3><p>${state.data.team.length} ${t('memberCount').toLowerCase()}</p></div>${icon('lock',19)}</div>
  ${table(['fullName','email','role','status'],state.data.team.map(u=>row('user',u,[
   `<span class="person-cell">${initialsBadge(u.displayName)}<strong>${e(u.displayName)}</strong></span>`,e(u.email),
   `<span class="role-label">${roleLabel(u.role)}</span>`,chip(u.enabled?'ACTIVE':'OUT_OF_SERVICE')
  ])))}</div>`;}
function settingsPage(){return `${pageHead('settingsTitle','settingsSubtitle')}
  <div class="settings-layout"><div class="panel settings-card"><span class="settings-icon">${icon('globe',24)}</span>
    <h3>${t('appearance')}</h3><p>${t('language')}</p>
    <div class="setting-options"><button class="setting-option ${locale()==='en'?'on':''}" data-lang="en">English ${locale()==='en'?icon('check',17):''}</button>
    <button class="setting-option ${locale()==='de'?'on':''}" data-lang="de">Deutsch ${locale()==='de'?icon('check',17):''}</button></div></div>
    <div class="panel settings-card"><span class="settings-icon">${icon('shield',24)}</span><h3>${t('appInfo')}</h3>
      <p>${t('productionHint')}</p><div class="tech-stack">${['Java 21','Spring Boot','PostgreSQL','Flyway','Spring Security','Testcontainers'].map(x=>`<span>${x}</span>`).join('')}</div>
      <a class="external-link" target="_blank" rel="noopener noreferrer" href="https://github.com/ahadPorkar">${t('github')} ${icon('external',16)}</a>
      <a class="external-link" target="_blank" rel="noopener noreferrer" href="https://ahadporkar.com/">${t('portfolio')} ${icon('external',16)}</a></div>
    <div class="panel settings-card account-card"><span class="settings-icon">${icon('users',24)}</span><h3>${e(state.user.name)}</h3>
      <p>${e(state.user.email)}<br>${e(state.user.organization)}</p><span class="role-label">${roleLabel(state.user.role)}</span>
      ${button('logout',t('signOut'),'logout','outline')}</div>
  </div>`;}
function page(){switch(state.route){
  case '/orders':return ordersPage();case '/sites':return sitesPage();case '/assets':return assetsPage();
  case '/plans':return plansPage();case '/inspections':return inspectionsPage();case '/templates':return templatesPage();
  case '/defects':return defectsPage();case '/reports':return reportsPage();case '/activity':return activityPage();
  case '/team':return teamPage();case '/settings':return settingsPage();default:return dashboard();}}
function field(label,name,type='text',value='',opts={}) {
  const required=opts.required!==false?'required':'';
  if(type==='select')return `<label class="field ${opts.class||''}"><span>${t(label)}</span><select name="${name}" ${required} ${opts.disabled?'disabled':''}>
    <option value="">${t('choose')}</option>${(opts.options||[]).map(o=>`<option value="${e(o.value)}" ${String(value)===String(o.value)?'selected':''}>${e(o.label)}</option>`).join('')}</select></label>`;
  if(type==='textarea')return `<label class="field ${opts.class||''}"><span>${t(label)}</span><textarea name="${name}" rows="${opts.rows||4}" ${required} placeholder="${e(opts.placeholder||'')}">${e(value||'')}</textarea></label>`;
  if(type==='checkbox')return `<label class="check-field"><input type="checkbox" name="${name}" ${value?'checked':''}/><span>${t(label)}</span></label>`;
  return `<label class="field ${opts.class||''}"><span>${t(label)}</span><input name="${name}" type="${type}" value="${e(value||'')}" ${required} ${opts.min?`min="${opts.min}"`:''} ${opts.max?`max="${opts.max}"`:''}
    ${opts.placeholder?`placeholder="${e(opts.placeholder)}"`:''}/></label>`;
}
const choice=(rows,label)=>rows.map(x=>({value:x.id,label:label(x)}));
function modalFrame(title,desc,content,action='save',buttonLabel='create') {
  return `<div class="overlay" data-action="overlay"><div class="modal" role="dialog" aria-modal="true" aria-label="${e(t(title))}">
    <div class="modal-head"><div><span class="eyebrow">FACILITYFLOW / ${t('platformTag')}</span><h2>${t(title)}</h2><p>${t(desc)||''}</p></div>
      <button class="icon-btn" data-action="close-modal" aria-label="${t('close')}">${icon('close',21)}</button></div>
    <form id="modal-form" data-action="${action}"><div class="modal-body">${content}</div>
      <div class="modal-footer"><button type="button" class="btn btn-outline" data-action="close-modal">${t('cancel')}</button>
      <button type="submit" class="btn btn-primary">${icon('check',17)} ${t(buttonLabel)}</button></div></form></div></div>`;
}
function formFor(kind,id,templateId=null) {
  const data=({site:'sites',asset:'assets',plan:'plans',order:'orders',user:'team'})[kind];
  const item=data?byId(data,id)||{}:{};const editing=Boolean(id);
  const sites=choice(state.data.sites,x=>`${x.code} · ${x.name}`);
  const assets=choice(state.data.assets,x=>`${x.code} · ${x.name}`);
  const members=choice(state.data.team.filter(x=>['ADMIN','MANAGER','TECHNICIAN'].includes(x.role)),x=>x.displayName);
  let title='',desc='',body='';
  if(kind==='site') {
    title='formSite';desc='formSiteDesc';body=`<div class="form-grid">${field('code','code','text',item.code)}${field('name','name','text',item.name)}
      ${field('address','address','text',item.address,{required:false,class:'col-2'})}${field('city','city','text',item.city,{required:false})}
      ${field('postalCode','postalCode','text',item.postalCode,{required:false})}${field('contactName','contactName','text',item.contactName,{required:false})}
      ${field('contactEmail','contactEmail','email',item.contactEmail,{required:false})}</div>`;
  } else if(kind==='asset') {
    title='formAsset';desc='formAssetDesc';body=`<div class="form-grid">${field('site','siteId','select',item.siteId,{options:sites,disabled:editing})}
      ${field('code','code','text',item.code)}${field('name','name','text',item.name,{class:'col-2'})}
      ${field('category','type','select',item.type,{options:assetTypes.map(x=>({value:x,label:typeLabel(x)}))})}
      ${field('status','status','select',item.status||'ACTIVE',{options:['ACTIVE','OUT_OF_SERVICE','RETIRED'].map(x=>({value:x,label:statusLabel(x)}))})}
      ${field('location','location','text',item.location,{required:false})}${field('manufacturer','manufacturer','text',item.manufacturer,{required:false})}
      ${field('serialNumber','serialNumber','text',item.serialNumber,{required:false})}${field('installedOn','installedOn','date',item.installedOn,{required:false})}</div>`;
  } else if(kind==='plan') {
    title='formPlan';desc='formPlanDesc';body=`<div class="form-grid">${field('asset','assetId','select',item.assetId,{options:assets,disabled:editing})}
      ${field('title','title','text',item.title)}${field('interval','intervalDays','number',item.intervalDays||30,{min:1,max:3650})}
      ${field('nextDue','nextDueDate','date',item.nextDueDate||isoDate(7))}
      ${field('assignee','assignedToUserId','select',item.assignedToUserId,{options:members,required:false})}
      ${field('instructions','instructions','textarea',item.instructions,{required:false,class:'col-2'})}
      ${field('active','active','checkbox',item.active!==false)}</div>`;
  } else if(kind==='order') {
    title='formOrder';desc='ordersSubtitle';body=`<div class="form-grid">${field('site','siteId','select','',{options:sites})}
      ${field('asset','assetId','select','',{options:assets,required:false})}
      ${field('title','title','text','',{class:'col-2'})}
      ${field('priority','priority','select','MEDIUM',{options:priorities.map(x=>({value:x,label:priorityLabel(x)}))})}
      ${field('deadline','dueDate','date',isoDate(7),{required:false})}
      ${field('assignee','assignedToUserId','select','',{options:members,required:false})}
      ${field('description','description','textarea','',{class:'col-2',required:false})}</div>`;
  } else if(kind==='template') {
    title='formTemplate';desc='formTemplateDesc';body=`<div class="form-grid">${field('name','name','text','',{class:'col-2'})}
      ${field('description','description','text','',{required:false,class:'col-2'})}
      ${field('category','assetType','select','',{options:assetTypes.map(x=>({value:x,label:typeLabel(x)})),required:false})}
      ${field('items','items','textarea','No visible damage\nSafety devices intact\nFunctional test passed',{class:'col-2',rows:6,placeholder:t('checklistHelp')})}</div>`;
  } else if(kind==='inspection') {
    title='formInspection';desc='formInspectionDesc';body=`<div class="form-grid">${field('asset','assetId','select','',{options:assets})}
      ${field('template','templateId','select',templateId||'',{options:choice(state.data.templates,x=>x.name)})}
      <div class="col-2 inspection-checklist" id="inspection-items"><div class="checklist-placeholder">${icon('checklist',23)}<p>${t('templateNotSelected')}</p></div></div>
      ${field('inspectionNotes','notes','textarea','',{required:false,class:'col-2',rows:3})}</div>
      <p class="form-hint">${icon('info',16)} ${t('inspectionGenerated')}</p>`;
  } else if(kind==='user') {
    title='formUser';desc='formUserDesc';body=`<div class="form-grid">${field('fullName','displayName','text','',{class:'col-2'})}
      ${field('email','email','email','',{class:'col-2'})}
      ${field('password','password','password','',{class:'col-2',min:12})}
      ${field('role','role','select','TECHNICIAN',{options:['ADMIN','MANAGER','TECHNICIAN','VIEWER'].map(x=>({value:x,label:roleLabel(x)}))})}</div>`;
  } else if(kind==='assign') {
    title='formAssign';desc='ordersSubtitle';body=field('assignee','userId','select','',{options:members});
  } else if(kind==='complete') {
    title='formComplete';desc='needResolution';body=field('resolution','resolution','textarea','',{rows:5});
  }
  const form=modalFrame(title,desc,body,'save',editing?'save':'create');
  return form.replace('data-action="save"','data-action="save" data-kind="'+kind+'" data-id="'+e(id||'')+'"');
}
async function showForm(kind,id=null,templateId=null) {
  state.modal=formFor(kind,id,templateId);render();
  if(kind==='inspection')await fillInspectionTemplate();
  requestAnimationFrame(()=>document.querySelector('.modal [name]')?.focus());
}
async function fillInspectionTemplate() {
  const form=document.getElementById('modal-form');if(!form)return;
  const id=form.querySelector('[name="templateId"]')?.value;
  const area=document.getElementById('inspection-items');
  if(!area)return;
  if(!id){area.innerHTML=`<div class="checklist-placeholder">${t('templateNotSelected')}</div>`;return;}
  const detail=await api('/inspection-templates/'+id);
  const selected=form.querySelector('[name="assetId"]')?.value;
  const asset=byId('assets',selected);
  if(detail.template.assetType && asset && asset.type!==detail.template.assetType) {
    area.innerHTML=`<div class="inline-warning">${t('failed')}: ${t('category')} · ${typeLabel(detail.template.assetType)}</div>`;
    return;
  }
  area.innerHTML=`<div class="checklist-header">${icon('shield',20)} <div><strong>${t('inspectionStep')}</strong><p>${t('itemsRequired')}</p></div></div>
    ${detail.items.map((item,i)=>`<fieldset class="inspection-item" data-item="${item.id}"><legend><span class="question-index">${String(i+1).padStart(2,'0')}</span> ${e(item.label)}</legend>
     <div class="inspection-answers"><label class="choice-answer good"><input required type="radio" name="res-${item.id}" value="true"/>${icon('check',16)} ${t('inspectionPass')}</label>
     <label class="choice-answer bad"><input required type="radio" name="res-${item.id}" value="false"/>${icon('close',16)} ${t('inspectionFail')}</label></div>
     <input type="text" class="item-note" name="note-${item.id}" placeholder="${t('notes')}"/></fieldset>`).join('')}`;
}
function detailAction(action,id,text,ic,variant='primary') {
  return button(action,text,ic,variant,`data-id="${e(id)}"`);
}
function drawer(){
  const {kind,id}=state.drawer;let body='';let title=t('details');let eyebrow='FACILITYFLOW';
  const obj=byId(({order:'orders',asset:'assets',plan:'plans',inspection:'inspections',defect:'defects',user:'team',event:'activity'})[kind],id);
  if(!obj)return '';
  const kv=(key,value)=>`<div class="detail-pair"><span>${t(key)}</span><strong>${value}</strong></div>`;
  if(kind==='order') {
    title=obj.title;eyebrow=obj.number;
    const order=['OPEN','ASSIGNED','IN_PROGRESS','COMPLETED','APPROVED'];const index=order.indexOf(obj.status);
    const steps=order.map((s,i)=>`<div class="step ${i<=index?'passed':''}"><span>${i<index?icon('check',15):i+1}</span><small>${statusLabel(s)}</small></div>`).join('');
    body=`<div class="drawer-banner">${chip(obj.priority,'priority')} ${chip(obj.status)}</div>
      <h4>${t('lifecycle')}</h4><div class="workflow-steps">${steps}</div>
      <div class="detail-grid">${kv('site',e(siteName(obj.siteId)))}${kv('asset',e(assetName(obj.assetId)))}
        ${kv('assignee',e(personName(obj.assignedToUserId)))}${kv('deadline',d(obj.dueDate))}
        ${kv('created',d(obj.createdAt))}${kv('priority',chip(obj.priority,'priority'))}</div>
      <h4>${t('description')}</h4><p class="drawer-description">${e(obj.description)}</p>
      ${obj.resolution?`<h4>${t('resolution')}</h4><p class="drawer-description">${e(obj.resolution)}</p>`:''}
      <div class="drawer-actions">${canManage()&&['OPEN','ASSIGNED'].includes(obj.status)?detailAction('assign',id,t('changeAssignee'),'users','outline'):''}
       ${canPerform()&&obj.status==='ASSIGNED'?detailAction('start',id,t('start'),'bolt'):''}
       ${canPerform()&&obj.status==='IN_PROGRESS'?detailAction('complete',id,t('complete'),'check'):''}
       ${canManage()&&obj.status==='COMPLETED'?detailAction('approve',id,t('approve'),'shield'):''}
       ${canManage()&&['OPEN','ASSIGNED'].includes(obj.status)?detailAction('cancel-order',id,t('cancel'),'close','outline'):''}
       ${detailAction('download-report',id,t('download'),'download','outline').replace('data-id=', 'data-kind="order" data-id=')}</div>`;
  } else if(kind==='asset') {
    title=obj.name;eyebrow=obj.code;
    body=`<div class="drawer-banner">${chip(obj.status)} <span class="mono-id">${typeLabel(obj.type)}</span></div>
      <div class="detail-grid">${kv('site',e(siteName(obj.siteId)))}${kv('category',typeLabel(obj.type))}
       ${kv('location',e(obj.location))}${kv('manufacturer',e(obj.manufacturer))}
       ${kv('serialNumber',e(obj.serialNumber))}${kv('installedOn',d(obj.installedOn))}</div>
       <h4>${t('nextSteps')}</h4><div class="drawer-actions">${canManage()?detailAction('edit-asset',id,t('edit'),'settings','outline'):''}
       ${canPerform()?detailAction('inspect-asset',id,t('inspectAsset'),'shield'):''}</div>`;
  } else if(kind==='plan') {
    title=obj.title;eyebrow=t('navPlans');
    body=`<div class="detail-grid">${kv('asset',e(assetName(obj.assetId)))}${kv('interval',e(obj.intervalDays)+' d')}
      ${kv('nextDue',d(obj.nextDueDate))}${kv('assignee',e(personName(obj.assignedToUserId)))}
      ${kv('status',chip(obj.active?'ACTIVE':'OUT_OF_SERVICE'))}</div>
      <h4>${t('instructions')}</h4><p class="drawer-description">${e(obj.instructions)}</p>
      ${canManage()?`<div class="drawer-actions">${detailAction('edit-plan',id,t('edit'),'settings','outline')}</div>`:''}`;
  } else if(kind==='inspection') {
    title=obj.number;eyebrow=t('navInspections');
    body=`<div class="drawer-banner">${chip(obj.status)}</div>
      <div class="detail-grid">${kv('asset',e(assetName(obj.assetId)))}${kv('inspector',e(personName(obj.inspectorUserId)))}
        ${kv('date',d(obj.performedAt))}${kv('reportNo',e(obj.number))}</div>
      <h4>${t('inspectionNotes')}</h4><p class="drawer-description">${e(obj.notes)}</p>
      <div class="drawer-actions">${detailAction('download-report',id,t('download'),'download').replace('data-id=','data-kind="inspection" data-id=')}</div>`;
  } else if(kind==='defect') {
    title=obj.title;eyebrow=t('navDefects');
    body=`<div class="drawer-banner">${chip(obj.severity,'priority')} ${chip(obj.status)}</div>
      <div class="detail-grid">${kv('asset',e(assetName(obj.assetId)))}${kv('created',d(obj.createdAt))}</div>
      <h4>${t('details')}</h4><p class="drawer-description">${e(obj.details)}</p>
      ${obj.correctiveWorkOrderId?`<div class="related-entity">${icon('wrench',20)}<span>${t('defectLinked')}</span><button data-action="linked-order" data-id="${obj.correctiveWorkOrderId}">${t('openOrder')} ${icon('arrow',15)}</button></div>`:''}
      <div class="drawer-actions">${obj.status==='OPEN'&&canManage()?detailAction('resolve',id,t('resolve'),'check'):''}</div>`;
  } else if(kind==='user') {
    title=obj.displayName;eyebrow=t('teamTitle');
    body=`<div class="drawer-person">${initialsBadge(obj.displayName,'large')}<strong>${e(obj.email)}</strong></div>
      <div class="detail-grid">${kv('role',roleLabel(obj.role))}${kv('status',chip(obj.enabled?'ACTIVE':'OUT_OF_SERVICE'))}</div>`;
  } else if(kind==='event') {
    title=obj.action;eyebrow=t('navActivity');
    body=`<div class="detail-grid">${kv('date',d(obj.createdAt))}${kv('category',e(obj.entityType))}
      ${kv('description',e(obj.description))}</div>`;
  }
  return `<div class="drawer-scrim" data-action="close-drawer"></div><aside class="drawer" role="dialog" aria-modal="true" aria-label="${e(title)}">
    <div class="drawer-top"><span class="eyebrow">${e(eyebrow)}</span><button class="icon-btn" data-action="close-drawer" aria-label="${t('close')}">${icon('close',22)}</button></div>
    <h2>${e(title)}</h2>${body}</aside>`;
}
function loginView(){
  return `<div class="login-shell"><section class="login-story"><div class="login-brand">${brandMark()}<span>FacilityFlow</span></div>
    <div class="login-copy"><span class="eyebrow light">${t('platformTag')}</span>
      <h1>${t('marketingHeadline')}</h1><p>${t('marketingSub')}</p>
      <div class="login-mini-stats"><div>${icon('checklist',21)} <span>INSPECTIONS</span></div><div>${icon('wrench',21)} <span>MAINTENANCE</span></div><div>${icon('shield',21)} <span>COMPLIANCE</span></div></div></div>
    <div class="login-signature">${t('footer')}</div><div class="login-shapes"><i></i><i></i><i></i></div>
    </section><section class="login-content"><div class="login-language"><button data-lang="en" class="${locale()==='en'?'selected':''}">EN</button><button data-lang="de" class="${locale()==='de'?'selected':''}">DE</button></div>
      <div class="login-card"><div class="login-symbol">${icon('lock',23)}</div><h2>${t('welcomeBack')}</h2><p>${t('signInMessage')}</p>
        <form id="login-form"><label class="field"><span>${t('email')}</span><input name="email" type="email" required autocomplete="username" placeholder="you@company.com" /></label>
         <label class="field"><span>${t('password')}</span><input name="password" type="password" required autocomplete="current-password" placeholder="••••••••••••" /></label>
         <button class="btn btn-primary btn-block" type="submit">${t('signIn')} ${icon('arrow',17)}</button></form>
         <div class="demo-hint"><strong>${t('demoAccount')}</strong><p>demo@facilityflow.local / DemoPass123!</p><small>${t('demoAccountHint')}</small></div>
         <div class="login-divider"><span>OR / ODER</span></div>
         <a class="btn btn-showcase" href="?preview=1#/dashboard">${icon('bolt',18)}<span>${t('exploreDemo')}</span>${icon('arrow',18)}</a>
         <p class="showcase-note">${t('showcaseNote')}</p></div>
      <div class="login-links"><a target="_blank" rel="noopener noreferrer" href="https://github.com/ahadPorkar">GitHub ${icon('external',15)}</a>
      <a target="_blank" rel="noopener noreferrer" href="https://ahadporkar.com/">Portfolio ${icon('external',15)}</a></div></section></div>`;
}
function render(){document.documentElement.lang=locale();
  app.innerHTML=state.loading?`<div class="loading-screen">${brandMark()}<p>FacilityFlow</p><span class="loading-bar"></span></div>`:
    !state.user?loginView():shell();
  if(state.search)applySearch(state.search);
  if(state.route==='/orders'&&app.querySelector('#order-filter'))applyOrderFilter();
}
function applySearch(term){const val=term.toLowerCase().trim();
  document.querySelectorAll('[data-searchtext]').forEach(el=>{el.hidden=Boolean(val&&!el.dataset.searchtext.includes(val));});
}
function applyOrderFilter(){const val=document.querySelector('#order-filter')?.value||'';
  document.querySelectorAll('.data-table tbody tr').forEach(tr=>{
    const order=byId('orders',tr.dataset.id);
    tr.hidden=Boolean(val&&order?.status!==val) || Boolean(state.search&&!tr.dataset.searchtext.includes(state.search.toLowerCase()));
  });
}
async function executeMutation(path,body=null,method='POST'){
  await api(path,{method,body:body===null?undefined:JSON.stringify(body)});
  state.drawer=null;state.modal=null;await load();render();notify(t('workflowNotice'));
}
async function submitModal(form){
  const kind=form.dataset.kind;const id=form.dataset.id||null;
  const payload=Object.fromEntries(new FormData(form).entries());
  Object.keys(payload).forEach(key=>{if(payload[key]==='')payload[key]=null;});
  let path='',method=id?'PUT':'POST';
  if(kind==='site')path=`/sites${id?'/'+id:''}`;
  else if(kind==='asset'){
    path=`/assets${id?'/'+id:''}`;
    if(id && !payload.siteId)payload.siteId=byId('assets',id)?.siteId;
  }else if(kind==='plan'){
    path=`/maintenance-plans${id?'/'+id:''}`;
    payload.intervalDays=Number(payload.intervalDays);
    payload.active=Boolean(form.querySelector('[name="active"]')?.checked);
    if(id&&!payload.assetId)payload.assetId=byId('plans',id)?.assetId;
  }else if(kind==='order')path='/work-orders';
  else if(kind==='user')path='/team';
  else if(kind==='template'){
    path='/inspection-templates';payload.items=(payload.items||'').split('\n')
      .map(v=>v.trim()).filter(Boolean).map(label=>({label,required:true}));
    if(!payload.items.length)throw new Error(t('itemsRequired'));
  }else if(kind==='inspection'){
    path='/inspections';const detail=await api('/inspection-templates/'+payload.templateId);
    payload.results=detail.items.map(item=>({itemId:item.id,
      passed:form.querySelector(`input[name="res-${item.id}"]:checked`)?.value==='true',
      note:form.querySelector(`[name="note-${item.id}"]`)?.value||null}));
    if(detail.items.some(item=>!form.querySelector(`input[name="res-${item.id}"]:checked`)))
      throw new Error(t('itemsRequired'));
    for(const k of Object.keys(payload))if(k.startsWith('res-')||k.startsWith('note-'))delete payload[k];
  }else if(kind==='assign'){path=`/work-orders/${id}/assign`;method='POST';}
  else if(kind==='complete'){path=`/work-orders/${id}/complete`;method='POST';}
  if(!path)return;
  await api(path,{method,body:JSON.stringify(payload)});
  state.modal=null;state.drawer=null;await load();render();notify(id?t('updatedNotice'):t('createdNotice'));
}
async function handleAction(target) {
  const action=target.dataset.action;const id=target.dataset.id;
  if(action==='menu'){state.mobile=true;render();return;}
  if(action==='close-menu'){state.mobile=false;render();return;}
  if(action==='close-drawer'){state.drawer=null;render();return;}
  if(action==='close-modal'){state.modal=null;render();return;}
  if(action==='overlay' && target.classList.contains('overlay')){state.modal=null;render();return;}
  if(action==='navigate'){location.hash=target.dataset.to;return;}
  if(action==='open-form'){await showForm(target.dataset.kind,id||null,target.dataset.template||null);return;}
  if(action==='edit-asset'){await showForm('asset',id);return;}
  if(action==='edit-plan'){await showForm('plan',id);return;}
  if(action==='inspect-asset'){state.drawer=null;await showForm('inspection');const el=document.querySelector('[name="assetId"]');if(el)el.value=id;return;}
  if(action==='linked-order'){state.drawer={kind:'order',id};render();return;}
  if(action==='start'||action==='approve')return executeMutation(`/work-orders/${id}/${action}`);
  if(action==='assign'||action==='complete'){await showForm(action,id);return;}
  if(action==='cancel-order'){
    if(confirm(t('confirmCancel')))return executeMutation(`/work-orders/${id}/cancel`);return;
  }
  if(action==='resolve'){if(confirm(t('confirmResolve')))return executeMutation(`/defects/${id}/resolve`);return;}
  if(action==='generate-due'){
    const result=await api('/maintenance-plans/generate-due',{method:'POST'});await load();render();
    notify(t('createdCount',{count:result.created}));return;
  }
  if(action==='download-report'){
    await downloadPdf(target.dataset.kind==='inspection'?`/inspections/${id}/report`:`/work-orders/${id}/report`,locale());
    if(isPreview)notify(t('previewPrint'));return;
  }
  if(action==='refresh'){await load();render();notify(t('saved'));return;}
  if(action==='reset-demo'){if(confirm('Reset synthetic demo data?')){resetPreview();await load();render();}return;}
  if(action==='logout'){await logout();if(!isPreview){state.user=null;state.data={};render();}return;}
}
function showError(ex){notify(ex.status===403?t('permissionDenied'):(ex.message||t('failed')),'error');}
app.addEventListener('click',async event=>{
  const lang=event.target.closest('[data-lang]');
  if(lang){setLocale(lang.dataset.lang);render();return;}
  const nav=event.target.closest('[data-nav]');
  if(nav){state.mobile=false;state.search='';location.hash=nav.dataset.nav;render();return;}
  const action=event.target.closest('[data-action]');
  if(action){if(action.dataset.action==='overlay'&&event.target!==action)return;
    try{await handleAction(action);}catch(ex){showError(ex);}return;}
  const detail=event.target.closest('[data-detail]');
  if(detail&&!event.target.closest('button,a,input')){
    state.drawer={kind:detail.dataset.detail,id:detail.dataset.id};render();
  }
});
app.addEventListener('keydown',event=>{
  if((event.key==='Enter'||event.key===' ')&&event.target.matches('tr[data-detail]')){
    event.preventDefault();state.drawer={kind:event.target.dataset.detail,id:event.target.dataset.id};render();
  }
});
app.addEventListener('input',event=>{
  if(event.target.id==='global-search'){state.search=event.target.value;applySearch(state.search);if(state.route==='/orders')applyOrderFilter();}
});
app.addEventListener('change',async event=>{
  if(event.target.id==='order-filter')applyOrderFilter();
  if(event.target.name==='templateId'||(event.target.name==='assetId'&&document.querySelector('form[data-kind="inspection"]'))){
    try{await fillInspectionTemplate();}catch(ex){showError(ex);}
  }
});
app.addEventListener('submit',async event=>{
  if(event.target.id==='login-form'){
    event.preventDefault();const button=event.target.querySelector('button[type=submit]');button.disabled=true;
    const fd=new FormData(event.target);
    try{state.user=await login(fd.get('email'),fd.get('password'));await load();location.hash='/dashboard';render();}
    catch(ex){showError(new Error(t('signInError')));}finally{button.disabled=false;}return;
  }
  if(event.target.id==='modal-form'){
    event.preventDefault();const submit=event.target.querySelector('[type=submit]');submit.disabled=true;
    try{await submitModal(event.target);}catch(ex){showError(ex);submit.disabled=false;}
  }
});
window.addEventListener('hashchange',()=>{state.drawer=null;state.modal=null;state.mobile=false;state.search='';render();});
window.addEventListener('keydown',event=>{if(event.key==='Escape'){if(state.modal)state.modal=null;else state.drawer=null;render();}});
(async()=>{
  render();
  try{state.user=await initialize();if(state.user){await load();}else{state.loading=false;}
  }catch(ex){state.loading=false;showError(ex);}
  render();
})();

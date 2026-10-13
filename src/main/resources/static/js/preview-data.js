import {isoDate} from './utils.js';

const uid=()=>`pv-${Math.random().toString(36).slice(2,10)}`;
const now=()=>new Date().toISOString();
const daysAgo=(days)=>new Date(Date.now()-days*86400000).toISOString();
const stamp = (obj)=>({id:uid(),organizationId:'preview-org',createdAt:now(),...obj});

export function createPreviewState() {
  const sites=[
    stamp({id:'st-1',code:'BER-001',name:'The Atrium',address:'Friedrichstraße 80',city:'Berlin',postalCode:'10117',contactName:'Lena Weber',contactEmail:'contact@example.invalid'}),
    stamp({id:'st-2',code:'BER-002',name:'Kreuzberg Hub',address:'Oranienstraße 41',city:'Berlin',postalCode:'10999'}),
    stamp({id:'st-3',code:'HAM-003',name:'Hafen Offices',address:'Am Sandtorkai 12',city:'Hamburg',postalCode:'20457'}),
    stamp({id:'st-4',code:'MUC-004',name:'Nord Campus',address:'Leopoldstraße 25',city:'München',postalCode:'80802'})
  ];
  const assets=[
    ['a1','st-1','HVAC-101','Air handling unit AHU-01','HVAC','Rooftop plant room'],
    ['a2','st-1','FIRE-202','Fire alarm control panel','FIRE_SAFETY','Basement B1'],
    ['a3','st-1','EL-203','Main distribution board','ELECTRICAL','Electrical room'],
    ['a4','st-2','HVAC-310','Heating circulation pump','HVAC','Utility plant room'],
    ['a5','st-2','EL-311','Emergency light system','ELECTRICAL','North wing'],
    ['a6','st-2','PL-312','Water pressure station','PLUMBING','Basement'],
    ['a7','st-3','LIFT-410','Passenger elevator A','ELEVATOR','Main lobby'],
    ['a8','st-3','HVAC-411','Ventilation fan 2','HVAC','Rooftop'],
    ['a9','st-3','FIRE-412','Smoke detector circuit','FIRE_SAFETY','Level 2'],
    ['a10','st-4','HVAC-510','Central cooling unit','HVAC','Plant room'],
    ['a11','st-4','EL-511','UPS battery system','ELECTRICAL','Server room'],
    ['a12','st-4','PL-512','Backflow preventer','PLUMBING','Utility room']
  ].map(([id,siteId,code,name,type,location])=>stamp({id,siteId,code,name,type,location,manufacturer:'Siemens',status:'ACTIVE'}));
  const team=[
    {id:'u1',displayName:'Olivia Weber',email:'demo@facilityflow.local',role:'ADMIN',enabled:true},
    {id:'u2',displayName:'Jonas Richter',email:'technician@facilityflow.local',role:'TECHNICIAN',enabled:true},
    {id:'u3',displayName:'Lena Fischer',email:'viewer@facilityflow.local',role:'VIEWER',enabled:true},
    {id:'u4',displayName:'Max Hoffmann',email:'manager@facilityflow.local',role:'MANAGER',enabled:true}
  ];
  const plans=assets.slice(0,8).map((asset,i)=>stamp({
    id:'plan-'+i,assetId:asset.id,title:`Preventive service · ${asset.name}`,
    intervalDays:i%2?90:30,nextDueDate:isoDate(i*6-5),active:true,
    assignedToUserId:'u2',instructions:'Visual inspection, functional test and documentation.'
  }));
  const orders=[
    ['wo1','st-1','a1','Replace AHU-01 air filter','HIGH','IN_PROGRESS',1,'u2'],
    ['wo2','st-1','a2','Fire panel functional test','CRITICAL','ASSIGNED',-2,'u2'],
    ['wo3','st-2','a4','Heating pump vibration check','MEDIUM','OPEN',3,null],
    ['wo4','st-3','a7','Elevator inspection follow-up','HIGH','COMPLETED',1,'u2'],
    ['wo5','st-4','a11','UPS preventive maintenance','LOW','APPROVED',-2,'u2'],
    ['wo6','st-2','a5','Emergency lighting walk-through','MEDIUM','ASSIGNED',7,'u2'],
    ['wo7','st-4','a10','Cooling unit seasonal service','HIGH','OPEN',5,null],
    ['wo8','st-3','a8','Check ventilation performance','LOW','COMPLETED',3,'u2'],
  ].map(([id,siteId,assetId,title,priority,status,dueDays,assignedToUserId],i)=>stamp({
    id,number:`WO-10${31+i}`,siteId,assetId,title,description:'Scheduled building maintenance task',
    priority,status,dueDate:isoDate(dueDays),assignedToUserId,
    resolution:['APPROVED','COMPLETED'].includes(status)?'Work completed and documented.':null,
    createdAt:daysAgo(1+i)
  }));
  const template=stamp({id:'tp1',name:'Technical safety inspection',description:'Functional, visual and documentation checks',assetType:null,active:true});
  const items=['No visible damage','Safety guards intact','Functional test passed','Maintenance label current']
    .map((label,i)=>stamp({id:`ci${i}`,templateId:'tp1',position:i+1,label,required:true}));
  const inspection=stamp({id:'in1',number:'IN-1001',templateId:'tp1',assetId:'a2',inspectorUserId:'u2',status:'FAIL',
    notes:'Emergency signal delay requires corrective action.',performedAt:daysAgo(1)});
  const results=items.map((item,i)=>stamp({inspectionId:'in1',checklistItemId:item.id,labelSnapshot:item.label,
    position:i+1,passed:i!==2,note:i===2?'Signal delay above accepted threshold':''}));
  const defects=[stamp({id:'df1',assetId:'a2',inspectionId:'in1',correctiveWorkOrderId:'wo2',
    title:'Functional test passed',details:'Emergency signal delay measured',severity:'HIGH',status:'OPEN'})];
  const audit=[
    ['WORK_ORDER','wo1','STARTED','Work started'],['INSPECTION','in1','SUBMITTED','Inspection failed'],
    ['WORK_ORDER','wo2','ASSIGNED','Technician assigned'],['MAINTENANCE_PLAN','plan-1','CREATED','Plan published'],
    ['ASSET','a3','CREATED','Equipment registered'],['SITE','st-4','CREATED','Building registered']
  ].map(([entityType,entityId,action,description],i)=>stamp({id:`ev${i}`,actorUserId:'u1',entityType,entityId,action,description,
    createdAt:daysAgo(i===0?0:i)}));
  return {sites,assets,team,plans,orders,templates:[template],items,inspections:[inspection],results,defects,audit};
}

export function createPreviewClient(storage=null) {
  const key='facilityflow.preview.v1';
  let state;
  try { state=JSON.parse(storage?.getItem(key)||'null')||createPreviewState(); }
  catch { state=createPreviewState(); }
  const save=()=>{try {storage?.setItem(key,JSON.stringify(state));}catch{ /* private mode */ }};
  const event=(entityType,entityId,action,description)=>state.audit.unshift(stamp({actorUserId:'u1',entityType,entityId,action,description}));
  const one=(arr,id)=>arr.find(x=>x.id===id);
  const fail=(message)=>{throw new Error(message);};
  const clone=(value)=>structuredClone(value);
  const statuses=['OPEN','ASSIGNED','IN_PROGRESS','COMPLETED','APPROVED','CANCELLED'];
  const resourceNames={'sites':'sites','assets':'assets','maintenance-plans':'plans','work-orders':'orders',
    'inspection-templates':'templates','inspections':'inspections','defects':'defects','team':'team','audit-events':'audit'};
  const dashboard=()=>({
    sites:state.sites.length,assets:state.assets.length,
    activeAssets:state.assets.filter(x=>x.status==='ACTIVE').length,
    activePlans:state.plans.filter(x=>x.active).length,
    openDefects:state.defects.filter(x=>x.status==='OPEN').length,
    overdue:state.orders.filter(x=>x.dueDate && x.dueDate<isoDate() && !['APPROVED','CANCELLED'].includes(x.status)).length,
    workOrderStatuses:Object.fromEntries(statuses.map(s=>[s,state.orders.filter(x=>x.status===s).length])),
    recentOrders:state.orders.slice(0,5),activity:state.audit.slice(0,6)
  });
  const handle=(rawPath,opts={})=>{
    const url=new URL(rawPath,'https://preview.invalid');
    const segments=url.pathname.replace(/^\/api\//,'/').split('/').filter(Boolean);
    const [section,id,action]=segments;
    const method=(opts.method||'GET').toUpperCase();
    const input=typeof opts.body==='string'?JSON.parse(opts.body):(opts.body||{});
    if(section==='dashboard')return clone(dashboard());
    if(section==='auth') {
      if(id==='me'||id==='login')return {id:'u1',name:'Olivia Weber',email:'demo@facilityflow.local',role:'ADMIN',organization:'Nordhaus Facility Services'};
      if(id==='logout'||id==='csrf')return {success:true,token:'preview'};
    }
    if(section==='maintenance-plans' && id==='generate-due' && method==='POST') {
      let count=0;
      for(const p of state.plans.filter(p=>p.active&&p.nextDueDate<=isoDate())) {
        const a=one(state.assets,p.assetId);
        state.orders.unshift(stamp({number:`WO-P${Math.floor(Math.random()*9999)}`,siteId:a.siteId,assetId:a.id,
          maintenancePlanId:p.id,title:p.title,priority:'MEDIUM',status:p.assignedToUserId?'ASSIGNED':'OPEN',
          dueDate:p.nextDueDate,assignedToUserId:p.assignedToUserId,description:p.instructions}));
        do {const d=new Date(`${p.nextDueDate}T12:00:00Z`);d.setUTCDate(d.getUTCDate()+p.intervalDays);
          p.nextDueDate=d.toISOString().slice(0,10);}while(p.nextDueDate<=isoDate());
        count++;
      }
      save();return {created:count};
    }
    if(section==='inspection-templates' && method==='GET' && id) return clone({
      template:one(state.templates,id)||fail('Not found'),items:state.items.filter(x=>x.templateId===id)
    });
    if(section==='inspections' && method==='GET' && id && !action) return clone({
      inspection:one(state.inspections,id)||fail('Not found'),
      results:state.results.filter(x=>x.inspectionId===id),defects:state.defects.filter(x=>x.inspectionId===id)
    });
    if(method==='POST' && section==='work-orders' && action) {
      const w=one(state.orders,id)||fail('Not found');
      const transitions={start:['ASSIGNED','IN_PROGRESS'],complete:['IN_PROGRESS','COMPLETED'],
        approve:['COMPLETED','APPROVED'],cancel:['OPEN','CANCELLED']};
      if(action==='assign') {
        if(!['OPEN','ASSIGNED'].includes(w.status))fail('Only open or assigned orders can be reassigned');
        w.assignedToUserId=input.userId;w.status='ASSIGNED';
      } else {
        const [expected,next]=transitions[action]||fail('Unknown transition');
        if(w.status!==expected && !(action==='cancel'&&w.status==='ASSIGNED'))fail('Invalid workflow transition');
        w.status=next;if(action==='complete')w.resolution=input.resolution;
        if(action==='approve')state.defects.filter(x=>x.correctiveWorkOrderId===id).forEach(x=>x.status='RESOLVED');
      }
      event('WORK_ORDER',w.id,action.toUpperCase(),'Work order workflow updated');save();return clone(w);
    }
    if(method==='POST' && section==='defects' && action==='resolve') {
      const defect=one(state.defects,id)||fail('Not found');if(defect.status!=='OPEN')fail('Already resolved');
      defect.status='RESOLVED';defect.resolvedAt=now();event('DEFECT',id,'RESOLVED','Defect resolved');save();return clone(defect);
    }
    if(method==='POST' && section==='inspections' && !id) {
      const a=one(state.assets,input.assetId);const tmpl=one(state.templates,input.templateId);
      if(!a||!tmpl)fail('Invalid asset or checklist');
      const expected=state.items.filter(x=>x.templateId===tmpl.id);
      if(input.results.length!==expected.length||new Set(input.results.map(x=>x.itemId)).size!==expected.length)fail('Every item must be answered');
      const failures=input.results.filter(x=>!x.passed);
      const inspection=stamp({number:`IN-${Math.floor(Math.random()*9000)+1000}`,assetId:a.id,templateId:tmpl.id,
        inspectorUserId:'u1',performedAt:now(),status:failures.length?'FAIL':'PASS',notes:input.notes});
      state.inspections.unshift(inspection);
      input.results.forEach(r=>{const check=one(expected,r.itemId);state.results.push(stamp({inspectionId:inspection.id,
        checklistItemId:r.itemId,labelSnapshot:check.label,position:check.position,passed:r.passed,note:r.note}));});
      let corrective=null;
      if(failures.length) {
        corrective=stamp({number:`WO-${Math.floor(Math.random()*9000)+1000}`,siteId:a.siteId,assetId:a.id,
          title:`Corrective action - ${a.name}`,priority:'HIGH',status:'OPEN',dueDate:isoDate(7),description:'Inspection findings'});
        state.orders.unshift(corrective);
        failures.forEach(r=>{const item=one(expected,r.itemId);state.defects.unshift(stamp({
          assetId:a.id,inspectionId:inspection.id,correctiveWorkOrderId:corrective.id,title:item.label,
          details:r.note,severity:item.required?'HIGH':'MEDIUM',status:'OPEN'
        }));});
      }
      event('INSPECTION',inspection.id,'SUBMITTED',failures.length?'Inspection failed':'Inspection passed');save();
      return handle(`/api/inspections/${inspection.id}`);
    }
    if(method==='POST' && section==='inspection-templates' && !id) {
      const tmpl=stamp({name:input.name,description:input.description,assetType:input.assetType||null,active:true});
      state.templates.push(tmpl);input.items.forEach((it,i)=>state.items.push(stamp({
        templateId:tmpl.id,label:it.label,required:it.required??true,position:i+1
      })));
      event('INSPECTION_TEMPLATE',tmpl.id,'CREATED','Checklist created');save();
      return handle(`/api/inspection-templates/${tmpl.id}`);
    }
    if(resourceNames[section]) {
      const rows=state[resourceNames[section]];
      if(method==='GET') return clone(id?(one(rows,id)||fail('Not found')):rows);
      if(method==='POST' || method==='PUT') {
        let item=id?one(rows,id):null;
        if(id&&!item)fail('Not found');
        if(!item) {item=stamp({...input});rows.unshift(item);} else Object.assign(item,input);
        if(section==='work-orders')Object.assign(item,{number:item.number||`WO-${Math.floor(Math.random()*9000)}`,
          status:item.status||(input.assignedToUserId?'ASSIGNED':'OPEN')});
        if(section==='assets')item.status=item.status||'ACTIVE';
        if(section==='team')Object.assign(item,{enabled:true,password:undefined});
        event(section.toUpperCase(),item.id,id?'UPDATED':'CREATED','Record saved');save();return clone(item);
      }
    }
    fail(`Unsupported preview request: ${method} ${rawPath}`);
  };
  return {handle, reset(){state=createPreviewState();save();}, snapshot(){return clone(state);}};
}

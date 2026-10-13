import test from 'node:test';
import assert from 'node:assert/strict';
import {createPreviewClient,createPreviewState} from '../src/main/resources/static/js/preview-data.js';
import {isoDate} from '../src/main/resources/static/js/utils.js';

const api=()=>createPreviewClient();

test('Dashboard, catalog, team, inspection and report summary have coherent sample fixtures',()=>{
  const client=api();const data=client.snapshot();
  assert.equal(data.sites.length,4);
  assert.equal(client.handle('/api/dashboard').assets,12);
  assert.equal(client.handle('/api/team').length,4);
  assert.equal(client.handle('/api/inspections/in1').defects.length,1);
  assert.equal(client.handle('/api/inspection-templates/tp1').items.length,4);
});
test('Building and equipment CRUD in interactive showcase',()=>{
  const client=api();
  const s=client.handle('/api/sites',{method:'POST',body:JSON.stringify({code:'BER-999',name:'New wing',city:'Berlin'})});
  assert.equal(client.handle('/api/sites/'+s.id).name,'New wing');
  const updated=client.handle('/api/sites/'+s.id,{method:'PUT',body:JSON.stringify({name:'East wing'})});
  assert.equal(updated.name,'East wing');
  const asset=client.handle('/api/assets',{method:'POST',body:JSON.stringify({siteId:s.id,code:'AIR-1',name:'Air conditioning',type:'HVAC'})});
  assert.equal(client.handle('/api/assets/'+asset.id).code,'AIR-1');
});
test('New work order can be assigned, started, completed and approved',()=>{
  const client=api();
  const order=client.handle('/api/work-orders',{method:'POST',body:JSON.stringify({siteId:'st-1',assetId:'a1',title:'Fix fan',priority:'HIGH'})});
  assert.equal(order.status,'OPEN');
  assert.equal(client.handle(`/api/work-orders/${order.id}/assign`,{method:'POST',body:JSON.stringify({userId:'u2'})}).status,'ASSIGNED');
  assert.equal(client.handle(`/api/work-orders/${order.id}/start`,{method:'POST'}).status,'IN_PROGRESS');
  assert.equal(client.handle(`/api/work-orders/${order.id}/complete`,{method:'POST',body:JSON.stringify({resolution:'Repaired'})}).status,'COMPLETED');
  assert.equal(client.handle(`/api/work-orders/${order.id}/approve`,{method:'POST'}).status,'APPROVED');
  assert.throws(()=>client.handle(`/api/work-orders/${order.id}/start`,{method:'POST'}),/Invalid workflow transition/);
});
test('Preventive maintenance generator is idempotent',()=>{
  const client=api();
  const before=client.handle('/api/maintenance-plans').length;
  const first=client.handle('/api/maintenance-plans/generate-due',{method:'POST'});
  const second=client.handle('/api/maintenance-plans/generate-due',{method:'POST'});
  assert.ok(first.created>=1);
  assert.equal(second.created,0);
  assert.equal(client.handle('/api/maintenance-plans').length,before);
});
test('Inspection failure creates one corrective order and defect, approval closes defect',()=>{
  const client=api();const before=client.handle('/api/dashboard');
  const checklist=client.handle('/api/inspection-templates/tp1').items;
  const created=client.handle('/api/inspections',{method:'POST',body:JSON.stringify({assetId:'a1',templateId:'tp1',notes:'Test',
    results:checklist.map((item,i)=>({itemId:item.id,passed:i!==1,note:i===1?'Leaking':'OK'}))})});
  assert.equal(created.inspection.status,'FAIL');assert.equal(created.defects.length,1);
  assert.equal(client.handle('/api/dashboard').openDefects,before.openDefects+1);
  const wo=client.handle('/api/work-orders/'+created.defects[0].correctiveWorkOrderId);
  client.handle(`/api/work-orders/${wo.id}/assign`,{method:'POST',body:JSON.stringify({userId:'u2'})});
  client.handle(`/api/work-orders/${wo.id}/start`,{method:'POST'});
  client.handle(`/api/work-orders/${wo.id}/complete`,{method:'POST',body:JSON.stringify({resolution:'Repaired'})});
  client.handle(`/api/work-orders/${wo.id}/approve`,{method:'POST'});
  const defect=client.handle('/api/defects/'+created.defects[0].id);
  assert.equal(defect.status,'RESOLVED');
});
test('Incomplete inspections are rejected',()=>{
  const client=api();
  assert.throws(()=>client.handle('/api/inspections',{method:'POST',body:JSON.stringify({
    assetId:'a1',templateId:'tp1',results:[]
  })}),/Every item must be answered/);
});
test('Template creation, defect resolution, audit and user administration are functional',()=>{
  const client=api();
  const t=client.handle('/api/inspection-templates',{method:'POST',body:JSON.stringify({
    name:'Lift inspection',items:[{label:'Safety lock',required:true}]
  })});
  assert.equal(t.items.length,1);
  const resolved=client.handle('/api/defects/df1/resolve',{method:'POST'});
  assert.equal(resolved.status,'RESOLVED');
  const member=client.handle('/api/team',{method:'POST',body:JSON.stringify({
    displayName:'New member',email:'new@example.invalid',role:'TECHNICIAN',password:'unused-password'})});
  assert.equal(member.role,'TECHNICIAN');
  assert.ok(client.handle('/api/audit-events').length>=3);
});

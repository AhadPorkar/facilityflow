package com.ahadporkar.facilityflow.bootstrap;

import com.ahadporkar.facilityflow.domain.*;
import com.ahadporkar.facilityflow.domain.Enums.*;
import com.ahadporkar.facilityflow.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

/** Seeded demo is explicitly opt-in. No demo accounts are created in production by default. */
@Component
public class BootstrapData implements CommandLineRunner {
    private final OrganizationRepository organizations;private final UserAccountRepository users;
    private final SiteRepository sites;private final AssetRepository assets;private final WorkOrderRepository orders;
    private final MaintenancePlanRepository plans;private final InspectionTemplateRepository templates;
    private final ChecklistItemRepository items;private final InspectionRepository inspections;
    private final InspectionResultRepository results;private final DefectRepository defects;
    private final AuditEventRepository events;private final PasswordEncoder encoder;
    @Value("${app.demo-seed:false}") private boolean demo;
    @Value("${app.bootstrap-email:}") private String bootstrapEmail;
    @Value("${app.bootstrap-password:}") private String bootstrapPassword;
    @Value("${app.bootstrap-name:FacilityFlow Admin}") private String bootstrapName;
    public BootstrapData(OrganizationRepository organizations,UserAccountRepository users,
        SiteRepository sites,AssetRepository assets,WorkOrderRepository orders,
        MaintenancePlanRepository plans,InspectionTemplateRepository templates,
        ChecklistItemRepository items,InspectionRepository inspections,InspectionResultRepository results,
        DefectRepository defects,AuditEventRepository events,PasswordEncoder encoder) {
        this.organizations=organizations;this.users=users;this.sites=sites;this.assets=assets;
        this.orders=orders;this.plans=plans;this.templates=templates;this.items=items;
        this.inspections=inspections;this.results=results;this.defects=defects;this.events=events;this.encoder=encoder;
    }
    @Override @Transactional
    public void run(String... args) {
        if (organizations.count()!=0) return;
        if (demo) { seedDemo();return; }
        if (!bootstrapEmail.isBlank() && bootstrapPassword.length()>=12) {
            Organization org=org("My Facility Organization");
            user(org,"Administrator",bootstrapEmail.toLowerCase(Locale.ROOT),bootstrapPassword,Role.ADMIN);
        }
        // Without bootstrap credentials, the database remains empty until provisioning is configured.
    }
    private Organization org(String name) {
        Organization o=new Organization();o.setName(name);return organizations.save(o);
    }
    private UserAccount user(Organization o,String name,String email,String password,Role role) {
        UserAccount u=new UserAccount();u.setOrganizationId(o.getId());u.setDisplayName(name);
        u.setEmail(email);u.setPasswordHash(encoder.encode(password));u.setRole(role);u.setEnabled(true);
        return users.save(u);
    }
    private Site site(UUID org,String code,String name,String address,String city) {
        Site s=new Site();s.setOrganizationId(org);s.setCode(code);s.setName(name);
        s.setAddress(address);s.setCity(city);s.setPostalCode("10115");return sites.save(s);
    }
    private Asset asset(UUID org,Site site,String code,String name,AssetType type,String location) {
        Asset a=new Asset();a.setOrganizationId(org);a.setSiteId(site.getId());a.setCode(code);
        a.setName(name);a.setType(type);a.setLocation(location);a.setManufacturer("Siemens");
        a.setStatus(AssetStatus.ACTIVE);return assets.save(a);
    }
    private WorkOrder work(UUID org,Site site,Asset asset,String name,Priority priority,WorkStatus status,
                           LocalDate due,UserAccount technician) {
        WorkOrder w=new WorkOrder();w.setOrganizationId(org);w.setSiteId(site.getId());
        w.setAssetId(asset.getId());w.setNumber("WO-"+UUID.randomUUID().toString().substring(0,8).toUpperCase(Locale.ROOT));
        w.setTitle(name);w.setPriority(priority);w.setStatus(status);w.setDueDate(due);
        w.setAssignedToUserId(technician==null?null:technician.getId());w.setUpdatedAt(Instant.now());
        if(status==WorkStatus.COMPLETED||status==WorkStatus.APPROVED) {
            w.setResolution("Inspection completed and documented.");w.setCompletedAt(Instant.now());
        }
        return orders.save(w);
    }
    private void seedDemo() {
        Organization org=org("Nordhaus Facility Services");UUID oid=org.getId();
        UserAccount manager=user(org,"Olivia Weber","demo@facilityflow.local","DemoPass123!",Role.ADMIN);
        UserAccount tech=user(org,"Jonas Richter","technician@facilityflow.local","DemoPass123!",Role.TECHNICIAN);
        user(org,"Lena Fischer","viewer@facilityflow.local","DemoPass123!",Role.VIEWER);
        Site a=site(oid,"BER-001","The Atrium","Friedrichstrasse 80","Berlin");
        Site b=site(oid,"BER-002","Kreuzberg Hub","Oranienstrasse 41","Berlin");
        Site c=site(oid,"HAM-003","Hafen Offices","Am Sandtorkai 12","Hamburg");
        Site d=site(oid,"MUC-004","Nord Campus","Leopoldstrasse 25","Munich");
        List<Asset> machines=List.of(
          asset(oid,a,"HVAC-101","Air handling unit AHU-01",AssetType.HVAC,"Roof level"),
          asset(oid,a,"FIRE-202","Fire alarm control panel",AssetType.FIRE_SAFETY,"Basement"),
          asset(oid,a,"EL-203","Main distribution board",AssetType.ELECTRICAL,"Electrical room"),
          asset(oid,b,"HVAC-310","Heating circulation pump",AssetType.HVAC,"Plant room"),
          asset(oid,b,"EL-311","Emergency light system",AssetType.ELECTRICAL,"North wing"),
          asset(oid,b,"PL-312","Water pressure station",AssetType.PLUMBING,"Basement"),
          asset(oid,c,"LIFT-410","Passenger lift A",AssetType.ELEVATOR,"Main hall"),
          asset(oid,c,"HVAC-411","Ventilation fan 2",AssetType.HVAC,"Roof level"),
          asset(oid,c,"FIRE-412","Smoke detector circuit",AssetType.FIRE_SAFETY,"Level 2"),
          asset(oid,d,"HVAC-510","Central cooling unit",AssetType.HVAC,"Plant room"),
          asset(oid,d,"EL-511","UPS battery system",AssetType.ELECTRICAL,"Server room"),
          asset(oid,d,"PL-512","Backflow preventer",AssetType.PLUMBING,"Utility room"));
        LocalDate today=LocalDate.now(ZoneOffset.UTC);
        for(int i=0;i<8;i++) {
            Asset asset=machines.get(i);MaintenancePlan p=new MaintenancePlan();p.setOrganizationId(oid);
            p.setAssetId(asset.getId());p.setTitle("Preventive service - "+asset.getName());
            p.setIntervalDays(i%2==0?30:90);p.setNextDueDate(today.plusDays(i*6-5));
            p.setActive(true);p.setInstructions("Visual inspection, functional test and documentation.");
            p.setAssignedToUserId(tech.getId());plans.save(p);
        }
        work(oid,a,machines.get(0),"Replace AHU-01 air filter",Priority.HIGH,WorkStatus.IN_PROGRESS,today.plusDays(1),tech);
        work(oid,a,machines.get(1),"Fire panel functional test",Priority.CRITICAL,WorkStatus.ASSIGNED,today.minusDays(1),tech);
        work(oid,b,machines.get(3),"Heating pump vibration check",Priority.MEDIUM,WorkStatus.OPEN,today.plusDays(3),null);
        work(oid,c,machines.get(6),"Elevator inspection follow-up",Priority.HIGH,WorkStatus.COMPLETED,today.plusDays(1),tech);
        work(oid,d,machines.get(10),"UPS preventive maintenance",Priority.LOW,WorkStatus.APPROVED,today.minusDays(2),tech);
        work(oid,b,machines.get(4),"Emergency lighting walk-through",Priority.MEDIUM,WorkStatus.ASSIGNED,today.plusDays(7),tech);
        work(oid,d,machines.get(9),"Cooling unit seasonal service",Priority.HIGH,WorkStatus.OPEN,today.plusDays(5),null);
        InspectionTemplate tmpl=new InspectionTemplate();tmpl.setOrganizationId(oid);
        tmpl.setName("Technical safety inspection");tmpl.setDescription("Visual, operational and documentation controls");
        tmpl.setActive(true);templates.save(tmpl);
        String[] labels={"No visible damage","Safety guards intact","Functional test passed","Maintenance label current"};
        List<ChecklistItem> checks=new ArrayList<>();int i=0;
        for(String label:labels) {ChecklistItem ci=new ChecklistItem();ci.setOrganizationId(oid);
            ci.setTemplateId(tmpl.getId());ci.setPosition(++i);ci.setLabel(label);ci.setRequired(true);
            checks.add(items.save(ci));}
        Inspection inspect=new Inspection();inspect.setOrganizationId(oid);inspect.setTemplateId(tmpl.getId());
        inspect.setAssetId(machines.get(1).getId());inspect.setInspectorUserId(tech.getId());
        inspect.setPerformedAt(Instant.now().minusSeconds(86400));inspect.setStatus(InspectionStatus.FAIL);
        inspect.setNotes("Follow up required for control panel.");inspect.setNumber("IN-DEMO-001");inspections.save(inspect);
        for(int j=0;j<checks.size();j++) {InspectionResult ir=new InspectionResult();ir.setOrganizationId(oid);
            ir.setInspectionId(inspect.getId());ir.setChecklistItemId(checks.get(j).getId());
            ir.setLabelSnapshot(checks.get(j).getLabel());ir.setPosition(j+1);ir.setPassed(j!=2);
            ir.setNote(j==2?"Emergency signal delay measured":"");results.save(ir);}
        Defect defect=new Defect();defect.setOrganizationId(oid);defect.setAssetId(machines.get(1).getId());
        defect.setInspectionId(inspect.getId());defect.setTitle("Functional test passed");
        defect.setDetails("Emergency signal delay measured");defect.setSeverity(Priority.HIGH);
        defect.setStatus(DefectStatus.OPEN);defects.save(defect);
        for(String action:List.of("CREATED","ASSIGNED","INSPECTION_SUBMITTED","PLAN_ACTIVATED")) {
            AuditEvent e=new AuditEvent();e.setOrganizationId(oid);e.setActorUserId(manager.getId());
            e.setEntityType("SYSTEM");e.setEntityId(oid);e.setAction(action);
            e.setDescription("Demo operations event");events.save(e);
        }
    }
}

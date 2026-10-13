package com.ahadporkar.facilityflow;

import com.ahadporkar.facilityflow.domain.*;
import com.ahadporkar.facilityflow.domain.Enums.*;
import com.ahadporkar.facilityflow.repository.*;
import com.ahadporkar.facilityflow.security.PrincipalUser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Real PostgreSQL + Flyway + HTTP/security integration contract tests. */
@Testcontainers
@SpringBootTest(properties={"app.demo-seed=false","app.bootstrap-email=","app.bootstrap-password="})
@AutoConfigureMockMvc
@Transactional
class FacilityFlowIntegrationTest {
    @Container static final PostgreSQLContainer<?> DB=new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("facilityflow_test").withUsername("test").withPassword("test");
    @DynamicPropertySource static void database(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url",DB::getJdbcUrl);
        r.add("spring.datasource.username",DB::getUsername);
        r.add("spring.datasource.password",DB::getPassword);
    }
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired PasswordEncoder encoder;
    @Autowired OrganizationRepository organizations;
    @Autowired UserAccountRepository users;
    @Autowired SiteRepository sites;
    @Autowired AssetRepository assets;
    @Autowired WorkOrderRepository orders;
    @Autowired MaintenancePlanRepository plans;
    @Autowired InspectionTemplateRepository templates;
    @Autowired ChecklistItemRepository items;
    @Autowired InspectionRepository inspections;
    @Autowired InspectionResultRepository results;
    @Autowired DefectRepository defects;
    @Autowired AuditEventRepository audit;
    private Organization org,foreign;
    private UserAccount admin,tech,viewer,foreignAdmin;
    private Site site,foreignSite;
    private Asset asset;

    @BeforeEach void setUp(){
        org=new Organization();org.setName("Test operations");organizations.save(org);
        foreign=new Organization();foreign.setName("Other organization");organizations.save(foreign);
        admin=account(org,"admin",Role.ADMIN);tech=account(org,"technician",Role.TECHNICIAN);
        viewer=account(org,"viewer",Role.VIEWER);foreignAdmin=account(foreign,"foreign",Role.ADMIN);
        site=new Site();site.setOrganizationId(org.getId());site.setCode("BER-T01");site.setName("Berlin HQ");
        site.setCity("Berlin");sites.save(site);
        foreignSite=new Site();foreignSite.setOrganizationId(foreign.getId());
        foreignSite.setCode("MUN-T02");foreignSite.setName("Munich office");sites.save(foreignSite);
        asset=new Asset();asset.setOrganizationId(org.getId());asset.setSiteId(site.getId());
        asset.setCode("HVAC-T01");asset.setName("Air handling unit");
        asset.setType(AssetType.HVAC);asset.setStatus(AssetStatus.ACTIVE);assets.save(asset);
    }
    private UserAccount account(Organization tenant,String name,Role role){
        UserAccount a=new UserAccount();a.setOrganizationId(tenant.getId());
        a.setEmail(name+"-"+UUID.randomUUID().toString().substring(0,6)+"@test.example");
        a.setPasswordHash(encoder.encode("LongStrongPass123!"));
        a.setDisplayName(name);a.setRole(role);a.setEnabled(true);return users.save(a);
    }
    private String body(Object payload)throws Exception{return json.writeValueAsString(payload);}
    private JsonNode response(org.springframework.test.web.servlet.ResultActions result)throws Exception{
        return json.readTree(result.andReturn().getResponse().getContentAsString());
    }
    private WorkOrder order(WorkStatus status,UserAccount assignee){
        WorkOrder w=new WorkOrder();w.setOrganizationId(org.getId());w.setSiteId(site.getId());
        w.setAssetId(asset.getId());w.setNumber("WO-"+UUID.randomUUID().toString().substring(0,8));
        w.setTitle("HVAC service");w.setStatus(status);w.setPriority(Priority.HIGH);
        w.setAssignedToUserId(assignee==null?null:assignee.getId());
        w.setDueDate(LocalDate.now().plusDays(3));return orders.save(w);
    }
    private InspectionTemplate template(){
        InspectionTemplate t=new InspectionTemplate();t.setOrganizationId(org.getId());
        t.setName("Functional test");t.setActive(true);templates.save(t);
        for(int x=0;x<2;x++){ChecklistItem item=new ChecklistItem();item.setOrganizationId(org.getId());
            item.setTemplateId(t.getId());item.setLabel("Safety checkpoint "+x);item.setRequired(true);
            item.setPosition(x+1);items.save(item);}
        return t;
    }

    @Test @DisplayName("Security: unauthorized API is rejected") void authenticationIsRequired()throws Exception{
        mvc.perform(get("/api/dashboard")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/me").with(user(new PrincipalUser(admin))))
            .andExpect(status().isOk()).andExpect(jsonPath("$.role").value("ADMIN"));
    }
    @Test @DisplayName("Authentication: real BCrypt login creates an authenticated session") void loginWorks()throws Exception{
        mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
          .content(body(Map.of("email",admin.getEmail(),"password","LongStrongPass123!"))))
          .andExpect(status().isOk()).andExpect(jsonPath("$.organization").value("Test operations"));
    }
    @Test @DisplayName("Authorization: viewer cannot modify buildings") void viewerIsReadOnly()throws Exception{
        mvc.perform(post("/api/sites").with(user(new PrincipalUser(viewer))).with(csrf())
          .contentType(MediaType.APPLICATION_JSON).content(body(Map.of("code","N1","name","No"))))
          .andExpect(status().isForbidden());
    }
    @Test @DisplayName("Tenant isolation: cannot query a foreign building") void noCrossTenantSites()throws Exception{
        mvc.perform(get("/api/sites/"+foreignSite.getId()).with(user(new PrincipalUser(admin))))
          .andExpect(status().isNotFound());
        mvc.perform(post("/api/work-orders").with(user(new PrincipalUser(admin))).with(csrf())
          .contentType(MediaType.APPLICATION_JSON).content(body(Map.of(
            "siteId",foreignSite.getId(),"title","Bad access","priority","HIGH"))))
          .andExpect(status().isNotFound());
    }
    @Test @DisplayName("Building register: create, update and list") void sitesCrud()throws Exception{
        JsonNode result=response(mvc.perform(post("/api/sites")
          .with(user(new PrincipalUser(admin))).with(csrf()).contentType(MediaType.APPLICATION_JSON)
          .content(body(Map.of("code","BER-002","name","New site","city","Berlin"))))
          .andExpect(status().isCreated()));
        String id=result.get("id").asText();
        mvc.perform(put("/api/sites/"+id).with(user(new PrincipalUser(admin))).with(csrf())
          .contentType(MediaType.APPLICATION_JSON).content(body(Map.of("code","BER-002","name","Renamed site"))))
          .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Renamed site"));
        mvc.perform(get("/api/sites").with(user(new PrincipalUser(admin))))
          .andExpect(status().isOk()).andExpect(jsonPath("$[0].code").exists());
    }
    @Test @DisplayName("Equipment: site must belong to same tenant") void assetsCannotCrossTenant()throws Exception{
        mvc.perform(post("/api/assets").with(user(new PrincipalUser(admin))).with(csrf())
          .contentType(MediaType.APPLICATION_JSON).content(body(Map.of(
            "siteId",foreignSite.getId(),"code","X-1","name","Other asset","type","HVAC"))))
          .andExpect(status().isNotFound());
        mvc.perform(post("/api/assets").with(user(new PrincipalUser(admin))).with(csrf())
          .contentType(MediaType.APPLICATION_JSON).content(body(Map.of(
            "siteId",site.getId(),"code","X-2","name","Own asset","type","ELECTRICAL"))))
          .andExpect(status().isCreated());
    }
    @Test @DisplayName("Preventive maintenance: due job is created once") void scheduledJobIdempotency()throws Exception{
        MaintenancePlan p=new MaintenancePlan();p.setOrganizationId(org.getId());p.setAssetId(asset.getId());
        p.setTitle("Quarterly service");p.setIntervalDays(90);p.setNextDueDate(LocalDate.now().minusDays(1));
        p.setActive(true);plans.save(p);
        var request=post("/api/maintenance-plans/generate-due")
            .with(user(new PrincipalUser(admin))).with(csrf());
        mvc.perform(request).andExpect(status().isOk()).andExpect(jsonPath("$.created").value(1));
        mvc.perform(post("/api/maintenance-plans/generate-due")
            .with(user(new PrincipalUser(admin))).with(csrf()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.created").value(0));
        assertThat(orders.existsByMaintenancePlanIdAndScheduledFor(p.getId(),LocalDate.now().minusDays(1))).isTrue();
    }
    @Test @DisplayName("Work orders: invalid state transitions are rejected") void transitionGuard()throws Exception{
        WorkOrder w=order(WorkStatus.OPEN, null);
        mvc.perform(post("/api/work-orders/"+w.getId()+"/approve")
          .with(user(new PrincipalUser(admin))).with(csrf()))
          .andExpect(status().isUnprocessableEntity());
        mvc.perform(post("/api/work-orders/"+w.getId()+"/start")
          .with(user(new PrincipalUser(tech))).with(csrf()))
          .andExpect(status().isForbidden());
    }
    @Test @DisplayName("Workflow: technician completion followed by manager approval") void lifecycle()throws Exception{
        WorkOrder w=order(WorkStatus.ASSIGNED,tech);
        mvc.perform(post("/api/work-orders/"+w.getId()+"/start")
          .with(user(new PrincipalUser(tech))).with(csrf()))
          .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("IN_PROGRESS"));
        mvc.perform(post("/api/work-orders/"+w.getId()+"/complete")
          .with(user(new PrincipalUser(tech))).with(csrf()).contentType(MediaType.APPLICATION_JSON)
          .content(body(Map.of("resolution","Replaced filter and tested pressure"))))
          .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("COMPLETED"));
        mvc.perform(post("/api/work-orders/"+w.getId()+"/approve")
          .with(user(new PrincipalUser(admin))).with(csrf()))
          .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("APPROVED"));
    }
    @Test @DisplayName("Templates: checklist questions can be published") void checklistCreation()throws Exception{
        mvc.perform(post("/api/inspection-templates").with(user(new PrincipalUser(admin))).with(csrf())
          .contentType(MediaType.APPLICATION_JSON).content(body(Map.of(
          "name","Annual inspection","items",new Object[]{Map.of("label","Visual damage"),Map.of("label","Noise")}
        )))).andExpect(status().isCreated()).andExpect(jsonPath("$.items.length()").value(2));
    }
    @Test @DisplayName("Inspections: failed check creates defect and corrective work order") void failureWorkflow()throws Exception{
        InspectionTemplate t=template();var checklist=items.findAllByTemplateIdOrderByPositionAsc(t.getId());
        var inputs=new Object[]{Map.of("itemId",checklist.get(0).getId(),"passed",true),
          Map.of("itemId",checklist.get(1).getId(),"passed",false,"note","Overheating")};
        mvc.perform(post("/api/inspections").with(user(new PrincipalUser(tech))).with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(body(Map.of("assetId",asset.getId(),"templateId",t.getId(),"results",inputs))))
          .andExpect(status().isCreated()).andExpect(jsonPath("$.inspection.status").value("FAIL"))
          .andExpect(jsonPath("$.defects.length()").value(1));
        assertThat(defects.countByOrganizationIdAndStatus(org.getId(),DefectStatus.OPEN)).isEqualTo(1);
        assertThat(orders.countByOrganizationIdAndStatus(org.getId(),WorkStatus.OPEN)).isGreaterThan(0);
    }
    @Test @DisplayName("Inspections: omitted checklist answers are rejected") void incompleteInspection()throws Exception{
        InspectionTemplate t=template();var first=items.findAllByTemplateIdOrderByPositionAsc(t.getId()).getFirst();
        mvc.perform(post("/api/inspections").with(user(new PrincipalUser(tech))).with(csrf())
          .contentType(MediaType.APPLICATION_JSON).content(body(Map.of(
            "assetId",asset.getId(),"templateId",t.getId(),"results",new Object[]{Map.of("itemId",first.getId(),"passed",true)}))))
          .andExpect(status().isUnprocessableEntity());
    }
    @Test @DisplayName("Defects: manager can resolve a finding") void resolveDefect()throws Exception{
        InspectionTemplate t=template();Inspection i=new Inspection();i.setOrganizationId(org.getId());
        i.setAssetId(asset.getId());i.setTemplateId(t.getId());i.setInspectorUserId(tech.getId());
        i.setPerformedAt(java.time.Instant.now());i.setStatus(InspectionStatus.FAIL);i.setNumber("IN-001");inspections.save(i);
        Defect defect=new Defect();defect.setOrganizationId(org.getId());defect.setAssetId(asset.getId());
        defect.setInspectionId(i.getId());defect.setTitle("Noise");defect.setSeverity(Priority.HIGH);
        defect.setStatus(DefectStatus.OPEN);defects.save(defect);
        mvc.perform(post("/api/defects/"+defect.getId()+"/resolve")
          .with(user(new PrincipalUser(admin))).with(csrf()))
          .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("RESOLVED"));
    }
    @Test @DisplayName("Analytics: dashboard reflects tenant data") void analyticsAreTenantScoped()throws Exception{
        mvc.perform(get("/api/dashboard").with(user(new PrincipalUser(admin))))
          .andExpect(status().isOk()).andExpect(jsonPath("$.sites").value(1))
          .andExpect(jsonPath("$.assets").value(1));
        mvc.perform(get("/api/dashboard").with(user(new PrincipalUser(foreignAdmin))))
          .andExpect(status().isOk()).andExpect(jsonPath("$.sites").value(1))
          .andExpect(jsonPath("$.assets").value(0));
    }
    @Test @DisplayName("Reporting: downloadable work-order PDF") void downloadableReport()throws Exception{
        WorkOrder w=order(WorkStatus.COMPLETED,tech);
        byte[] bytes = mvc.perform(get("/api/work-orders/"+w.getId()+"/report?lang=de")
          .with(user(new PrincipalUser(admin))))
          .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_PDF))
          .andReturn().getResponse().getContentAsByteArray();
        assertThat(new String(bytes,0,Math.min(bytes.length,5),java.nio.charset.StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }
    @Test @DisplayName("Team: only administrators can create accounts") void teamRoleBoundary()throws Exception{
        String payload=body(Map.of("displayName","New tech","email","new-"+UUID.randomUUID()+"@test.example",
          "password","SecurePassword543!","role","TECHNICIAN"));
        mvc.perform(post("/api/team").with(user(new PrincipalUser(tech))).with(csrf())
          .contentType(MediaType.APPLICATION_JSON).content(payload)).andExpect(status().isForbidden());
        mvc.perform(post("/api/team").with(user(new PrincipalUser(admin))).with(csrf())
          .contentType(MediaType.APPLICATION_JSON).content(payload)).andExpect(status().isCreated());
    }
    @Test @DisplayName("Audit: events recorded and visible to managers only") void auditEvents()throws Exception{
        mvc.perform(post("/api/work-orders").with(user(new PrincipalUser(admin))).with(csrf())
          .contentType(MediaType.APPLICATION_JSON).content(body(Map.of(
            "siteId",site.getId(),"title","Urgent service","priority","CRITICAL"))))
          .andExpect(status().isCreated());
        mvc.perform(get("/api/audit-events").with(user(new PrincipalUser(admin))))
          .andExpect(status().isOk()).andExpect(jsonPath("$[0].action").value("CREATED"));
        mvc.perform(get("/api/audit-events").with(user(new PrincipalUser(viewer))))
          .andExpect(status().isForbidden());
    }
}

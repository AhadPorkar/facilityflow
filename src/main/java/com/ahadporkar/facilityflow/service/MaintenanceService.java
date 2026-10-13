package com.ahadporkar.facilityflow.service;

import com.ahadporkar.facilityflow.api.*;
import com.ahadporkar.facilityflow.domain.*;
import com.ahadporkar.facilityflow.domain.Enums.*;
import com.ahadporkar.facilityflow.repository.*;
import com.ahadporkar.facilityflow.security.CurrentUser;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

@Service
public class MaintenanceService {
    private final MaintenancePlanRepository plans;
    private final WorkOrderRepository orders;
    private final AssetRepository assetRepository;
    private final CatalogService catalog;
    private final CurrentUser current;
    private final AuditService audit;
    private final Clock clock;
    public MaintenanceService(MaintenancePlanRepository plans,WorkOrderRepository orders,AssetRepository assetRepository,
                              CatalogService catalog,CurrentUser current,AuditService audit,Clock clock) {
        this.plans=plans;this.orders=orders;this.assetRepository=assetRepository;
        this.catalog=catalog;this.current=current;this.audit=audit;this.clock=clock;
    }
    public MaintenancePlan get(UUID id) { return plans.findByIdAndOrganizationId(id,current.organizationId())
        .orElseThrow(() -> ApiException.notFound("Maintenance plan")); }
    @Transactional(readOnly=true)
    public List<MaintenancePlan> list() {
        return plans.findAllByOrganizationIdOrderByNextDueDateAsc(current.organizationId());
    }
    @Transactional
    public MaintenancePlan save(UUID id,Requests.PlanUpsert r) {
        catalog.asset(r.assetId());
        if (r.assignedToUserId()!=null) catalog.teamMember(r.assignedToUserId());
        MaintenancePlan p=id==null?new MaintenancePlan():get(id);
        if (id!=null && !p.getAssetId().equals(r.assetId()))
            throw ApiException.invalid("Maintenance plan asset cannot be changed");
        p.setOrganizationId(current.organizationId());p.setAssetId(r.assetId());
        p.setTitle(r.title().strip());p.setIntervalDays(r.intervalDays());
        p.setNextDueDate(r.nextDueDate());p.setInstructions(CatalogService.trim(r.instructions()));
        p.setAssignedToUserId(r.assignedToUserId());p.setActive(r.active()==null?true:r.active());
        plans.saveAndFlush(p);audit.record("MAINTENANCE_PLAN",p.getId(),id==null?"CREATED":"UPDATED","Plan updated");
        return p;
    }
    /** Tenant-scoped operator trigger; the regular scheduler processes all tenants. */
    @Transactional
    public int generateForCurrentTenant() { return generate(current.organizationId()); }
    @Scheduled(cron="${app.maintenance-cron:0 0 3 * * *}",zone="UTC")
    @Transactional
    public void generateScheduled() { generate(null); }
    private int generate(UUID organizationFilter) {
        LocalDate now=LocalDate.now(clock); int count=0;
        // Pessimistic locks serialize schedulers; DB unique(plan_id, scheduled_for) is a second guard.
        for (MaintenancePlan plan: plans.lockDuePlans(now)) {
            if (organizationFilter!=null && !plan.getOrganizationId().equals(organizationFilter)) continue;
            LocalDate cycle=plan.getNextDueDate();
            if (!orders.existsByMaintenancePlanIdAndScheduledFor(plan.getId(),cycle)) {
                Asset a=catalogAssetForSystem(plan.getAssetId());
                WorkOrder w=new WorkOrder();w.setOrganizationId(plan.getOrganizationId());
                w.setNumber(WorkOrderService.number());w.setSiteId(a.getSiteId());
                w.setAssetId(a.getId());w.setMaintenancePlanId(plan.getId());w.setScheduledFor(cycle);
                w.setTitle(plan.getTitle());w.setDescription(plan.getInstructions());
                w.setPriority(Priority.MEDIUM);w.setDueDate(cycle);
                w.setAssignedToUserId(plan.getAssignedToUserId());
                w.setStatus(plan.getAssignedToUserId()==null?WorkStatus.OPEN:WorkStatus.ASSIGNED);
                w.setUpdatedAt(Instant.now(clock));orders.save(w);
                audit.record(plan.getOrganizationId(),null,"WORK_ORDER",w.getId(),"AUTO_CREATED","Due maintenance cycle");
                count++;
            }
            // Collapse missed cycles into one corrective backlog item, no burst after downtime.
            do { cycle=cycle.plusDays(plan.getIntervalDays()); } while (!cycle.isAfter(now));
            plan.setNextDueDate(cycle);plans.save(plan);
        }
        return count;
    }
    private Asset catalogAssetForSystem(UUID id) {
        return assetRepository.findById(id).orElseThrow(() -> ApiException.notFound("Asset"));
    }
}

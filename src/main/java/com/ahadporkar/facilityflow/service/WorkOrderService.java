package com.ahadporkar.facilityflow.service;

import com.ahadporkar.facilityflow.api.*;
import com.ahadporkar.facilityflow.domain.*;
import com.ahadporkar.facilityflow.domain.Enums.*;
import com.ahadporkar.facilityflow.repository.*;
import com.ahadporkar.facilityflow.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

@Service
public class WorkOrderService {
    private final WorkOrderRepository orders;
    private final DefectRepository defects;
    private final CurrentUser current;
    private final CatalogService catalog;
    private final AuditService audit;
    private final Clock clock;
    public WorkOrderService(WorkOrderRepository orders,DefectRepository defects,CurrentUser current,
                            CatalogService catalog,AuditService audit,Clock clock) {
        this.orders=orders;this.defects=defects;this.current=current;
        this.catalog=catalog;this.audit=audit;this.clock=clock;
    }
    public static String number() { return "WO-"+UUID.randomUUID().toString().substring(0,8).toUpperCase(Locale.ROOT); }
    public WorkOrder get(UUID id) { return orders.findByIdAndOrganizationId(id,current.organizationId())
        .orElseThrow(() -> ApiException.notFound("Work order")); }
    @Transactional(readOnly=true)
    public List<WorkOrder> list() { return orders.findAllByOrganizationIdOrderByCreatedAtDesc(current.organizationId()); }
    @Transactional
    public WorkOrder create(Requests.WorkOrderCreate r) {
        Site site=catalog.site(r.siteId());
        if (r.assetId()!=null && !catalog.asset(r.assetId()).getSiteId().equals(site.getId()))
            throw ApiException.invalid("Asset must belong to selected site");
        if (r.assignedToUserId()!=null) checkAssignee(r.assignedToUserId());
        WorkOrder w=new WorkOrder();w.setOrganizationId(current.organizationId());
        w.setSiteId(site.getId());w.setAssetId(r.assetId());w.setNumber(number());
        w.setTitle(CatalogService.trim(r.title()));w.setDescription(CatalogService.trim(r.description()));
        w.setPriority(r.priority());w.setDueDate(r.dueDate());
        w.setAssignedToUserId(r.assignedToUserId());
        w.setStatus(r.assignedToUserId()==null?WorkStatus.OPEN:WorkStatus.ASSIGNED);
        w.setCreatedByUserId(current.userId());w.setUpdatedAt(Instant.now(clock));
        orders.saveAndFlush(w);audit.record("WORK_ORDER",w.getId(),"CREATED","Work order created");
        return w;
    }
    public void checkAssignee(UUID userId) {
        UserAccount assignee=catalog.teamMember(userId);
        if (assignee.getRole()!=Role.TECHNICIAN && assignee.getRole()!=Role.MANAGER && assignee.getRole()!=Role.ADMIN)
            throw ApiException.invalid("Assignee must be an active technician or manager");
    }
    @Transactional
    public WorkOrder assign(UUID id,UUID userId) {
        WorkOrder w=get(id);
        if (w.getStatus()!=WorkStatus.OPEN && w.getStatus()!=WorkStatus.ASSIGNED)
            throw ApiException.invalid("Only open or assigned orders can be reassigned");
        checkAssignee(userId);w.setAssignedToUserId(userId);w.setStatus(WorkStatus.ASSIGNED);
        w.setUpdatedAt(Instant.now(clock));orders.save(w);
        audit.record("WORK_ORDER",id,"ASSIGNED","Technician assigned");return w;
    }
    private void canExecute(WorkOrder w) {
        if (current.isManager()) return;
        if (w.getAssignedToUserId()==null || !w.getAssignedToUserId().equals(current.userId()))
            throw ApiException.forbidden();
    }
    @Transactional
    public WorkOrder start(UUID id) {
        WorkOrder w=get(id);canExecute(w);
        if (w.getStatus()!=WorkStatus.ASSIGNED) throw ApiException.invalid("Order must be assigned before start");
        w.setStatus(WorkStatus.IN_PROGRESS);w.setStartedAt(Instant.now(clock));w.setUpdatedAt(Instant.now(clock));
        orders.save(w);audit.record("WORK_ORDER",id,"STARTED","Work started");return w;
    }
    @Transactional
    public WorkOrder complete(UUID id,String resolution) {
        WorkOrder w=get(id);canExecute(w);
        if (w.getStatus()!=WorkStatus.IN_PROGRESS) throw ApiException.invalid("Start work before completing it");
        w.setStatus(WorkStatus.COMPLETED);w.setResolution(resolution.strip());
        w.setCompletedAt(Instant.now(clock));w.setUpdatedAt(Instant.now(clock));
        orders.save(w);audit.record("WORK_ORDER",id,"COMPLETED","Awaiting manager approval");return w;
    }
    @Transactional
    public WorkOrder approve(UUID id) {
        WorkOrder w=get(id);
        if (w.getStatus()!=WorkStatus.COMPLETED) throw ApiException.invalid("Only completed work can be approved");
        w.setStatus(WorkStatus.APPROVED);w.setApprovedAt(Instant.now(clock));w.setUpdatedAt(Instant.now(clock));
        orders.save(w);
        // Approval of corrective work closes its defects in one transaction.
        for (Defect defect: defects.findAllByCorrectiveWorkOrderIdAndStatus(id,DefectStatus.OPEN)) {
            defect.setStatus(DefectStatus.RESOLVED); defect.setResolvedAt(Instant.now(clock));defects.save(defect);
            audit.record("DEFECT",defect.getId(),"RESOLVED","Corrective work approved");
        }
        audit.record("WORK_ORDER",id,"APPROVED","Work accepted by manager");return w;
    }
    @Transactional
    public WorkOrder cancel(UUID id) {
        WorkOrder w=get(id);
        if (w.getStatus()!=WorkStatus.OPEN && w.getStatus()!=WorkStatus.ASSIGNED)
            throw ApiException.invalid("Only open or assigned orders can be cancelled");
        w.setStatus(WorkStatus.CANCELLED);w.setUpdatedAt(Instant.now(clock));orders.save(w);
        audit.record("WORK_ORDER",id,"CANCELLED","Work order cancelled");return w;
    }
    /** Called by inspection workflow; same database transaction, no external side effects. */
    public WorkOrder corrective(Asset asset,String title,UUID creator) {
        WorkOrder w=new WorkOrder();w.setOrganizationId(asset.getOrganizationId());
        w.setSiteId(asset.getSiteId());w.setAssetId(asset.getId());
        w.setNumber(number());w.setTitle(title);w.setPriority(Priority.HIGH);
        w.setStatus(WorkStatus.OPEN);w.setDueDate(LocalDate.now(clock).plusDays(7));
        w.setCreatedByUserId(creator);w.setUpdatedAt(Instant.now(clock));
        orders.save(w);audit.record(asset.getOrganizationId(),creator,"WORK_ORDER",w.getId(),"CREATED","Generated from failed inspection");
        return w;
    }
}

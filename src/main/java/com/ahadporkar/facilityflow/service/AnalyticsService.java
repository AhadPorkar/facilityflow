package com.ahadporkar.facilityflow.service;

import com.ahadporkar.facilityflow.domain.Enums.*;
import com.ahadporkar.facilityflow.repository.*;
import com.ahadporkar.facilityflow.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

@Service
public class AnalyticsService {
    private final SiteRepository sites;private final AssetRepository assets;
    private final MaintenancePlanRepository plans;private final WorkOrderRepository orders;
    private final DefectRepository defects;private final AuditEventRepository audit;
    private final CurrentUser current;private final Clock clock;
    public AnalyticsService(SiteRepository sites,AssetRepository assets,MaintenancePlanRepository plans,
        WorkOrderRepository orders,DefectRepository defects,AuditEventRepository audit,CurrentUser current,Clock clock) {
        this.sites=sites;this.assets=assets;this.plans=plans;this.orders=orders;
        this.defects=defects;this.audit=audit;this.current=current;this.clock=clock;
    }
    @Transactional(readOnly=true)
    public Map<String,Object> overview() {
        UUID org=current.organizationId();
        Map<String,Object> data=new LinkedHashMap<>();
        data.put("sites",sites.countByOrganizationId(org));
        data.put("assets",assets.countByOrganizationId(org));
        data.put("activeAssets",assets.countByOrganizationIdAndStatus(org,AssetStatus.ACTIVE));
        data.put("activePlans",plans.countByOrganizationIdAndActiveTrue(org));
        data.put("openDefects",defects.countByOrganizationIdAndStatus(org,DefectStatus.OPEN));
        data.put("overdue",orders.countByOrganizationIdAndDueDateBeforeAndStatusNotIn(
            org,LocalDate.now(clock),List.of(WorkStatus.APPROVED,WorkStatus.CANCELLED)));
        Map<String,Long> statuses=new LinkedHashMap<>();
        for (WorkStatus status:WorkStatus.values())
            statuses.put(status.name(),orders.countByOrganizationIdAndStatus(org,status));
        data.put("workOrderStatuses",statuses);
        data.put("recentOrders",orders.findAllByOrganizationIdOrderByCreatedAtDesc(org).stream().limit(5).toList());
        data.put("activity",audit.findTop100ByOrganizationIdOrderByCreatedAtDesc(org).stream().limit(6).toList());
        return data;
    }
}

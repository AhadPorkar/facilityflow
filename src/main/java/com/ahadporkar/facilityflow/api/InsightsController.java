package com.ahadporkar.facilityflow.api;

import com.ahadporkar.facilityflow.domain.AuditEvent;
import com.ahadporkar.facilityflow.service.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
public class InsightsController {
    private final AnalyticsService analytics;private final AuditService audit;
    private final PdfReportService reports;
    public InsightsController(AnalyticsService analytics,AuditService audit,PdfReportService reports) {
        this.analytics=analytics;this.audit=audit;this.reports=reports;
    }
    @GetMapping("/api/dashboard") public Map<String,Object> dashboard() { return analytics.overview(); }
    @GetMapping("/api/audit-events")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<AuditEvent> audit() { return audit.recent(); }
    @GetMapping(value="/api/inspections/{id}/report",produces=MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> inspection(@PathVariable UUID id,@RequestParam(defaultValue="en") String lang) {
        return pdf(reports.inspection(id,lang.equalsIgnoreCase("de")),"inspection-"+id+".pdf");
    }
    @GetMapping(value="/api/work-orders/{id}/report",produces=MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> order(@PathVariable UUID id,@RequestParam(defaultValue="en") String lang) {
        return pdf(reports.workOrder(id,lang.equalsIgnoreCase("de")),"work-order-"+id+".pdf");
    }
    private ResponseEntity<byte[]> pdf(byte[] data,String filename) {
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\""+filename+"\"")
            .header("X-Content-Type-Options","nosniff")
            .contentType(MediaType.APPLICATION_PDF).body(data);
    }
}

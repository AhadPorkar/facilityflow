package com.ahadporkar.facilityflow.api;

import com.ahadporkar.facilityflow.domain.*;
import com.ahadporkar.facilityflow.service.InspectionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
public class InspectionController {
    private final InspectionService service;
    public InspectionController(InspectionService service) { this.service=service; }
    @GetMapping("/api/inspection-templates")
    public List<InspectionTemplate> templates() { return service.templates(); }
    @GetMapping("/api/inspection-templates/{id}")
    public InspectionService.TemplateDetail template(@PathVariable UUID id) { return service.template(id); }
    @PostMapping("/api/inspection-templates") @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public InspectionService.TemplateDetail createTemplate(@Valid @RequestBody Requests.TemplateCreate r) {
        return service.createTemplate(r);
    }
    @GetMapping("/api/inspections") public List<Inspection> inspections() { return service.inspections(); }
    @GetMapping("/api/inspections/{id}")
    public InspectionService.InspectionDetail inspection(@PathVariable UUID id) { return service.get(id); }
    @PostMapping("/api/inspections") @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','TECHNICIAN')")
    public InspectionService.InspectionDetail perform(@Valid @RequestBody Requests.InspectionCreate r) {
        return service.perform(r);
    }
    @GetMapping("/api/defects") public List<Defect> defects() { return service.defects(); }
    @PostMapping("/api/defects/{id}/resolve") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public Defect resolve(@PathVariable UUID id) { return service.resolve(id); }
}

package com.ahadporkar.facilityflow.api;

import com.ahadporkar.facilityflow.domain.MaintenancePlan;
import com.ahadporkar.facilityflow.service.MaintenanceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController @RequestMapping("/api/maintenance-plans")
public class MaintenanceController {
    private final MaintenanceService service;
    public MaintenanceController(MaintenanceService service) { this.service=service; }
    @GetMapping public List<MaintenancePlan> list() { return service.list(); }
    @GetMapping("/{id}") public MaintenancePlan get(@PathVariable UUID id) { return service.get(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public MaintenancePlan create(@Valid @RequestBody Requests.PlanUpsert r) { return service.save(null,r); }
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public MaintenancePlan update(@PathVariable UUID id,@Valid @RequestBody Requests.PlanUpsert r) {
        return service.save(id,r);
    }
    @PostMapping("/generate-due") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public Map<String,Integer> generate() { return Map.of("created",service.generateForCurrentTenant()); }
}

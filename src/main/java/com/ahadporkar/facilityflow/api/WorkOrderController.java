package com.ahadporkar.facilityflow.api;

import com.ahadporkar.facilityflow.domain.WorkOrder;
import com.ahadporkar.facilityflow.service.WorkOrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController @RequestMapping("/api/work-orders")
public class WorkOrderController {
    private final WorkOrderService service;
    public WorkOrderController(WorkOrderService service) { this.service=service; }
    @GetMapping public List<WorkOrder> list() { return service.list(); }
    @GetMapping("/{id}") public WorkOrder get(@PathVariable UUID id) { return service.get(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public WorkOrder create(@Valid @RequestBody Requests.WorkOrderCreate r) { return service.create(r); }
    @PostMapping("/{id}/assign") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public WorkOrder assign(@PathVariable UUID id,@Valid @RequestBody Requests.Assign r) {
        return service.assign(id,r.userId());
    }
    @PostMapping("/{id}/start") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','TECHNICIAN')")
    public WorkOrder start(@PathVariable UUID id) { return service.start(id); }
    @PostMapping("/{id}/complete") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','TECHNICIAN')")
    public WorkOrder complete(@PathVariable UUID id,@Valid @RequestBody Requests.Complete r) {
        return service.complete(id,r.resolution());
    }
    @PostMapping("/{id}/approve") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public WorkOrder approve(@PathVariable UUID id) { return service.approve(id); }
    @PostMapping("/{id}/cancel") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public WorkOrder cancel(@PathVariable UUID id) { return service.cancel(id); }
}

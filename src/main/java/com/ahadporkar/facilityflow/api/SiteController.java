package com.ahadporkar.facilityflow.api;

import com.ahadporkar.facilityflow.domain.Site;
import com.ahadporkar.facilityflow.service.CatalogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController @RequestMapping("/api/sites")
public class SiteController {
    private final CatalogService catalog;
    public SiteController(CatalogService catalog) { this.catalog=catalog; }
    @GetMapping public List<Site> list() { return catalog.sites(); }
    @GetMapping("/{id}") public Site get(@PathVariable UUID id) { return catalog.site(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public Site create(@Valid @RequestBody Requests.SiteUpsert request) { return catalog.saveSite(null,request); }
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public Site update(@PathVariable UUID id,@Valid @RequestBody Requests.SiteUpsert request) {
        return catalog.saveSite(id,request);
    }
}

package com.ahadporkar.facilityflow.api;

import com.ahadporkar.facilityflow.domain.Asset;
import com.ahadporkar.facilityflow.service.CatalogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController @RequestMapping("/api/assets")
public class AssetController {
    private final CatalogService catalog;
    public AssetController(CatalogService catalog) { this.catalog=catalog; }
    @GetMapping public List<Asset> list() { return catalog.assets(); }
    @GetMapping("/{id}") public Asset get(@PathVariable UUID id) { return catalog.asset(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public Asset create(@Valid @RequestBody Requests.AssetUpsert request) { return catalog.saveAsset(null,request); }
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public Asset update(@PathVariable UUID id,@Valid @RequestBody Requests.AssetUpsert request) {
        return catalog.saveAsset(id,request);
    }
}

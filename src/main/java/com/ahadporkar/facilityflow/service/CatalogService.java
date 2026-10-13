package com.ahadporkar.facilityflow.service;

import com.ahadporkar.facilityflow.api.*;
import com.ahadporkar.facilityflow.domain.*;
import com.ahadporkar.facilityflow.domain.Enums.*;
import com.ahadporkar.facilityflow.repository.*;
import com.ahadporkar.facilityflow.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class CatalogService {
    private final SiteRepository sites;
    private final AssetRepository assets;
    private final UserAccountRepository users;
    private final CurrentUser current;
    private final AuditService audit;
    public CatalogService(SiteRepository sites,AssetRepository assets,UserAccountRepository users,
                          CurrentUser current,AuditService audit) {
        this.sites=sites;this.assets=assets;this.users=users;this.current=current;this.audit=audit;
    }
    public static String trim(String value) { return value==null?null:value.trim(); }
    public Site site(UUID id) {
        return sites.findByIdAndOrganizationId(id,current.organizationId()).orElseThrow(() -> ApiException.notFound("Site"));
    }
    public Asset asset(UUID id) {
        return assets.findByIdAndOrganizationId(id,current.organizationId()).orElseThrow(() -> ApiException.notFound("Asset"));
    }
    public UserAccount teamMember(UUID id) {
        var user=users.findById(id).orElseThrow(() -> ApiException.notFound("User"));
        if (!user.getOrganizationId().equals(current.organizationId()) || !Boolean.TRUE.equals(user.getEnabled()))
            throw ApiException.notFound("User");
        return user;
    }
    @Transactional(readOnly=true)
    public List<Site> sites() { return sites.findAllByOrganizationIdOrderByNameAsc(current.organizationId()); }
    @Transactional
    public Site saveSite(UUID id,Requests.SiteUpsert r) {
        Site s=id==null?new Site():site(id);
        s.setOrganizationId(current.organizationId()); s.setCode(trim(r.code()).toUpperCase(Locale.ROOT));
        s.setName(trim(r.name())); s.setAddress(trim(r.address())); s.setCity(trim(r.city()));
        s.setPostalCode(trim(r.postalCode())); s.setContactName(trim(r.contactName()));
        s.setContactEmail(trim(r.contactEmail())); s=sites.saveAndFlush(s);
        audit.record("SITE",s.getId(),id==null?"CREATED":"UPDATED","Site details saved");
        return s;
    }
    @Transactional(readOnly=true)
    public List<Asset> assets() { return assets.findAllByOrganizationIdOrderByNameAsc(current.organizationId()); }
    @Transactional
    public Asset saveAsset(UUID id,Requests.AssetUpsert r) {
        site(r.siteId());
        Asset a=id==null?new Asset():asset(id);
        if (id!=null && !a.getSiteId().equals(r.siteId()))
            throw ApiException.invalid("Move an asset via a dedicated audited transfer workflow");
        a.setOrganizationId(current.organizationId()); a.setSiteId(r.siteId());
        a.setCode(trim(r.code()).toUpperCase(Locale.ROOT)); a.setName(trim(r.name()));
        a.setType(r.type()); a.setLocation(trim(r.location())); a.setManufacturer(trim(r.manufacturer()));
        a.setSerialNumber(trim(r.serialNumber())); a.setStatus(r.status()==null?AssetStatus.ACTIVE:r.status());
        a.setInstalledOn(r.installedOn()); a=assets.saveAndFlush(a);
        audit.record("ASSET",a.getId(),id==null?"CREATED":"UPDATED","Equipment register updated");
        return a;
    }
}

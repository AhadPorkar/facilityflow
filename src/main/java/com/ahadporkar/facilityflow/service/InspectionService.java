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
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class InspectionService {
    public record TemplateDetail(InspectionTemplate template,List<ChecklistItem> items) {}
    public record InspectionDetail(Inspection inspection,List<InspectionResult> results,List<Defect> defects) {}
    private final InspectionTemplateRepository templates;
    private final ChecklistItemRepository checklist;
    private final InspectionRepository inspections;
    private final InspectionResultRepository results;
    private final DefectRepository defects;
    private final CatalogService catalog;
    private final WorkOrderService work;
    private final CurrentUser current;
    private final AuditService audit;
    private final Clock clock;
    public InspectionService(InspectionTemplateRepository templates,ChecklistItemRepository checklist,
                             InspectionRepository inspections,InspectionResultRepository results,
                             DefectRepository defects,CatalogService catalog,WorkOrderService work,
                             CurrentUser current,AuditService audit,Clock clock) {
        this.templates=templates;this.checklist=checklist;this.inspections=inspections;
        this.results=results;this.defects=defects;this.catalog=catalog;this.work=work;
        this.current=current;this.audit=audit;this.clock=clock;
    }
    @Transactional(readOnly=true)
    public List<InspectionTemplate> templates() {
        return templates.findAllByOrganizationIdOrderByNameAsc(current.organizationId());
    }
    public TemplateDetail template(UUID id) {
        InspectionTemplate t=templates.findByIdAndOrganizationId(id,current.organizationId())
            .orElseThrow(() -> ApiException.notFound("Inspection template"));
        return new TemplateDetail(t,checklist.findAllByTemplateIdOrderByPositionAsc(t.getId()));
    }
    @Transactional
    public TemplateDetail createTemplate(Requests.TemplateCreate r) {
        if (r.items().size()>30) throw ApiException.invalid("Template limited to 30 checklist items");
        InspectionTemplate t=new InspectionTemplate();t.setOrganizationId(current.organizationId());
        t.setName(r.name().strip());t.setDescription(CatalogService.trim(r.description()));
        t.setAssetType(r.assetType());t.setActive(true);templates.save(t);
        int index=1;
        for (Requests.TemplateItem item:r.items()) {
            ChecklistItem c=new ChecklistItem();c.setOrganizationId(current.organizationId());
            c.setTemplateId(t.getId());c.setPosition(index++);c.setLabel(item.label().strip());
            c.setRequired(item.required()==null || item.required());checklist.save(c);
        }
        audit.record("INSPECTION_TEMPLATE",t.getId(),"CREATED","Checklist published");
        return template(t.getId());
    }
    @Transactional(readOnly=true)
    public List<Inspection> inspections() {
        return inspections.findAllByOrganizationIdOrderByPerformedAtDesc(current.organizationId());
    }
    public InspectionDetail get(UUID id) {
        Inspection i=inspections.findByIdAndOrganizationId(id,current.organizationId())
            .orElseThrow(() -> ApiException.notFound("Inspection"));
        return new InspectionDetail(i,results.findAllByInspectionIdOrderByPositionAsc(i.getId()),
            defects.findAllByInspectionIdOrderByCreatedAtAsc(i.getId()));
    }
    @Transactional
    public InspectionDetail perform(Requests.InspectionCreate r) {
        Asset asset=catalog.asset(r.assetId());TemplateDetail template=template(r.templateId());
        if (!Boolean.TRUE.equals(template.template().getActive())) throw ApiException.invalid("Template is inactive");
        if (template.template().getAssetType()!=null && template.template().getAssetType()!=asset.getType())
            throw ApiException.invalid("Template is not compatible with asset category");
        Map<UUID,Requests.InspectionItem> byId=new HashMap<>();
        for (Requests.InspectionItem item:r.results()) {
            if (byId.put(item.itemId(),item)!=null) throw ApiException.invalid("Duplicate checklist item");
        }
        Set<UUID> expected=template.items().stream().map(ChecklistItem::getId).collect(Collectors.toSet());
        if (!byId.keySet().equals(expected)) throw ApiException.invalid("All checklist items must be submitted exactly once");
        boolean failed=r.results().stream().anyMatch(item -> !item.passed());
        Inspection inspection=new Inspection();inspection.setOrganizationId(current.organizationId());
        inspection.setTemplateId(r.templateId());inspection.setAssetId(asset.getId());
        inspection.setInspectorUserId(current.userId());inspection.setPerformedAt(Instant.now(clock));
        inspection.setStatus(failed?InspectionStatus.FAIL:InspectionStatus.PASS);
        inspection.setNotes(CatalogService.trim(r.notes()));
        inspection.setNumber("IN-"+UUID.randomUUID().toString().substring(0,8).toUpperCase(Locale.ROOT));
        inspections.save(inspection);
        WorkOrder corrective=null;
        if (failed) corrective=work.corrective(asset,"Corrective action - "+asset.getName(),current.userId());
        for (ChecklistItem item:template.items()) {
            Requests.InspectionItem provided=byId.get(item.getId());
            InspectionResult result=new InspectionResult();result.setOrganizationId(current.organizationId());
            result.setInspectionId(inspection.getId());result.setChecklistItemId(item.getId());
            result.setLabelSnapshot(item.getLabel());result.setPosition(item.getPosition());
            result.setPassed(provided.passed());result.setNote(CatalogService.trim(provided.note()));
            results.save(result);
            if (!provided.passed()) {
                Defect defect=new Defect();defect.setOrganizationId(current.organizationId());
                defect.setAssetId(asset.getId());defect.setInspectionId(inspection.getId());
                defect.setCorrectiveWorkOrderId(corrective.getId());defect.setTitle(item.getLabel());
                defect.setDetails(CatalogService.trim(provided.note()));
                defect.setSeverity(Boolean.TRUE.equals(item.getRequired())?Priority.HIGH:Priority.MEDIUM);
                defect.setStatus(DefectStatus.OPEN);defects.save(defect);
                audit.record("DEFECT",defect.getId(),"DISCOVERED","Failed inspection item");
            }
        }
        audit.record("INSPECTION",inspection.getId(),"SUBMITTED",failed?"Inspection failed":"Inspection passed");
        return get(inspection.getId());
    }
    @Transactional(readOnly=true)
    public List<Defect> defects() {
        return defects.findAllByOrganizationIdOrderByCreatedAtDesc(current.organizationId());
    }
    @Transactional
    public Defect resolve(UUID id) {
        Defect d=defects.findByIdAndOrganizationId(id,current.organizationId())
            .orElseThrow(() -> ApiException.notFound("Defect"));
        if (d.getStatus()!=DefectStatus.OPEN) throw ApiException.invalid("Defect already resolved");
        d.setStatus(DefectStatus.RESOLVED);d.setResolvedAt(Instant.now(clock));defects.save(d);
        audit.record("DEFECT",id,"RESOLVED","Manually verified and resolved");return d;
    }
}

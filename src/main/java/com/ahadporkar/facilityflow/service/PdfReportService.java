package com.ahadporkar.facilityflow.service;

import com.ahadporkar.facilityflow.domain.*;
import com.ahadporkar.facilityflow.repository.UserAccountRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Human-readable operational reports, not certified regulatory inspection attestations. */
@Service
public class PdfReportService {
    private final InspectionService inspections;private final WorkOrderService work;
    private final CatalogService catalog;private final UserAccountRepository users;
    public PdfReportService(InspectionService inspections,WorkOrderService work,
                            CatalogService catalog,UserAccountRepository users) {
        this.inspections=inspections;this.work=work;this.catalog=catalog;this.users=users;
    }
    public byte[] inspection(UUID id,boolean german) {
        var detail=inspections.get(id);Inspection i=detail.inspection();
        Asset a=catalog.asset(i.getAssetId());Site site=catalog.site(a.getSiteId());
        String title=german?"Pruefbericht / Inspektion":"Inspection report";
        String who=users.findById(i.getInspectorUserId()).map(UserAccount::getDisplayName).orElse("Unknown");
        List<String> lines=new ArrayList<>();
        lines.add((german?"Bericht-Nr.: ":"Report no.: ")+i.getNumber());
        lines.add((german?"Anlage: ":"Equipment: ")+a.getName()+" ("+a.getCode()+")");
        lines.add((german?"Standort: ":"Site: ")+site.getName()+" - "+site.getCity());
        lines.add((german?"Pruefer: ":"Inspector: ")+who);
        lines.add((german?"Datum: ":"Date: ")+DateTimeFormatter.ISO_LOCAL_DATE
              .format(i.getPerformedAt().atOffset(ZoneOffset.UTC)));
        lines.add((german?"Ergebnis: ":"Outcome: ")+(i.getStatus()==com.ahadporkar.facilityflow.domain.Enums.InspectionStatus.PASS
              ? (german?"BESTANDEN":"PASS"):(german?"NICHT BESTANDEN":"FAIL")));
        lines.add("");lines.add(german?"CHECKLISTE":"CHECKLIST");
        for(InspectionResult r:detail.results()) {
            lines.add((r.getPassed()?"[OK] ":"[FAIL] ")+r.getLabelSnapshot());
            if(r.getNote()!=null && !r.getNote().isBlank()) lines.add("    "+r.getNote());
        }
        lines.add("");lines.add(german?"ANMERKUNGEN":"NOTES");
        lines.add(i.getNotes()==null?"-":i.getNotes());
        lines.add("");lines.add((german?"Erkannte Maengel: ":"Defects identified: ")+detail.defects().size());
        return render(title,lines,german);
    }
    public byte[] workOrder(UUID id,boolean german) {
        WorkOrder w=work.get(id);Site s=catalog.site(w.getSiteId());
        List<String> lines=new ArrayList<>();
        lines.add((german?"Auftrag-Nr.: ":"Order no.: ")+w.getNumber());
        lines.add((german?"Auftrag: ":"Work: ")+w.getTitle());
        lines.add((german?"Standort: ":"Site: ")+s.getName());
        lines.add((german?"Prioritaet: ":"Priority: ")+w.getPriority().name());
        lines.add((german?"Status: ":"Status: ")+w.getStatus().name());
        if (w.getDueDate()!=null) lines.add((german?"Faellig: ":"Due: ")+w.getDueDate());
        lines.add("");lines.add(german?"BESCHREIBUNG":"DESCRIPTION");
        lines.add(w.getDescription()==null?"-":w.getDescription());
        lines.add("");lines.add(german?"ERGEBNIS / DOKUMENTATION":"RESOLUTION / DOCUMENTATION");
        lines.add(w.getResolution()==null?"-":w.getResolution());
        return render(german?"Arbeitsauftragsbericht":"Work order report",lines,german);
    }
    private byte[] render(String title,List<String> lines,boolean german) {
        try(PDDocument doc=new PDDocument();ByteArrayOutputStream bytes=new ByteArrayOutputStream()) {
            var normal=new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            var bold=new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDPage page=null;PDPageContentStream out=null;float y=0;
            for(int item=0;item<lines.size()+1;item++) {
                String line=item==0?title:lines.get(item-1);
                if(out==null || y<88) {
                    if(out!=null)out.close();
                    page=new PDPage(PDRectangle.A4);doc.addPage(page);
                    out=new PDPageContentStream(doc,page);
                    out.setNonStrokingColor(new Color(20,37,66));out.addRect(0,782,595,60);out.fill();
                    out.setNonStrokingColor(Color.WHITE);out.beginText();out.setFont(bold,17);
                    out.newLineAtOffset(42,803);out.showText("FACILITYFLOW");out.endText();
                    y=750;
                    out.setNonStrokingColor(new Color(36,53,81));
                }
                if(item==0) {
                    y=draw(out,bold,15,42,y,line);y-=15;
                } else {
                    for(String wrapped:wrap(line,93)) {
                        boolean heading=wrapped.equals("CHECKLIST")||wrapped.equals("CHECKLISTE")||
                            wrapped.equals("NOTES")||wrapped.equals("ANMERKUNGEN")||
                            wrapped.startsWith("DESCRIPTION")||wrapped.startsWith("BESCHREIBUNG")||
                            wrapped.startsWith("RESOLUTION")||wrapped.startsWith("ERGEBNIS");
                        y=draw(out,heading?bold:normal,heading?11:10,42,y,wrapped);
                    }
                }
            }
            if(out!=null)out.close();
            // Page footer deliberately does not represent an electronic signature.
            doc.save(bytes);return bytes.toByteArray();
        }catch(IOException ex) { throw new IllegalStateException("Could not generate PDF",ex); }
    }
    private float draw(PDPageContentStream out,PDType1Font font,int size,float x,float y,String text)throws IOException {
        if(text.isEmpty())return y-10;
        String safe=text.replace('\n',' ').replace('\r',' ').replace('\t',' ')
           .replaceAll("[^\\x20-\\x7E\\xA0-\\xFF]","?");
        out.beginText();out.setFont(font,size);out.newLineAtOffset(x,y);out.showText(safe);out.endText();
        return y-(size+8);
    }
    private List<String> wrap(String input,int max) {
        if(input==null||input.isBlank())return List.of("");
        List<String> result=new ArrayList<>();StringBuilder line=new StringBuilder();
        for(String word:input.split("\\s+")) {
            if(line.length()+word.length()+1>max && !line.isEmpty()) {
                result.add(line.toString());line.setLength(0);
            }
            if(line.length()>0)line.append(' ');line.append(word);
        }
        if(!line.isEmpty())result.add(line.toString());return result;
    }
}

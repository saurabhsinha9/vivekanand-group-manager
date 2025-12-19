
package com.vivekanand.manager.finance;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import com.vivekanand.manager.events.dto.ParticipantView;
import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;

@Service
public class ReportService {
    public byte[] eventFinanceReport(String eventName, List<FinancialRecord> records, BigDecimal balance) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4);
            PdfWriter.getInstance(doc, baos);
            doc.open();
            doc.add(new Paragraph("Vivekanand Group - Event Finance Report"));
            doc.add(new Paragraph("Event: " + eventName));
            doc.add(new Paragraph(" "));
            Table t = new Table(5);
            t.setWidth(100);
            t.addCell("Type");
            t.addCell("Category");
            t.addCell("Description");
            t.addCell("Amount");
            t.addCell("Timestamp");
            for (var r : records) {
                t.addCell(r.getType().name());
                t.addCell(r.getCategory().name());
                t.addCell(r.getDescription() == null ? "" : r.getDescription());
                t.addCell(r.getAmount().toPlainString());
                t.addCell(r.getTimestamp().toString());
            }
            doc.add(t);
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Balance: " + balance.toPlainString()));
            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    public byte[] membersReport(java.util.List<com.vivekanand.manager.members.Member> members) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4);
            PdfWriter.getInstance(doc, baos);
            doc.open();
            doc.add(new Paragraph("Vivekanand Group - Members Report"));
            doc.add(new Paragraph(" "));
            Table table = new Table(4);
            table.setWidth(100);
            table.addCell("ID");
            table.addCell("Name");
            table.addCell("Phone");
            table.addCell("Email");
            for (var m : members) {
                table.addCell(String.valueOf(m.getId()));
                table.addCell(m.getFullName());
                table.addCell(m.getPhone() == null ? "" : m.getPhone());
                table.addCell(m.getEmail() == null ? "" : m.getEmail());
            }
            doc.add(table);
            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    public byte[] participantsReport(String eventName, java.util.List<com.vivekanand.manager.events.EventParticipant> parts) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4);
            PdfWriter.getInstance(doc, baos);
            doc.open();
            doc.add(new Paragraph("Vivekanand Group - Event Participants Report"));
            doc.add(new Paragraph("Event: " + eventName));
            doc.add(new Paragraph(" "));
            Table table = new Table(2);
            table.setWidth(100);
            table.addCell("Name");
            table.addCell("Role");
            for (var p : parts) { // legacy method kept but shifted meaning
                table.addCell("#" + p.getMemberId()); // fallback if called directly; will be replaced by name-aware variant below
                table.addCell(p.getRole() == null ? "" : p.getRole());
            }
            doc.add(table);
            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    public byte[] participantsNameRoleReport(@NotBlank String eventName, List<ParticipantView> parts) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4);
            PdfWriter.getInstance(doc, baos);
            doc.open();
            doc.add(new Paragraph("Vivekanand Group - Event Participants Report"));
            doc.add(new Paragraph("Event: " + eventName));
            doc.add(new Paragraph(" "));
            Table table = new Table(2);
            table.setWidth(100);
            table.addCell("Name");
            table.addCell("Role");
            for (var p : parts) {
                table.addCell(p.memberName() == null ? "" : p.memberName());
                table.addCell(p.role() == null ? "" : p.role());
            }
            doc.add(table);
            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }
}
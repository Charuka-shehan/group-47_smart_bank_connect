package com.lankatrust.smartbank.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lankatrust.smartbank.entity.Account;
import com.lankatrust.smartbank.entity.AuditLog;
import com.lankatrust.smartbank.entity.Transaction;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfReportService {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ByteArrayInputStream generateTransactionReport(List<Transaction> transactions, String titleText) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Font configurations
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(10, 25, 47));
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);

            // Title
            Paragraph title = new Paragraph(titleText, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Table setup
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2f, 1.5f, 2f, 1.5f, 1.5f, 2f});

            // Headers
            String[] headers = {"Ref Number", "Type", "Amount (LKR)", "Status", "Date", "Remarks"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(new Color(10, 25, 47));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(8);
                table.addCell(cell);
            }

            // Body
            for (Transaction txn : transactions) {
                table.addCell(new PdfPCell(new Phrase(txn.getReferenceNumber(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(txn.getType().toString(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(txn.getAmount().toString(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(txn.getStatus().toString(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(txn.getCreatedAt().format(formatter), cellFont)));
                table.addCell(new PdfPCell(new Phrase(txn.getRemarks() != null ? txn.getRemarks() : "N/A", cellFont)));
            }

            document.add(table);
            document.close();
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    public ByteArrayInputStream generateAccountReport(List<Account> accounts, String titleText) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(10, 25, 47));
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);

            Paragraph title = new Paragraph(titleText, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2.5f, 2.5f, 2f, 2f, 2.5f});

            String[] headers = {"Account Number", "Customer Name", "Type", "Balance (LKR)", "Status"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(new Color(10, 25, 47));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(8);
                table.addCell(cell);
            }

            for (Account acc : accounts) {
                table.addCell(new PdfPCell(new Phrase(acc.getAccountNumber(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(acc.getCustomer().getFullName(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(acc.getAccountType(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(acc.getBalance().toString(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(acc.getStatus().toString(), cellFont)));
            }

            document.add(table);
            document.close();
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    public ByteArrayInputStream generateAuditLogReport(List<AuditLog> logs, String titleText) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(10, 25, 47));
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);

            Paragraph title = new Paragraph(titleText, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2.5f, 2.5f, 4f, 2.5f});

            String[] headers = {"Actor", "Action", "Details", "Timestamp"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(new Color(10, 25, 47));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(8);
                table.addCell(cell);
            }

            for (AuditLog log : logs) {
                table.addCell(new PdfPCell(new Phrase(log.getActorEmail() != null ? log.getActorEmail() : "SYSTEM", cellFont)));
                table.addCell(new PdfPCell(new Phrase(log.getAction(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(log.getDetails() != null ? log.getDetails() : "N/A", cellFont)));
                table.addCell(new PdfPCell(new Phrase(log.getTimestamp() != null ? log.getTimestamp().format(formatter) : "N/A", cellFont)));
            }

            document.add(table);
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    public ByteArrayInputStream generateTableReport(String titleText, String[] headers, java.util.List<String[]> rows) {
        Document document = new Document(PageSize.A4.rotate());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, out);
            document.open();
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(10, 25, 47));
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.DARK_GRAY);

            Paragraph title = new Paragraph(titleText, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(16);
            document.add(title);

            PdfPTable table = new PdfPTable(headers.length);
            table.setWidthPercentage(100);
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(new Color(10, 25, 47));
                cell.setPadding(6);
                table.addCell(cell);
            }
            for (String[] row : rows) {
                for (String value : row) {
                    table.addCell(new PdfPCell(new Phrase(value != null ? value : "", cellFont)));
                }
            }
            document.add(table);
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ByteArrayInputStream(out.toByteArray());
    }
}

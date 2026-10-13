package com.myopty.billing;

import java.io.ByteArrayOutputStream;
import java.util.List;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Paragraph;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public Invoice getInvoiceById(int id) {
        return invoiceRepository.findById(id).orElse(null);
    }

    public byte[] generateInvoicePdf(int invoiceId) {
        Invoice invoice = invoiceRepository
                .findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found with id: " + invoiceId));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(new Paragraph("========================================"));
            document.add(new Paragraph("       / MYOPTY BILLING      "));
            document.add(new Paragraph("========================================"));
            document.add(new Paragraph("Invoice ID   : " + invoice.getInvoiceId()));
            document.add(new Paragraph("Total Amount : LKR " + invoice.getTotalAmount()));
            document.add(new Paragraph("Issue Date   : " + invoice.getIssueDate()));
            document.add(new Paragraph("----------------------------------------"));
            document.add(new Paragraph("       Thank you for your business!     "));
            document.add(new Paragraph("========================================"));

            document.close();
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }
}

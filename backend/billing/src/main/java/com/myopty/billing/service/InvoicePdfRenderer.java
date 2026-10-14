package com.myopty.billing.service;

import com.myopty.billing.exception.InvoiceRenderingException;
import com.myopty.billing.model.Invoice;
import com.myopty.billing.model.Payment;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

/**
 * Renders an invoice as a PDF, on the fly, with no stored file.
 *
 * <p>The alternative — writing a blob and pointing {@code invoice.pdfObjectKey} at
 * it — buys nothing here: the invoice's data is exactly the four money columns, so
 * the file is a rendering of the row, not something that can drift from it. The
 * object key stays null and the row remains the source of truth.
 *
 * <p>OpenPDF is the same library the catalog uses for its report exports, chosen
 * for the same reason: it is the narrow tool for a PDF table. A rendering failure
 * is wrapped in {@link InvoiceRenderingException} so no caller sees a
 * format-specific checked exception.
 */
@Service
public class InvoicePdfRenderer {

    private static final DateTimeFormatter ISSUED_AT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ROOT);

    /** The invoice as a PDF document. */
    public byte[] render(Invoice invoice, Payment payment, String orderNumber, String customerName) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(document, out);
            document.open();
            document.add(header());
            document.add(meta(invoice, payment, orderNumber, customerName));
            document.add(lineItems(invoice));
            document.add(totalBlock(invoice));
            document.close();
        } catch (DocumentException exception) {
            throw new InvoiceRenderingException("Could not render the invoice as a PDF.", exception);
        }
        return out.toByteArray();
    }

    private static Paragraph header() {
        Paragraph header = new Paragraph("MyOpty invoice", bold(18));
        header.setAlignment(Element.ALIGN_LEFT);
        return header;
    }

    private static PdfPTable meta(Invoice invoice, Payment payment, String orderNumber, String customerName)
            throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[] {1f, 2f});
        table.setSpacingBefore(12f);
        addMeta(table, "Invoice number", invoice.getInvoiceNumber());
        addMeta(
                table,
                "Issued",
                invoice.getIssuedAt() == null ? "" : invoice.getIssuedAt().format(ISSUED_AT));
        addMeta(table, "Order", orderNumber == null ? String.valueOf(invoice.getOrderId()) : orderNumber);
        addMeta(table, "Customer", customerName == null ? "" : customerName);
        addMeta(table, "Method", payment == null ? "" : String.valueOf(payment.getMethodId()));
        addMeta(
                table,
                "Transaction",
                payment == null || payment.getGatewayTransactionId() == null ? "" : payment.getGatewayTransactionId());
        return table;
    }

    private static void addMeta(PdfPTable table, String label, String value) {
        table.addCell(cell(label, bold(10)));
        table.addCell(cell(value, regular(10)));
    }

    private static PdfPTable lineItems(Invoice invoice) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[] {3f, 1f});
        table.setSpacingBefore(16f);
        table.addCell(cell("Description", bold(10)));
        table.addCell(cell("Amount", bold(10)));
        addLine(table, "Subtotal", invoice.getSubtotal());
        addLine(table, "Discount", invoice.getDiscountAmount().negate());
        addLine(table, "Tax", invoice.getTaxAmount());
        return table;
    }

    private static void addLine(PdfPTable table, String label, BigDecimal amount) {
        table.addCell(cell(label, regular(10)));
        table.addCell(cell(amount == null ? "" : amount.toPlainString(), regular(10)));
    }

    private static PdfPTable totalBlock(Invoice invoice) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[] {3f, 1f});
        table.setSpacingBefore(6f);
        table.addCell(cell("Total", bold(11)));
        table.addCell(cell(invoice.getTotal() == null ? "" : invoice.getTotal().toPlainString(), bold(11)));
        return table;
    }

    private static PdfPCell cell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorderWidth(0.5f);
        cell.setPadding(6f);
        return cell;
    }

    private static Font bold(int size) {
        return FontFactory.getFont(FontFactory.HELVETICA_BOLD, size);
    }

    private static Font regular(int size) {
        return FontFactory.getFont(FontFactory.HELVETICA, size);
    }
}

package com.myopty.catalog.service;

import com.myopty.catalog.dto.AvailabilityItemResponse;
import com.myopty.catalog.dto.AvailabilityReportResponse;
import com.myopty.catalog.exception.ReportExportException;
import com.myopty.catalog.model.ExportFormat;
import com.myopty.catalog.model.StockStatus;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
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
 * Renders a computed availability report as a downloadable PDF or Excel file.
 *
 * <p>It takes the report as data and turns it into bytes; it does not read the
 * database itself, so the same live view the page shows is what the file holds —
 * both come from {@link InventoryReportService#generate()}. Nothing is stored:
 * the export is a rendering of the request, not a new snapshot row.
 *
 * <p>Two libraries, one method each, chosen because each is the narrow tool for
 * its format: OpenPDF writes the PDF table, Apache POI the {@code .xlsx}
 * workbook. A rendering failure is wrapped in {@link ReportExportException} so
 * the controller never sees a format-specific checked exception.
 */
@Service
public class AvailabilityReportExportService {

    private static final DateTimeFormatter GENERATED_AT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ROOT);

    private static final String[] COLUMNS = {"Type", "Name", "Stock", "Low-stock threshold", "Status"};

    /** The report as a file in {@code format}. */
    public byte[] export(AvailabilityReportResponse report, ExportFormat format) {
        return switch (format) {
            case PDF -> toPdf(report);
            case XLSX -> toXlsx(report);
        };
    }

    private static byte[] toPdf(AvailabilityReportResponse report) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(document, out);
            document.open();
            document.add(new Paragraph("MyOpty availability report", bold(16)));
            document.add(new Paragraph(summaryLine(report), regular(10)));
            document.add(pdfTable(report.items()));
            document.close();
        } catch (DocumentException exception) {
            throw new ReportExportException("Could not render the availability report as a PDF.", exception);
        }
        return out.toByteArray();
    }

    private static PdfPTable pdfTable(List<AvailabilityItemResponse> items) {
        PdfPTable table = new PdfPTable(COLUMNS.length);
        table.setWidthPercentage(100);
        table.setSpacingBefore(12f);
        try {
            table.setWidths(new float[] {1f, 4f, 1f, 1.8f, 1.6f});
        } catch (DocumentException exception) {
            throw new ReportExportException("Could not size the availability report columns.", exception);
        }
        for (String column : COLUMNS) {
            table.addCell(cell(column, bold(10)));
        }
        for (AvailabilityItemResponse item : items) {
            table.addCell(cell(typeLabel(item), regular(10)));
            table.addCell(cell(item.name(), regular(10)));
            table.addCell(cell(String.valueOf(item.stockQty()), regular(10)));
            table.addCell(cell(String.valueOf(item.lowStockThreshold()), regular(10)));
            table.addCell(cell(statusLabel(item.status()), regular(10)));
        }
        return table;
    }

    private static byte[] toXlsx(AvailabilityReportResponse report) {
        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Availability");
            CellStyle header = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            header.setFont(boldFont);

            int rowIndex = 0;
            rowIndex = textRow(sheet, rowIndex, "MyOpty availability report");
            rowIndex = textRow(sheet, rowIndex, summaryLine(report));
            rowIndex++; // a blank spacer row between the header block and the table
            rowIndex = headerRow(sheet, rowIndex, header);
            for (AvailabilityItemResponse item : report.items()) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(typeLabel(item));
                row.createCell(1).setCellValue(item.name());
                row.createCell(2).setCellValue(item.stockQty());
                row.createCell(3).setCellValue(item.lowStockThreshold());
                row.createCell(4).setCellValue(statusLabel(item.status()));
            }
            for (int column = 0; column < COLUMNS.length; column++) {
                sheet.autoSizeColumn(column);
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception exception) {
            throw new ReportExportException(
                    "Could not render the availability report as an Excel workbook.", exception);
        }
    }

    private static int textRow(Sheet sheet, int rowIndex, String text) {
        sheet.createRow(rowIndex).createCell(0).setCellValue(text);
        return rowIndex + 1;
    }

    private static int headerRow(Sheet sheet, int rowIndex, CellStyle style) {
        Row row = sheet.createRow(rowIndex);
        for (int column = 0; column < COLUMNS.length; column++) {
            Cell cell = row.createCell(column);
            cell.setCellValue(COLUMNS[column]);
            cell.setCellStyle(style);
        }
        return rowIndex + 1;
    }

    private static PdfPCell cell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(6f);
        return cell;
    }

    private static Font bold(float size) {
        return FontFactory.getFont(FontFactory.HELVETICA_BOLD, size);
    }

    private static Font regular(float size) {
        return FontFactory.getFont(FontFactory.HELVETICA, size);
    }

    private static String summaryLine(AvailabilityReportResponse report) {
        return "Generated " + LocalDateTime.now().format(GENERATED_AT) + " · Items " + report.totalItems()
                + " · In stock " + report.inStockCount() + " · Low " + report.lowStockCount() + " · Out "
                + report.outOfStockCount();
    }

    private static String typeLabel(AvailabilityItemResponse item) {
        return item.itemType().equals("FRAME") ? "Frame" : "Lens";
    }

    private static String statusLabel(StockStatus status) {
        return switch (status) {
            case IN_STOCK -> "In stock";
            case LOW_STOCK -> "Low stock";
            case OUT_OF_STOCK -> "Out of stock";
        };
    }
}

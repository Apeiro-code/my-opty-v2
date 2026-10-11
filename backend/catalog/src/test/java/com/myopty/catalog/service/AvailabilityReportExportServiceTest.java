package com.myopty.catalog.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.myopty.catalog.dto.AvailabilityItemResponse;
import com.myopty.catalog.dto.AvailabilityReportResponse;
import com.myopty.catalog.model.ExportFormat;
import com.myopty.catalog.model.StockStatus;
import java.io.ByteArrayInputStream;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.parser.PdfTextExtractor;

/**
 * The two file renditions of a report, without a database or a web server: that
 * the PDF is a real PDF, that the Excel file is a real workbook, and that both
 * carry the rows the report held.
 *
 * <p>The assertions are on the bytes and, for the workbook, on cells parsed back
 * out of it — the file has to be openable by the tools the shop actually uses,
 * which "it returned some bytes" would not prove.
 */
class AvailabilityReportExportServiceTest {

    private static final byte[] PDF_MAGIC = {0x25, 0x50, 0x44, 0x46}; // %PDF

    private final AvailabilityReportExportService exporter = new AvailabilityReportExportService();

    private final AvailabilityReportResponse report = new AvailabilityReportResponse(
            2,
            1,
            1,
            0,
            List.of(
                    new AvailabilityItemResponse("FRAME", 1L, "Astra 2100", 2, 5, StockStatus.LOW_STOCK),
                    new AvailabilityItemResponse("LENS", 10L, "Essential 1.50", 30, 5, StockStatus.IN_STOCK)));

    @Test
    void thePdfIsARealPdf() {
        byte[] file = exporter.export(report, ExportFormat.PDF);

        assertThat(file.length).isGreaterThan(0);
        assertThat(file[0]).isEqualTo(PDF_MAGIC[0]);
        assertThat(file[1]).isEqualTo(PDF_MAGIC[1]);
        assertThat(file[2]).isEqualTo(PDF_MAGIC[2]);
        assertThat(file[3]).isEqualTo(PDF_MAGIC[3]);
    }

    @Test
    void thePdfCarriesTheReportAndItsRows() throws Exception {
        byte[] file = exporter.export(report, ExportFormat.PDF);

        // Read the file back with OpenPDF's own parser: the PDF compresses its
        // streams, so a byte scan would prove nothing about what the reader sees.
        PdfReader reader = new PdfReader(file);
        try {
            String text = new PdfTextExtractor(reader).getTextFromPage(1);
            assertThat(text).contains("MyOpty availability report");
            assertThat(text).contains("Astra 2100");
            assertThat(text).contains("Low stock");
            assertThat(text).contains("Essential 1.50");
            assertThat(text).contains("In stock");
        } finally {
            reader.close();
        }
    }

    @Test
    void theXlsxIsAWorkbookWithTheReportRows() throws Exception {
        byte[] file = exporter.export(report, ExportFormat.XLSX);

        // Every .xlsx is a zip archive, so it starts with the local-file header "PK".
        assertThat(file[0]).isEqualTo((byte) 'P');
        assertThat(file[1]).isEqualTo((byte) 'K');

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(file))) {
            Sheet sheet = workbook.getSheetAt(0);
            Row header = sheet.getRow(3);
            assertThat(header.getCell(0).getStringCellValue()).isEqualTo("Type");
            assertThat(header.getCell(1).getStringCellValue()).isEqualTo("Name");

            Row first = sheet.getRow(4);
            assertThat(first.getCell(0).getStringCellValue()).isEqualTo("Frame");
            assertThat(first.getCell(1).getStringCellValue()).isEqualTo("Astra 2100");
            assertThat(first.getCell(2).getNumericCellValue()).isEqualTo(2);
            assertThat(first.getCell(4).getStringCellValue()).isEqualTo("Low stock");

            Row second = sheet.getRow(5);
            assertThat(second.getCell(1).getStringCellValue()).isEqualTo("Essential 1.50");
            assertThat(second.getCell(2).getNumericCellValue()).isEqualTo(30);
            assertThat(second.getCell(4).getStringCellValue()).isEqualTo("In stock");
        }
    }
}

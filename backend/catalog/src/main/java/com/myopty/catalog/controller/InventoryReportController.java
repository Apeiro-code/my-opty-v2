package com.myopty.catalog.controller;

import com.myopty.catalog.dto.ApiResponse;
import com.myopty.catalog.dto.AvailabilityItemResponse;
import com.myopty.catalog.dto.AvailabilityReportResponse;
import com.myopty.catalog.model.ExportFormat;
import com.myopty.catalog.service.AvailabilityReportExportService;
import com.myopty.catalog.service.InventoryReportService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The shop owner's real-time availability report.
 *
 * <p>Mounted under {@code /api/shop/**} so the {@code ROLE_CLIENT} rule the
 * filter chain already applies is the only place the role is checked — a
 * customer never reaches reorder planning. The report is computed on every call
 * rather than cached, so what the client sees is where the shelf is now.
 *
 * <p>{@code /report/export} is the module's one non-JSON response: it streams a
 * PDF or Excel file the browser downloads. The filename is set in the
 * {@code Content-Disposition} header, though the frontend also names the file it
 * saves, because the header is not readable across origins.
 */
@RestController
@RequestMapping("/api/shop/inventory")
public class InventoryReportController {

    private final InventoryReportService service;
    private final AvailabilityReportExportService exporter;

    public InventoryReportController(InventoryReportService service, AvailabilityReportExportService exporter) {
        this.service = service;
        this.exporter = exporter;
    }

    @GetMapping("/report")
    public ApiResponse<AvailabilityReportResponse> report() {
        return ApiResponse.of(service.generate());
    }

    @GetMapping("/alerts")
    public ApiResponse<List<AvailabilityItemResponse>> alerts() {
        return ApiResponse.of(service.alerts());
    }

    /**
     * The report as a downloadable file. An unknown {@code format} is a client
     * mistake and answers 400 through {@link CatalogExceptionHandler}, the same
     * envelope every other bad request uses.
     */
    @GetMapping("/report/export")
    public ResponseEntity<Resource> export(@RequestParam ExportFormat format) {
        byte[] file = exporter.export(service.generate(), format);
        String filename = "availability-report-" + LocalDate.now() + "." + format.extension();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(format.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentLength(file.length)
                .body(new ByteArrayResource(file));
    }
}

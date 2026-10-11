package com.myopty.catalog.controller;

import com.myopty.catalog.dto.ApiResponse;
import com.myopty.catalog.dto.AvailabilityReportResponse;
import com.myopty.catalog.service.InventoryReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The shop owner's real-time availability report.
 *
 * <p>Mounted under {@code /api/shop/**} so the {@code ROLE_CLIENT} rule the
 * filter chain already applies is the only place the role is checked — a
 * customer never reaches reorder planning. The report is computed on every call
 * rather than cached, so what the client sees is where the shelf is now.
 */
@RestController
@RequestMapping("/api/shop/inventory")
public class InventoryReportController {

    private final InventoryReportService service;

    public InventoryReportController(InventoryReportService service) {
        this.service = service;
    }

    @GetMapping("/report")
    public ApiResponse<AvailabilityReportResponse> report() {
        return ApiResponse.of(service.generate());
    }
}

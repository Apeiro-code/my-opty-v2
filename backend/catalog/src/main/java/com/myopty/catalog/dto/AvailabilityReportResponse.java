package com.myopty.catalog.dto;

import java.util.List;

/**
 * The availability report as it stood at the moment it was read, mirroring V6's
 * count columns: how many active frames and lenses there are, and how the shelf
 * divides them into the three health bands.
 *
 * <p>This is a computed live view, not the {@code availability_report} snapshot
 * table: no row is written when it is viewed, because the story is a real-time
 * picture for planning reorders. Persisting and exporting a snapshot belongs to
 * the separate report-export story.
 */
public record AvailabilityReportResponse(
        int totalItems,
        int inStockCount,
        int lowStockCount,
        int outOfStockCount,
        List<AvailabilityItemResponse> items) {}

package com.myopty.catalog.service;

import com.myopty.catalog.dto.AvailabilityItemResponse;
import java.util.List;

/**
 * The seam that turns a low-stock crossing into a message that leaves the
 * building.
 *
 * <p>{@link LowStockAlerts} decides an item crossed into a band the shop must act
 * on; the sender only delivers the list. Today there is one implementation, over
 * SMTP, but a second channel — an in-app push, an SMS to the shop — is a new
 * implementation of this interface, not a second path inside the alerting rule.
 *
 * <p>A sender reports failure by throwing. The caller treats an alert as
 * best-effort and swallows the failure, because a low-stock notice must never
 * take down the sale that triggered it.
 */
public interface LowStockAlertSender {

    /** Delivers one alert covering every item that crossed, to {@code recipientEmail}. */
    void send(List<AvailabilityItemResponse> items, String recipientEmail);
}

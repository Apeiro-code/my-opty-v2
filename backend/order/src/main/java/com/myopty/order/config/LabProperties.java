package com.myopty.order.config;

import com.myopty.order.model.OrderType;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * How long the lab takes, per lens type, and therefore how the estimated receive
 * date is quoted when an order is approved.
 *
 * <p>A progressive lens is surfaced and edged to closer tolerances than a single
 * vision blank, so one lead time for every order would quote the simplest work as
 * slow as the hardest. The defaults are the shop's starting numbers; a deployment
 * overrides them through the {@code LAB_LEAD_DAYS_*} contract without a rebuild,
 * which is the point of making them configuration rather than a constant.
 */
@ConfigurationProperties(prefix = "myopty.lab")
public class LabProperties {

    /** Lead time applied when a type is somehow absent from the map. */
    private static final int FALLBACK_LEAD_DAYS = 5;

    /** Working days from approval to a lens being ready, keyed by lens type. */
    private Map<OrderType, Integer> leadDays =
            new EnumMap<>(Map.of(OrderType.SINGLE_VISION, 3, OrderType.BIFOCAL, 5, OrderType.PROGRESSIVE, 7));

    public Map<OrderType, Integer> getLeadDays() {
        return leadDays;
    }

    public void setLeadDays(Map<OrderType, Integer> leadDays) {
        this.leadDays = leadDays;
    }

    /** The lead time for one order's lens type, falling back rather than failing. */
    public int leadDaysFor(OrderType orderType) {
        return leadDays.getOrDefault(orderType, FALLBACK_LEAD_DAYS);
    }
}

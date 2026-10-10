package com.myopty.order.dto;

import java.time.LocalDate;

/**
 * The shop's correction to an order's estimated receive date.
 *
 * <p>{@code receiveDate} is nullable so the shop can withdraw an estimate that is
 * no longer meaningful — a null means "not quoted", not "today". Approval is what
 * first sets it; this body only ever moves it.
 */
public record ReceiveDateRequest(LocalDate receiveDate) {}

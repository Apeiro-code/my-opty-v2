package com.myopty.contracts.order;

import java.math.BigDecimal;

/**
 * The promotion a customer's item is entitled to, as the consumer needs it: the
 * percentage to take off and the description to label it with. The price itself
 * is not here — the consumer holds the item's price and does its own rounding,
 * so this contract never has to know what a product costs.
 *
 * @param percentage the percent off, greater than zero and at most 100
 * @param description the promotion's name, for the offer label beside the price
 */
public record AppliedDiscount(BigDecimal percentage, String description) {}

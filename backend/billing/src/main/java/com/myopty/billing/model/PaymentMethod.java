package com.myopty.billing.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A way the shop accepts payment, as the checkout offers it: the code the request
 * names, a label to show the customer and where it sits in the list.
 *
 * <p>{@code provider_config} is deliberately unmapped. It is the gateway's JSON
 * credentials, and the local {@code fake} provider needs none; mapping it would
 * both write it back changed on an unrelated save and put a secret on the object
 * the controller serialises. It is read only by the gateway adapter, directly,
 * when a real provider is configured.
 *
 * <p>The database fills {@code created_at} and {@code updated_at} from their
 * column defaults, so no fields exist for them and a save never writes them.
 */
@Table("payment_method")
public class PaymentMethod {

    @Id
    private Long id;

    private String code;
    private String displayName;

    /** Named explicitly: the property is {@code active} but the column is {@code is_active}. */
    @Column("is_active")
    private boolean active;

    private int sortOrder;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}

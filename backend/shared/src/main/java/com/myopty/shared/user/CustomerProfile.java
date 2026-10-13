package com.myopty.shared.user;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * The customer-only half of a profile: the extra fields {@code app_user} does not
 * carry, keyed by the same id.
 *
 * <p>{@code user_id} is the primary key as well as the foreign key, so a profile
 * is not a second identity to keep in step — it is an extension of the one row.
 * A save therefore has an assigned id from the start, which is why
 * {@link CustomerProfileRepository} upserts rather than relying on
 * {@code CrudRepository.save} to choose between INSERT and UPDATE. The table has
 * no {@code created_at} / {@code updated_at}, so no audit fields are mapped.
 */
@Table("customer_profile")
public class CustomerProfile {

    @Id
    private Long userId;

    private String address;
    private PreferredContact preferredContact;

    public CustomerProfile(Long userId, String address, PreferredContact preferredContact) {
        this.userId = userId;
        this.address = address;
        this.preferredContact = preferredContact;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public PreferredContact getPreferredContact() {
        return preferredContact;
    }

    public void setPreferredContact(PreferredContact preferredContact) {
        this.preferredContact = preferredContact;
    }
}

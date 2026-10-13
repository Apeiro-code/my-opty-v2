package com.myopty.shared.user;

import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/**
 * Reads and writes {@code customer_profile}.
 *
 * <p>A narrow marker repository rather than {@code CrudRepository} because the
 * profile story only reads one and writes one, both by user id.
 *
 * <p>{@link #upsert} is a native {@code INSERT ... ON DUPLICATE KEY UPDATE}
 * rather than {@code save}: the id is assigned (it is also the foreign key), so
 * Spring Data JDBC would treat a {@code save} as an UPDATE and never create the
 * missing row. The first profile edit for an account that has never had one would
 * silently change nothing. The single statement is also what keeps a first edit
 * and a later edit the same code path.
 */
public interface CustomerProfileRepository extends Repository<CustomerProfile, Long> {

    Optional<CustomerProfile> findById(Long userId);

    @Modifying
    @Query("""
            INSERT INTO customer_profile (user_id, address, preferred_contact)
            VALUES (:userId, :address, :preferredContact)
            ON DUPLICATE KEY UPDATE
                address = VALUES(address),
                preferred_contact = VALUES(preferred_contact)
            """)
    void upsert(
            @Param("userId") Long userId,
            @Param("address") String address,
            @Param("preferredContact") String preferredContact);
}

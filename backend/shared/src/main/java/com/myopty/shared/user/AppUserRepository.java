package com.myopty.shared.user;

import java.util.Optional;
import org.springframework.data.repository.Repository;

/**
 * Reads {@code app_user}. A marker repository, not {@code CrudRepository}: until
 * registration (Epic 2) lands, nothing in the running system may create or
 * mutate a user, and the narrow interface makes that a compile-time fact rather
 * than a review habit. Registration will widen this deliberately, in its own
 * change, together with whatever verification flow goes with it.
 */
public interface AppUserRepository extends Repository<AppUser, Long> {

    Optional<AppUser> findById(Long id);

    Optional<AppUser> findByEmail(String email);
}

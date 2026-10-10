package com.myopty.order.repository;

import com.myopty.order.model.Prescription;
import org.springframework.data.repository.Repository;

/**
 * Writes {@code prescription}. A narrow marker repository rather than
 * {@code CrudRepository}: the submit story only creates a row, so declaring just
 * {@link #save} keeps reads off the interface until the stories that need them
 * (view, review queue) widen it deliberately.
 *
 * <p>Spring Data JDBC treats a null id as "insert" and returns the instance with
 * the generated key populated, which is where the service gets the new id from.
 */
public interface PrescriptionRepository extends Repository<Prescription, Long> {

    Prescription save(Prescription prescription);
}

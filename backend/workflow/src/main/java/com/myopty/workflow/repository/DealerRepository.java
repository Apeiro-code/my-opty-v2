package com.myopty.workflow.repository;

import com.myopty.workflow.model.Dealer;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DealerRepository extends ListCrudRepository<Dealer, Integer> {

    @Query("SELECT * FROM DEALER WHERE email = :email")
    Optional<Dealer> findByEmail(String email);

    @Query("SELECT * FROM DEALER WHERE active = true")
    List<Dealer> findAllActive();
}
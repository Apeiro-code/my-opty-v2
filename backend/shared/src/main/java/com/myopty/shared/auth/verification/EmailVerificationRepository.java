package com.myopty.shared.auth.verification;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface EmailVerificationRepository extends CrudRepository<EmailVerification, Long> {
    Optional<EmailVerification> findTopByUserIdAndPurposeOrderByIdDesc(Long userId, EmailVerificationPurpose purpose);
}

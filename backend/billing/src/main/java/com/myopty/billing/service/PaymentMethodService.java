package com.myopty.billing.service;

import com.myopty.billing.dto.PaymentMethodResponse;
import com.myopty.billing.exception.InvalidPaymentException;
import com.myopty.billing.model.PaymentMethod;
import com.myopty.billing.repository.PaymentMethodRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The checkout's view of the accepted payment methods.
 *
 * <p>Only active methods are listed, ordered by the {@code sort_order} the seed gives
 * them, so retiring a method is a flag rather than a delete and the checkout follows
 * the shop's intent without a redeploy.
 */
@Service
public class PaymentMethodService {

    private final PaymentMethodRepository methods;

    public PaymentMethodService(PaymentMethodRepository methods) {
        this.methods = methods;
    }

    @Transactional(readOnly = true)
    public List<PaymentMethodResponse> listActive() {
        return methods.findAllByActiveTrueOrderBySortOrderAsc().stream()
                .map(PaymentMethodResponse::from)
                .toList();
    }

    /**
     * The active method with {@code code}, or a bad-request failure.
     *
     * <p>It refuses an inactive method the same way it refuses an unknown one: to the
     * caller, a method the shop has retired is not a choice, and telling the two apart
     * would leak the shop's configuration.
     */
    @Transactional(readOnly = true)
    public PaymentMethod requireActiveByCode(String code) {
        return methods.findByCode(code)
                .filter(PaymentMethod::isActive)
                .orElseThrow(() -> new InvalidPaymentException("Unknown payment method: " + code + "."));
    }
}

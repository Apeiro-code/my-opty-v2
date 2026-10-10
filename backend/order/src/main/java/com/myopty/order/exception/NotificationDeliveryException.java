package com.myopty.order.exception;

/**
 * A notification could not be handed to its sender.
 *
 * <p>Raised only when {@code myopty.notifications.fail-on-error} is on. The order
 * change that caused the notification is already committed by then: this tells the
 * shop that a customer was not told, not that the order did not move. It is
 * therefore a bad-gateway failure of the mail path, not a bad request.
 */
public class NotificationDeliveryException extends RuntimeException {

    public NotificationDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}

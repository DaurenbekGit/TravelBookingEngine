// Owner: Member B
package kz.aitu.travel.booking;

/** Reports an invalid lifecycle operation without changing the booking. */
public final class InvalidBookingStateException extends IllegalStateException {
    private static final long serialVersionUID = 1L;
    public InvalidBookingStateException(String message) { super(message); }
}

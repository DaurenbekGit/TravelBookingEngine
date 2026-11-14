// Owner: Member B
package kz.aitu.travel.booking;

public final class BookingNotFoundException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;
    public BookingNotFoundException(String id) { super("Unknown booking: " + id); }
}

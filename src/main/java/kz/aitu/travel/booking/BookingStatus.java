// Owner: Nurkhat
package kz.aitu.travel.booking;

import java.util.Map;
import java.util.Set;

/** Domain lifecycle values and allowed transitions; ordinary validation, not a pattern. */
public enum BookingStatus {
    PENDING("ОЖИДАЕТ"), CONFIRMED("ПОДТВЕРЖДЕНО"), PAID("ОПЛАЧЕНО"), CANCELLED("ОТМЕНЕНО");
    private static final Map<BookingStatus, Set<BookingStatus>> ALLOWED = Map.of(
            PENDING, Set.of(CONFIRMED, CANCELLED),
            CONFIRMED, Set.of(PAID, CANCELLED),
            PAID, Set.of(CANCELLED),
            CANCELLED, Set.of());
    private final String russianName;
    BookingStatus(String russianName) { this.russianName = russianName; }
    public String russianName() { return russianName; }
    public boolean canTransitionTo(BookingStatus target) { return ALLOWED.get(this).contains(target); }
}

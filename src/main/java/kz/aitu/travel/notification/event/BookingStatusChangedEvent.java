// Owner: Abylay
package kz.aitu.travel.notification.event;

import kz.aitu.travel.booking.BookingStatus;
import kz.aitu.travel.domain.Customer;
import kz.aitu.travel.domain.Money;

/** Observer (Behavioral): typed payload describing a successful lifecycle operation. */
public record BookingStatusChangedEvent(Customer customer, String bookingId,
                                       BookingStatus oldStatus, BookingStatus newStatus, String detail,
                                       Money cancellationFee, Money refund) implements TravelEvent {
    @Override public String message() { return bookingId + ": " + oldStatus + " -> " + newStatus + "; " + detail; }
    @Override public String russianMessage() {
        String text = bookingId + ": " + oldStatus.russianName() + " -> " + newStatus.russianName();
        if (newStatus == BookingStatus.CANCELLED) {
            text += "; сбор за отмену " + cancellationFee + "; возврат " + refund + " (симуляция)";
        }
        return text;
    }
}

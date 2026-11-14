// Owner: Member C
package kz.aitu.travel.notification.event;

import kz.aitu.travel.domain.Customer;

/** Observer (Behavioral): sealed, immutable event contract. */
public sealed interface TravelEvent permits BookingStatusChangedEvent, PriceDropEvent {
    Customer customer();
    String message();
    String russianMessage();
}

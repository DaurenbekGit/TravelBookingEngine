// Owner: Abylay
package kz.aitu.travel.notification.event;

import kz.aitu.travel.domain.Customer;
import kz.aitu.travel.domain.Money;

/** Observer (Behavioral): price drop payload addressed to one watching customer. */
public record PriceDropEvent(Customer customer, String offerId, Money oldPrice, Money newPrice) implements TravelEvent {
    @Override public String russianMessage() { return "Снижение цены " + offerId + ": " + oldPrice + " -> " + newPrice; }
    @Override public String message() { return "Price drop " + offerId + ": " + oldPrice + " -> " + newPrice; }
}

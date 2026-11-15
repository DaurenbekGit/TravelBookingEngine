// Owner: Nurkhat
package kz.aitu.travel.booking.extra;

import kz.aitu.travel.booking.item.BookingItem;
import kz.aitu.travel.domain.Money;

/** Decorator (Structural): ConcreteDecorator adding extra baggage. */
public final class ExtraBaggageDecorator extends BookingItemDecorator {
    public static final Money FEE = Money.of("8000");
    public ExtraBaggageDecorator(BookingItem wrapped) {
        super(wrapped);
        if (wrapped.offer().type() != kz.aitu.travel.domain.TravelType.FLIGHT) {
            throw new IllegalArgumentException("Extra baggage requires FLIGHT");
        }
    }
    @Override public Money price() { return wrapped.price().plus(FEE); }
    @Override public String description() { return wrapped.description() + " + Extra baggage"; }
}

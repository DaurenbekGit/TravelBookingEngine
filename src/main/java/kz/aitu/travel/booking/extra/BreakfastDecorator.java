// Owner: Member B
package kz.aitu.travel.booking.extra;

import kz.aitu.travel.booking.item.BookingItem;
import kz.aitu.travel.domain.Money;

/** Decorator (Structural): ConcreteDecorator adding breakfast. */
public final class BreakfastDecorator extends BookingItemDecorator {
    public static final Money PER_NIGHT = Money.of("5000");
    public BreakfastDecorator(BookingItem wrapped) {
        super(wrapped);
        if (wrapped.offer().type() != kz.aitu.travel.domain.TravelType.HOTEL) {
            throw new IllegalArgumentException("Breakfast requires HOTEL");
        }
    }
    @Override public Money price() { return wrapped.price().plus(PER_NIGHT.times(wrapped.offer().duration().toDays())); }
    @Override public String description() { return wrapped.description() + " + Breakfast"; }
}

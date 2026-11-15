// Owner: Nurkhat
package kz.aitu.travel.booking.extra;

import kz.aitu.travel.booking.item.BookingItem;
import kz.aitu.travel.domain.Money;

/** Decorator (Structural): ConcreteDecorator adding insurance (5% of current subtotal). */
public final class TravelInsuranceDecorator extends BookingItemDecorator {
    public static final int PERCENT = 5;
    public TravelInsuranceDecorator(BookingItem wrapped) {
        super(wrapped);
    }
    @Override public Money price() { return wrapped.price().plus(wrapped.price().percent(PERCENT)); }
    @Override public String description() { return wrapped.description() + " + Insurance (5% of current subtotal)"; }
}

// Owner: Member B
package kz.aitu.travel.booking.extra;

import kz.aitu.travel.booking.item.BookingItem;
import kz.aitu.travel.domain.Money;

/** Decorator (Structural): ConcreteDecorator adding flexible cancellation. */
public final class FlexibleCancellationDecorator extends BookingItemDecorator {
    public static final Money FEE = Money.of("7000");
    public FlexibleCancellationDecorator(BookingItem wrapped) {
        super(wrapped);
    }
    @Override public Money price() { return wrapped.price().plus(FEE); }
    @Override public String description() { return wrapped.description() + " + Flexible cancellation"; }
}

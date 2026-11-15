// Owner: Nurkhat
package kz.aitu.travel.booking.extra;

import java.util.Objects;
import kz.aitu.travel.booking.item.BookingItem;
import kz.aitu.travel.domain.Money;
import kz.aitu.travel.offer.TravelOffer;

/** Decorator (Structural): base wrapper delegates all Component operations. */
public abstract class BookingItemDecorator implements BookingItem {
    protected final BookingItem wrapped;
    protected BookingItemDecorator(BookingItem wrapped) {
        this.wrapped = Objects.requireNonNull(wrapped, "Booking item is required");
    }
    @Override public String description() { return wrapped.description(); }
    @Override public Money price() { return wrapped.price(); }
    @Override public TravelOffer offer() { return wrapped.offer(); }
}

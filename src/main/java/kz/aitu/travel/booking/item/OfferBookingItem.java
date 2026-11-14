// Owner: Member B
package kz.aitu.travel.booking.item;

import java.util.Objects;
import kz.aitu.travel.domain.Money;
import kz.aitu.travel.offer.TravelOffer;

/** Decorator (Structural): ConcreteComponent holding an immutable offer snapshot. */
public record OfferBookingItem(TravelOffer offer) implements BookingItem {
    public OfferBookingItem { Objects.requireNonNull(offer, "Offer is required"); }
    @Override public String description() { return offer.summary(); }
    @Override public Money price() { return offer.price(); }
}

// Owner: Member B
package kz.aitu.travel.booking.item;

import kz.aitu.travel.domain.Money;
import kz.aitu.travel.offer.TravelOffer;

/** Decorator (Structural): Component shared by base items and extras. */
public interface BookingItem {
    String description();
    Money price();
    TravelOffer offer();
}

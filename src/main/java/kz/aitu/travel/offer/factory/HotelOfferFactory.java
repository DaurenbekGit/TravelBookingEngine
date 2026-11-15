// Owner: Daurenbek
package kz.aitu.travel.offer.factory;

import kz.aitu.travel.offer.HotelOffer;
import kz.aitu.travel.offer.OfferSpec;
import kz.aitu.travel.offer.TravelOffer;

/** Factory Method (Creational): ConcreteCreator for HotelOffer. */
public final class HotelOfferFactory extends OfferFactory {
    @Override protected TravelOffer createOffer(OfferSpec spec) {
        OfferSpec.HotelDetails detail = (OfferSpec.HotelDetails) spec.details();
        if (detail.nights() < 1) throw new IllegalArgumentException("Nights must be positive");
        requireText(detail.roomType(), "Room type");
        return new HotelOffer(spec);
    }
}

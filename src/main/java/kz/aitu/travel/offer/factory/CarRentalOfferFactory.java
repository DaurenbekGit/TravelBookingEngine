// Owner: Daurenbek
package kz.aitu.travel.offer.factory;

import kz.aitu.travel.offer.CarRentalOffer;
import kz.aitu.travel.offer.OfferSpec;
import kz.aitu.travel.offer.TravelOffer;

/** Factory Method (Creational): ConcreteCreator for CarRentalOffer. */
public final class CarRentalOfferFactory extends OfferFactory {
    @Override protected TravelOffer createOffer(OfferSpec spec) {
        OfferSpec.CarDetails detail = (OfferSpec.CarDetails) spec.details();
        if (detail.days() < 1) throw new IllegalArgumentException("Rental days must be positive");
        requireText(detail.model(), "Car model");
        return new CarRentalOffer(spec);
    }
}

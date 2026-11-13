// Owner: Member A
package kz.aitu.travel.offer.factory;

import kz.aitu.travel.offer.FlightOffer;
import kz.aitu.travel.offer.OfferSpec;
import kz.aitu.travel.offer.TravelOffer;

/** Factory Method (Creational): ConcreteCreator for FlightOffer. */
public final class FlightOfferFactory extends OfferFactory {
    @Override protected TravelOffer createOffer(OfferSpec spec) {
        OfferSpec.FlightDetails detail = (OfferSpec.FlightDetails) spec.details();
        requireText(detail.flightNumber(), "Flight number");
        java.util.Objects.requireNonNull(detail.origin(), "Origin is required");
        return new FlightOffer(spec);
    }
}

// Owner: Daurenbek
package kz.aitu.travel.offer.visitor;

import kz.aitu.travel.offer.CarRentalOffer;
import kz.aitu.travel.offer.FlightOffer;
import kz.aitu.travel.offer.HotelOffer;

/** Visitor (Behavioral): ConcreteVisitor presenting each type's own details. */
public final class OfferDetailsVisitor implements OfferVisitor<String> {
    @Override public String visitFlight(FlightOffer offer) {
        return offer.summary() + " | Flight number: " + offer.flightNumber()
                + " | Route: " + offer.details().origin() + " -> " + offer.location()
                + " | Departure: " + offer.start() + " | Minutes: " + offer.duration().toMinutes();
    }
    @Override public String visitHotel(HotelOffer offer) {
        return offer.summary() + " | Room: " + offer.details().roomType()
                + " | Nights: " + offer.nights() + " | Check-in: " + offer.start();
    }
    @Override public String visitCarRental(CarRentalOffer offer) {
        return offer.summary() + " | Model: " + offer.details().model()
                + " | Rental days: " + offer.days() + " | Pickup: " + offer.start();
    }
}

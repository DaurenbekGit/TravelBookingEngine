// Owner: Member A
package kz.aitu.travel.offer.visitor;

import kz.aitu.travel.offer.CarRentalOffer;
import kz.aitu.travel.offer.FlightOffer;
import kz.aitu.travel.offer.HotelOffer;

/** Visitor (Behavioral): operation contract for the stable offer hierarchy. */
public interface OfferVisitor<R> {
    R visitFlight(FlightOffer offer);
    R visitHotel(HotelOffer offer);
    R visitCarRental(CarRentalOffer offer);
}

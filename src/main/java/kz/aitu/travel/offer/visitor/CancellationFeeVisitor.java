// Owner: Member A
package kz.aitu.travel.offer.visitor;

import kz.aitu.travel.domain.Money;
import kz.aitu.travel.offer.CarRentalOffer;
import kz.aitu.travel.offer.FlightOffer;
import kz.aitu.travel.offer.HotelOffer;

/**
 * Visitor (Behavioral): ConcreteVisitor computing illustrative cancellation fees.
 * Flights: 10% of base price. Hotels: one night's base price. Cars: fixed 3,000 KZT.
 * Extras do not enter these rules; Booking caps the aggregate fee at its total.
 */
public final class CancellationFeeVisitor implements OfferVisitor<Money> {
    public static final int FLIGHT_FEE_PERCENT = 10;
    public static final Money CAR_FEE = Money.of("3000");
    @Override public Money visitFlight(FlightOffer offer) { return offer.price().percent(FLIGHT_FEE_PERCENT); }
    @Override public Money visitHotel(HotelOffer offer) { return offer.price().dividedBy(offer.nights()); }
    @Override public Money visitCarRental(CarRentalOffer offer) { return CAR_FEE; }
}

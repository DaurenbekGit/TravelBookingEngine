// Owner: Daurenbek
package kz.aitu.travel.offer.visitor;

import kz.aitu.travel.domain.Money;
import kz.aitu.travel.offer.CarRentalOffer;
import kz.aitu.travel.offer.FlightOffer;
import kz.aitu.travel.offer.HotelOffer;

/**
 * Visitor (Behavioral): ConcreteVisitor showing illustrative taxes, not changing totals.
 * Flights: fixed 2,500 KZT airport fee. Hotels: 1,000 KZT per night. Cars: 12% of rental price.
 * These are teaching rules, not legal tax rates.
 */
public final class TaxCalculationVisitor implements OfferVisitor<Money> {
    public static final Money AIRPORT_FEE = Money.of("2500");
    public static final Money TOURIST_TAX_PER_NIGHT = Money.of("1000");
    public static final int RENTAL_VAT_PERCENT = 12;
    @Override public Money visitFlight(FlightOffer offer) { return AIRPORT_FEE; }
    @Override public Money visitHotel(HotelOffer offer) { return TOURIST_TAX_PER_NIGHT.times(offer.nights()); }
    @Override public Money visitCarRental(CarRentalOffer offer) { return offer.price().percent(RENTAL_VAT_PERCENT); }
}

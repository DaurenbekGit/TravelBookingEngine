// Owner: Daurenbek
package kz.aitu.travel.offer;

import java.util.Objects;
import kz.aitu.travel.offer.visitor.OfferVisitor;
import kz.aitu.travel.domain.Money;
import kz.aitu.travel.domain.TravelType;

/** Factory Method (Creational): concrete flight Product; Visitor (Behavioral): ConcreteElement. */
public final class FlightOffer extends TravelOffer {
    public FlightOffer(OfferSpec spec) {
        super(spec);
        Objects.requireNonNull(details(), "Offer details are required");
    }
    public OfferSpec.FlightDetails details() { return (OfferSpec.FlightDetails) spec().details(); }
    public String flightNumber() { return details().flightNumber(); }
    @Override public <R> R accept(OfferVisitor<R> visitor) { return visitor.visitFlight(this); }
    @Override public TravelType type() { return TravelType.FLIGHT; }
    @Override public String summary() { return super.summary() + " | " + flightNumber() + " from " + details().origin(); }
    @Override public TravelOffer withPrice(Money price) {
        return new FlightOffer(spec().withPrice(Objects.requireNonNull(price, "Price is required")));
    }
}

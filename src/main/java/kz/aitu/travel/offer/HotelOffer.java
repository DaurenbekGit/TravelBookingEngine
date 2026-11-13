// Owner: Member A
package kz.aitu.travel.offer;

import java.util.Objects;
import kz.aitu.travel.offer.visitor.OfferVisitor;
import kz.aitu.travel.domain.Money;
import kz.aitu.travel.domain.TravelType;

/** Factory Method (Creational): concrete hotel Product; Visitor (Behavioral): ConcreteElement. */
public final class HotelOffer extends TravelOffer {
    public HotelOffer(OfferSpec spec) {
        super(spec);
        Objects.requireNonNull(details(), "Offer details are required");
    }
    public OfferSpec.HotelDetails details() { return (OfferSpec.HotelDetails) spec().details(); }
    public int nights() { return details().nights(); }
    @Override public <R> R accept(OfferVisitor<R> visitor) { return visitor.visitHotel(this); }
    @Override public TravelType type() { return TravelType.HOTEL; }
    @Override public String summary() { return super.summary() + " | " + nights() + " nights, " + details().roomType(); }
    @Override public TravelOffer withPrice(Money price) {
        return new HotelOffer(spec().withPrice(Objects.requireNonNull(price, "Price is required")));
    }
}

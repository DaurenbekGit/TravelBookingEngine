package kz.aitu.travel.offer;

import java.util.Objects;

import kz.aitu.travel.domain.Money;
import kz.aitu.travel.domain.TravelType;
import kz.aitu.travel.offer.visitor.OfferVisitor;

/** Factory Method (Creational): concrete carrental Product; Visitor (Behavioral): ConcreteElement. */
public final class CarRentalOffer extends TravelOffer {
    public CarRentalOffer(OfferSpec spec) {
        super(spec);
        Objects.requireNonNull(details(), "Offer details are required");
    }
    public OfferSpec.CarDetails details() { return (OfferSpec.CarDetails) spec().details(); }
    public int days() { return details().days(); }
    @Override public <R> R accept(OfferVisitor<R> visitor) { return visitor.visitCarRental(this); }
    @Override public TravelType type() { return TravelType.CAR_RENTAL; }
    @Override public String summary() { return super.summary() + " | " + days() + " days, " + details().model(); }
    @Override public TravelOffer withPrice(Money price) {
        return new CarRentalOffer(spec().withPrice(Objects.requireNonNull(price, "Price is required")));
    }
}

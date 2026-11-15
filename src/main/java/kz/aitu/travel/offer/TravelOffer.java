// Owner: Daurenbek
package kz.aitu.travel.offer;

import kz.aitu.travel.offer.visitor.OfferVisitor;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import kz.aitu.travel.domain.City;
import kz.aitu.travel.domain.Money;
import kz.aitu.travel.domain.TravelType;

/** Factory Method (Creational): abstract Product; Visitor (Behavioral): Element with shared immutable data. */
public abstract class TravelOffer {
    private final OfferSpec spec;
    protected TravelOffer(OfferSpec spec) { this.spec = Objects.requireNonNull(spec); }
    protected final OfferSpec spec() { return spec; }
    public final String id() { return spec.id(); }
    public final String supplierName() { return spec.supplierName(); }
    public final String title() { return spec.title(); }
    public final City location() { return spec.location(); }
    public final Money price() { return spec.price(); }
    public final double rating() { return spec.rating(); }
    public final LocalDateTime start() { return spec.start(); }
    public final Duration duration() { return spec.duration(); }
    public String summary() { return title() + " | " + location().displayName() + " | " + price(); }
    public abstract <R> R accept(OfferVisitor<R> visitor);
    public abstract TravelType type();
    public abstract TravelOffer withPrice(Money price);
}

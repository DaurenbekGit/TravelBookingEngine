// Owner: Member A
package kz.aitu.travel.offer.factory;

import java.util.Objects;
import kz.aitu.travel.offer.OfferSpec;
import kz.aitu.travel.offer.TravelOffer;

/** Factory Method (Creational): Creator validates shared fields before dispatch. */
public abstract class OfferFactory {
    public final TravelOffer create(OfferSpec spec) {
        Objects.requireNonNull(spec, "Offer specification is required");
        requireText(spec.id(), "Offer id");
        requireText(spec.supplierName(), "Supplier");
        requireText(spec.title(), "Title");
        Objects.requireNonNull(spec.location(), "Location is required");
        Objects.requireNonNull(spec.price(), "Price is required");
        Objects.requireNonNull(spec.start(), "Start is required");
        if (!Double.isFinite(spec.rating()) || spec.rating() < 0 || spec.rating() > 5) {
            throw new IllegalArgumentException("Rating must be between 0 and 5");
        }
        if (spec.duration() == null || spec.duration().isNegative() || spec.duration().isZero()) {
            throw new IllegalArgumentException("Duration must be positive");
        }
        Objects.requireNonNull(spec.details(), "Offer details are required");
        try { return createOffer(spec); }
        catch (ClassCastException exception) {
            throw new IllegalArgumentException("Offer details do not match creator", exception);
        }
    }

    protected static void requireText(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
    }
    protected abstract TravelOffer createOffer(OfferSpec spec);
}

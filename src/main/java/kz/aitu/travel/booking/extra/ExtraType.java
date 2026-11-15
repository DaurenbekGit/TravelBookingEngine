// Owner: Nurkhat
package kz.aitu.travel.booking.extra;

import java.util.Set;
import java.util.function.UnaryOperator;
import kz.aitu.travel.booking.item.BookingItem;
import kz.aitu.travel.domain.TravelType;

/** Decorator selection: one enum entry supplies applicability and wrapper construction. */
public enum ExtraType {
    INSURANCE("Insurance (5% of subtotal)", Set.of(TravelType.values()), TravelInsuranceDecorator::new),
    BREAKFAST("Breakfast (5 000 KZT/night)", Set.of(TravelType.HOTEL), BreakfastDecorator::new),
    BAGGAGE("Extra baggage (8 000 KZT)", Set.of(TravelType.FLIGHT), ExtraBaggageDecorator::new),
    FLEXIBLE("Flexible cancellation (7 000 KZT)", Set.of(TravelType.values()), FlexibleCancellationDecorator::new);

    private final String displayName;
    private final Set<TravelType> types;
    private final UnaryOperator<BookingItem> decorate;
    ExtraType(String displayName, Set<TravelType> types, UnaryOperator<BookingItem> decorate) {
        this.displayName = displayName;
        this.types = Set.copyOf(types);
        this.decorate = decorate;
    }
    public String displayName() { return displayName; }
    public boolean appliesTo(TravelType type) { return types.contains(type); }
    public BookingItem apply(BookingItem item) {
        if (!appliesTo(item.offer().type())) {
            throw new IllegalArgumentException(displayName + " is unavailable for " + item.offer().type());
        }
        return decorate.apply(item);
    }
}

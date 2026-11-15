// Owner: Nurkhat
package kz.aitu.travel.booking;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import kz.aitu.travel.booking.item.BookingItem;
import kz.aitu.travel.domain.Customer;
import kz.aitu.travel.domain.Money;
import kz.aitu.travel.offer.visitor.OfferVisitor;

/** Builder (Creational): fluent ConcreteBuilder, validates before creating the Product. */
public final class BookingBuilder {
    private final String id;
    private final Instant createdAt;
    private final OfferVisitor<Money> cancellationFees;
    private final List<BookingItem> items = new ArrayList<>();
    private Customer customer;
    private String note = "";
    public BookingBuilder(String id, Instant createdAt, OfferVisitor<Money> cancellationFees) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Booking id is required");
        this.id = id;
        this.createdAt = Objects.requireNonNull(createdAt, "Creation time is required");
        this.cancellationFees = Objects.requireNonNull(cancellationFees, "Cancellation fee visitor is required");
    }
    public BookingBuilder forCustomer(Customer customer) { this.customer = customer; return this; }
    public BookingBuilder addItem(BookingItem item) {
        items.add(Objects.requireNonNull(item, "Booking item is required"));
        return this;
    }
    public BookingBuilder withNote(String note) {
        this.note = Objects.requireNonNull(note, "Note is required");
        return this;
    }
    public Booking build() {
        if (customer == null) throw new IllegalArgumentException("Customer is required");
        if (items.isEmpty()) throw new IllegalArgumentException("At least one booking item is required");
        var ids = new HashSet<String>();
        for (BookingItem item : items) {
            if (!ids.add(item.offer().id())) throw new IllegalArgumentException("Duplicate offer: " + item.offer().id());
        }
        return new Booking(id, customer, items, createdAt, note, cancellationFees);
    }
}

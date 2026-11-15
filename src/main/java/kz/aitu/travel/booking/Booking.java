// Owner: Nurkhat
package kz.aitu.travel.booking;

import java.time.Instant;
import java.util.List;
import kz.aitu.travel.booking.item.BookingItem;
import kz.aitu.travel.domain.Customer;
import kz.aitu.travel.domain.Money;
import kz.aitu.travel.offer.visitor.OfferVisitor;

/** Builder (Creational): Product with immutable contents and validated lifecycle operations. */
public final class Booking {
    private final String id;
    private final Customer customer;
    private final List<BookingItem> items;
    private final Instant createdAt;
    private final String note;
    private final OfferVisitor<Money> cancellationFees;
    private BookingStatus status = BookingStatus.PENDING;
    private String lastMessage = "Created";
    private Money cancellationFee = Money.ZERO;
    private Money refund = Money.ZERO;

    Booking(String id, Customer customer, List<BookingItem> items, Instant createdAt, String note,
            OfferVisitor<Money> cancellationFees) {
        this.id = id;
        this.customer = customer;
        this.items = List.copyOf(items);
        this.createdAt = createdAt;
        this.note = note;
        this.cancellationFees = cancellationFees;
    }
    public String id() { return id; }
    public Customer customer() { return customer; }
    public List<BookingItem> items() { return items; }
    public Instant createdAt() { return createdAt; }
    public String note() { return note; }
    public synchronized BookingStatus status() { return status; }
    public synchronized String lastMessage() { return lastMessage; }
    public synchronized Money cancellationFee() { return cancellationFee; }
    public synchronized Money refund() { return refund; }
    public Money totalPrice() {
        return items.stream().map(BookingItem::price).reduce(Money.ZERO, Money::plus);
    }
    public synchronized void confirm() {
        validate(BookingStatus.CONFIRMED, "confirm");
        status = BookingStatus.CONFIRMED;
        lastMessage = "Confirmed";
    }
    public synchronized void pay() {
        validate(BookingStatus.PAID, "pay");
        status = BookingStatus.PAID;
        lastMessage = "Payment accepted (simulation)";
    }
    public synchronized void cancel() {
        validate(BookingStatus.CANCELLED, "cancel");
        Money fee = items.stream().map(item -> item.offer().accept(cancellationFees))
                .reduce(Money.ZERO, Money::plus).min(totalPrice());
        Money paidRefund = status == BookingStatus.PAID ? totalPrice().minus(fee) : Money.ZERO;
        cancellationFee = fee;
        refund = paidRefund;
        status = BookingStatus.CANCELLED;
        lastMessage = "Cancelled; cancellation fee " + fee + "; refund " + refund + " (simulation)";
    }
    private void validate(BookingStatus target, String action) {
        if (!status.canTransitionTo(target)) {
            String hint = status == BookingStatus.PENDING && target == BookingStatus.PAID ? ", confirm it first" : "";
            throw new InvalidBookingStateException("Cannot " + action + " booking " + id
                    + ": booking is " + status + hint);
        }
    }
}

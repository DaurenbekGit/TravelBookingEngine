// Owner: Nurkhat
package kz.aitu.travel.booking;

import java.time.Clock;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import kz.aitu.travel.booking.extra.ExtraType;
import kz.aitu.travel.booking.item.BookingItem;
import kz.aitu.travel.booking.item.OfferBookingItem;
import kz.aitu.travel.domain.Customer;
import kz.aitu.travel.domain.Money;
import kz.aitu.travel.offer.visitor.OfferVisitor;
import kz.aitu.travel.notification.EventPublisher;
import kz.aitu.travel.notification.event.BookingStatusChangedEvent;
import kz.aitu.travel.offer.TravelOffer;
import kz.aitu.travel.repository.BookingRepository;

/** Coordinates Builder, Decorator and Visitor-based cancellation; publishes through the Observer interface. */
public final class BookingService {
    private final BookingRepository repository;
    private final EventPublisher events;
    private final Clock clock;
    private final OfferVisitor<Money> cancellationFees;
    public BookingService(BookingRepository repository, EventPublisher events, Clock clock, OfferVisitor<Money> cancellationFees) {
        this.repository = repository;
        this.events = events;
        this.clock = clock;
        this.cancellationFees = cancellationFees;
    }
    public Booking create(Customer customer, List<TravelOffer> offers, Map<String, List<ExtraType>> extras) {
        validateExtraKeys(offers, extras);
        var builder = new BookingBuilder(repository.nextId(), clock.instant(), cancellationFees).forCustomer(customer);
        for (TravelOffer offer : offers) {
            builder.addItem(decorate(offer, extras.getOrDefault(offer.id(), List.of())));
        }
        Booking booking = builder.build();
        repository.save(booking);
        return booking;
    }
    private void validateExtraKeys(List<TravelOffer> offers, Map<String, List<ExtraType>> extras) {
        var offerIds = new HashSet<>(offers.stream().map(TravelOffer::id).toList());
        if (!offerIds.containsAll(extras.keySet())) {
            throw new IllegalArgumentException("Extras refer to an offer outside this booking");
        }
    }
    private BookingItem decorate(TravelOffer offer, List<ExtraType> extras) {
        if (new HashSet<>(extras).size() != extras.size()) {
            throw new IllegalArgumentException("An extra can only be selected once per item");
        }
        BookingItem item = new OfferBookingItem(offer);
        for (ExtraType extra : extras) item = extra.apply(item);
        return item;
    }
    public Booking find(String id) {
        return repository.find(id).orElseThrow(() -> new BookingNotFoundException(id));
    }
    public List<Booking> bookingsOf(Customer customer) {
        return repository.findByCustomer(customer);
    }
    public Booking confirm(String id) { return change(id, Booking::confirm); }
    public Booking pay(String id) { return change(id, Booking::pay); }
    public Booking cancel(String id) { return change(id, Booking::cancel); }
    private Booking change(String id, Consumer<Booking> transition) {
        Booking booking = find(id);
        synchronized (booking) {
            BookingStatus oldStatus = booking.status();
            transition.accept(booking);
            repository.save(booking);
            events.publish(new BookingStatusChangedEvent(booking.customer(), id, oldStatus,
                    booking.status(), booking.lastMessage(), booking.cancellationFee(), booking.refund()));
        }
        return booking;
    }
}

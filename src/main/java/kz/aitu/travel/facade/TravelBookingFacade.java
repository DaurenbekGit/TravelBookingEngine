// Owner: Member C
package kz.aitu.travel.facade;

import java.util.Arrays;
import java.util.function.Consumer;
import kz.aitu.travel.domain.Money;
import kz.aitu.travel.offer.visitor.OfferVisitor;
import java.util.List;
import java.util.Map;
import kz.aitu.travel.booking.Booking;
import kz.aitu.travel.booking.BookingService;
import kz.aitu.travel.booking.extra.ExtraType;
import kz.aitu.travel.domain.Customer;
import kz.aitu.travel.domain.SearchCriteria;
import kz.aitu.travel.notification.EventPublisher;
import kz.aitu.travel.notification.TravelEventListener;
import kz.aitu.travel.notification.event.TravelEvent;
import kz.aitu.travel.notification.listener.AuditLogListener;
import kz.aitu.travel.offer.TravelOffer;
import kz.aitu.travel.pricewatch.PriceWatchService;
import kz.aitu.travel.ranking.RankingType;
import kz.aitu.travel.repository.OfferCatalog;
import kz.aitu.travel.search.OfferSearchService;

/** Facade (Structural): the UI's single entry point to search, bookings and notifications. */
public final class TravelBookingFacade {
    private final OfferSearchService search;
    private final BookingService bookings;
    private final PriceWatchService watches;
    private final OfferCatalog catalog;
    private final EventPublisher events;
    private final AuditLogListener audit;
    private final OfferVisitor<Money> taxes;
    private final OfferVisitor<String> details;
    private final Consumer<TravelEvent> previewChannels;
    public TravelBookingFacade(OfferSearchService search, BookingService bookings, PriceWatchService watches,
                               OfferCatalog catalog, EventPublisher events, AuditLogListener audit,
                               OfferVisitor<Money> taxes, OfferVisitor<String> details,
                               Consumer<TravelEvent> previewChannels) {
        this.search = search;
        this.bookings = bookings;
        this.watches = watches;
        this.catalog = catalog;
        this.events = events;
        this.audit = audit;
        this.taxes = taxes;
        this.details = details;
        this.previewChannels = previewChannels;
    }
    public List<TravelOffer> searchOffers(SearchCriteria criteria, RankingType ranking) { return search.search(criteria, ranking); }
    public Money offerTaxes(String id) { return findOffer(id).accept(taxes); }
    public String offerDetails(String id) { return findOffer(id).accept(details); }
    public void previewNotificationFamilies() {
        var log = audit.events();
        if (!log.isEmpty()) previewChannels.accept(log.get(log.size() - 1));
    }
    public TravelOffer findOffer(String id) { return catalog.find(id); }
    public List<ExtraType> availableExtras(String id) {
        var type = findOffer(id).type();
        return Arrays.stream(ExtraType.values()).filter(extra -> extra.appliesTo(type)).toList();
    }
    public Booking createBooking(Customer customer, List<String> ids, Map<String, List<ExtraType>> extras) {
        return bookings.create(customer, ids.stream().map(catalog::find).toList(), extras);
    }
    public Booking confirmBooking(String id) { return bookings.confirm(id); }
    public Booking payBooking(String id) { return bookings.pay(id); }
    public Booking cancelBooking(String id) { return bookings.cancel(id); }
    public Booking findBooking(String id) { return bookings.find(id); }
    public List<Booking> bookingsOf(Customer customer) { return bookings.bookingsOf(customer); }
    public void watchPrice(Customer customer, String id) { watches.watch(customer, id); }
    public void simulatePriceDrop(String id, int percent) { watches.simulatePriceDrop(id, percent); }
    public List<TravelEvent> notificationLog() { return audit.events(); }
    public void subscribe(TravelEventListener listener) { events.subscribe(listener); }
    public void unsubscribe(TravelEventListener listener) { events.unsubscribe(listener); }
}

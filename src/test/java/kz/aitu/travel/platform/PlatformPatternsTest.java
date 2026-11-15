// Owner: Abylay
package kz.aitu.travel.platform;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import kz.aitu.travel.booking.extra.ExtraType;
import kz.aitu.travel.booking.Booking;
import kz.aitu.travel.booking.BookingBuilder;
import kz.aitu.travel.booking.BookingStatus;
import kz.aitu.travel.booking.BookingNotFoundException;
import kz.aitu.travel.booking.item.OfferBookingItem;
import kz.aitu.travel.offer.visitor.CancellationFeeVisitor;
import kz.aitu.travel.notification.factory.InternationalNotificationFactory;
import kz.aitu.travel.config.AppContext;
import kz.aitu.travel.domain.City;
import kz.aitu.travel.domain.Customer;
import kz.aitu.travel.domain.Money;
import kz.aitu.travel.domain.SearchCriteria;
import kz.aitu.travel.domain.TravelType;
import kz.aitu.travel.notification.NotificationCenter;
import kz.aitu.travel.notification.TravelEventListener;
import kz.aitu.travel.notification.event.PriceDropEvent;
import kz.aitu.travel.notification.event.TravelEvent;
import kz.aitu.travel.notification.listener.AuditLogListener;
import kz.aitu.travel.offer.OfferSpec;
import kz.aitu.travel.offer.TravelOffer;
import kz.aitu.travel.offer.factory.FlightOfferFactory;
import kz.aitu.travel.pricewatch.PriceWatchService;
import kz.aitu.travel.ranking.RankingType;
import kz.aitu.travel.repository.InMemoryBookingRepository;
import kz.aitu.travel.repository.OfferCatalog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class PlatformPatternsTest {
    private final List<String> output = new ArrayList<>();
    private final NotificationCenter center = new NotificationCenter(output::add);
    private final Customer customer = new Customer("c-test", "Traveller", "c@example.test", "789");
    private final OfferCatalog catalog = new OfferCatalog();
    private final PriceWatchService watches = new PriceWatchService(catalog, center);
    private final List<TravelEvent> received = new ArrayList<>();
    private final TravelEvent event = new PriceDropEvent(customer, "f", Money.of("45000"), Money.of("40000"));

    private TravelOffer offer(String id) {
        return new FlightOfferFactory().create(new OfferSpec(id, "SkyJet", "Flight", City.NQZ,
                Money.of("45000"), 4.6, LocalDateTime.of(2026, 12, 20, 8, 0), Duration.ofMinutes(95),
                new OfferSpec.FlightDetails("SJ101", City.ALA)));
    }
    private void setupWatch() {
        catalog.store(List.of(offer("f")));
        center.subscribe(received::add);
        watches.watch(customer, "f");
    }
    @Test void subscribeDeliversEvent() {
        center.subscribe(received::add);
        center.publish(event);
        assertEquals(List.of(event), received);
    }
    @Test void unsubscribeStopsDelivery() {
        TravelEventListener listener = received::add;
        center.subscribe(listener);
        center.unsubscribe(listener);
        center.publish(event);
        assertTrue(received.isEmpty());
    }
    @Test void duplicateSubscriptionDeliversOnce() {
        TravelEventListener listener = received::add;
        center.subscribe(listener);
        center.subscribe(listener);
        center.publish(event);
        assertEquals(1, received.size());
    }
    @Test void listenerFailureDoesNotSuppressLaterListeners() {
        center.subscribe(e -> { throw new IllegalStateException("offline"); });
        center.subscribe(received::add);
        center.publish(event);
        assertEquals(1, received.size());
        assertEquals(List.of("Notification failed: offline"), output);
    }
    @Test void listenerCanUnsubscribeDuringDispatch() {
        TravelEventListener[] listener = new TravelEventListener[1];
        listener[0] = e -> { received.add(e); center.unsubscribe(listener[0]); };
        center.subscribe(listener[0]);
        center.publish(event);
        center.publish(event);
        assertEquals(1, received.size());
    }
    @Test void emailUsesInjectedOutputAndRecipient() {
        new InternationalNotificationFactory().createEmailListener(output::add).onEvent(event);
        assertTrue(output.get(0).startsWith("[EMAIL to c@example.test]"));
        assertTrue(output.get(0).contains("45 000 KZT -> 40 000 KZT"));
    }
    @Test void smsUsesInjectedOutputAndRecipient() {
        new InternationalNotificationFactory().createSmsListener(output::add).onEvent(event);
        assertTrue(output.get(0).startsWith("[SMS to 789]"));
    }
    @Test void auditExposesImmutableSnapshot() {
        var audit = new AuditLogListener();
        audit.onEvent(event);
        var snapshot = audit.events();
        audit.onEvent(event);
        assertEquals(1, snapshot.size());
        assertEquals(2, audit.events().size());
        assertThrows(UnsupportedOperationException.class, snapshot::clear);
    }
    @Test void priceDropUpdatesCatalogAndNotifiesWatcher() {
        setupWatch();
        watches.applyPriceChange("f", Money.of("40000"));
        var drop = assertInstanceOf(PriceDropEvent.class, received.get(0));
        assertEquals(customer, drop.customer());
        assertEquals(Money.of("45000"), drop.oldPrice());
        assertEquals(Money.of("40000"), catalog.find("f").price());
    }
    @ParameterizedTest @ValueSource(strings = {"45000", "50000"})
    void unchangedOrRisingPriceDoesNotNotify(String price) {
        setupWatch();
        watches.applyPriceChange("f", Money.of(price));
        assertTrue(received.isEmpty());
        assertEquals(Money.of(price), catalog.find("f").price());
    }
    @Test void unwatchedOfferDoesNotNotify() {
        setupWatch();
        catalog.store(List.of(offer("other")));
        watches.applyPriceChange("other", Money.of("100"));
        assertTrue(received.isEmpty());
    }
    @Test void repeatedWatchIsDeduplicatedByCustomerId() {
        setupWatch();
        watches.watch(new Customer(customer.id(), "Updated", "new@example.test", "111"), "f");
        watches.simulatePriceDrop("f", 10);
        assertEquals(1, received.size());
        assertEquals("new@example.test", received.get(0).customer().email());
    }
    @Test void eachWatchingCustomerReceivesOneEvent() {
        setupWatch();
        watches.watch(new Customer("other", "Other", "other@example.test", "222"), "f");
        watches.simulatePriceDrop("f", 10);
        assertEquals(2, received.size());
        assertEquals(2, received.stream().map(e -> e.customer().id()).distinct().count());
    }
    @ParameterizedTest @ValueSource(ints = {0, -1, 101}) void invalidDropIsRejected(int percent) {
        setupWatch();
        assertThrows(IllegalArgumentException.class, () -> watches.simulatePriceDrop("f", percent));
        assertEquals(Money.of("45000"), catalog.find("f").price());
    }
    @Test void hundredPercentDropMakesOfferFree() {
        setupWatch();
        watches.simulatePriceDrop("f", 100);
        assertEquals(Money.ZERO, catalog.find("f").price());
        assertEquals(1, received.size());
    }
    @Test void watchingUnknownOfferFailsClearly() {
        assertTrue(assertThrows(IllegalArgumentException.class, () -> watches.watch(customer, "missing"))
                .getMessage().contains("Unknown offer"));
    }
    @Test void facadeCompletesBookingAndPriceWatchJourney() {
        var bytes = new ByteArrayOutputStream();
        var facade = AppContext.createFacade(new PrintStream(bytes));
        var criteria = new SearchCriteria(TravelType.FLIGHT, City.ALA, City.NQZ, LocalDate.of(2026, 12, 20), 1);
        var offer = facade.searchOffers(criteria, RankingType.CHEAPEST).get(0);
        assertTrue(facade.offerDetails(offer.id()).contains("Flight number: SJ101"));
        assertEquals(Money.of("2500"), facade.offerTaxes(offer.id()));
        var booking = facade.createBooking(customer, List.of(offer.id()), Map.of(offer.id(), List.of(ExtraType.BAGGAGE)));
        facade.confirmBooking(booking.id());
        facade.payBooking(booking.id());
        assertEquals(BookingStatus.PAID, facade.findBooking(booking.id()).status());
        assertEquals(Money.of("53000"), booking.totalPrice());
        assertEquals(List.of(booking), facade.bookingsOf(customer));
        facade.watchPrice(customer, offer.id());
        facade.simulatePriceDrop(offer.id(), 10);
        assertEquals(Money.of("40500"), facade.findOffer(offer.id()).price());
        assertEquals(Money.of("53000"), booking.totalPrice());
        assertEquals(3, facade.notificationLog().size());
        assertTrue(bytes.toString().contains("[EMAIL"));
        assertTrue(bytes.toString().contains("[SMS"));
    }
    @Test void facadeContextsOwnIndependentRepositories() {
        var sink = new PrintStream(new ByteArrayOutputStream());
        var first = AppContext.createFacade(sink);
        var second = AppContext.createFacade(sink);
        var offer = first.searchOffers(new SearchCriteria(TravelType.FLIGHT, City.ALA, City.NQZ,
                LocalDate.of(2026, 12, 20), 1), RankingType.CHEAPEST).get(0);
        var booking = first.createBooking(customer, List.of(offer.id()), Map.of());
        assertThrows(BookingNotFoundException.class, () -> second.findBooking(booking.id()));
        assertTrue(second.bookingsOf(customer).isEmpty());
    }
    @Test void moneyRoundsToTiynAndFormatsKzt() {
        assertEquals(Money.of("1.01"), Money.of("1.005"));
        assertEquals("45 000 KZT", Money.of("45000").toString());
        assertEquals(Money.of("47250"), Money.of("45000").plus(Money.of("45000").percent(5)));
        assertThrows(IllegalArgumentException.class, () -> Money.of("-1"));
    }
    @ParameterizedTest @ValueSource(strings = {"NQZ", "Astana", "astana"}) void cityParsingSupportsBothFormats(String city) {
        assertEquals(City.NQZ, City.parse(city));
    }
    @Test void criteriaRejectInvalidLength() {
        assertThrows(IllegalArgumentException.class, () -> new SearchCriteria(TravelType.HOTEL,
                City.ALA, City.NQZ, LocalDate.of(2026, 12, 20), 0));
    }
    @Test void interactiveMenuRecoversFromInvalidInputAndUnknownIds() {
        var bytes = new ByteArrayOutputStream();
        String input = "nonsense\n4\nmissing\n1\n99\n0\n";
        AppContext.run(false, new java.io.ByteArrayInputStream(input.getBytes(java.nio.charset.StandardCharsets.UTF_8)),
                new PrintStream(bytes));
        assertTrue(bytes.toString().contains("Choose a menu number"));
        assertTrue(bytes.toString().contains("Unknown booking: missing"));
        assertTrue(bytes.toString().contains("Invalid Travel type"));
    }
    @Test void interactiveMenuHandlesEofDuringPrompt() {
        var bytes = new ByteArrayOutputStream();
        AppContext.run(false, new java.io.ByteArrayInputStream("1\n".getBytes(java.nio.charset.StandardCharsets.UTF_8)),
                new PrintStream(bytes));
        assertTrue(bytes.toString().contains("Travel type"));
    }

    private Booking booking(InMemoryBookingRepository repository, Customer traveller) {
        return new BookingBuilder(repository.nextId(), java.time.Instant.parse("2026-12-01T00:00:00Z"),
                new CancellationFeeVisitor()).forCustomer(traveller).addItem(new OfferBookingItem(offer("f"))).build();
    }
    @Test void repositorySavesAndFindsById() {
        var repository = new InMemoryBookingRepository();
        Booking booking = booking(repository, customer);
        repository.save(booking);
        assertSame(booking, repository.find(booking.id()).orElseThrow());
        assertEquals(List.of(booking), repository.all());
    }
    @Test void repositoryFindsByCustomerId() {
        var repository = new InMemoryBookingRepository();
        Booking ours = booking(repository, customer);
        repository.save(ours);
        repository.save(booking(repository, new Customer("other", "Other", "o@example.test", "123")));
        var changedContact = new Customer(customer.id(), "Updated", "updated@example.test", "456");
        assertEquals(List.of(ours), repository.findByCustomer(changedContact));
    }
    @Test void repositoryUnknownIdIsEmpty() {
        assertTrue(new InMemoryBookingRepository().find("missing").isEmpty());
    }
    @Test void repositoryIdsAreUniqueWithinEachInstance() {
        var first = new InMemoryBookingRepository();
        var second = new InMemoryBookingRepository();
        String firstId = first.nextId();
        assertNotEquals(firstId, first.nextId());
        assertEquals(firstId, second.nextId());
    }

}

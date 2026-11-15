// Owner: Nurkhat
package kz.aitu.travel.booking;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import kz.aitu.travel.booking.extra.BreakfastDecorator;
import kz.aitu.travel.booking.extra.ExtraBaggageDecorator;
import kz.aitu.travel.booking.extra.ExtraType;
import kz.aitu.travel.booking.extra.FlexibleCancellationDecorator;
import kz.aitu.travel.booking.extra.TravelInsuranceDecorator;
import kz.aitu.travel.booking.item.BookingItem;
import kz.aitu.travel.booking.item.OfferBookingItem;
import kz.aitu.travel.domain.City;
import kz.aitu.travel.domain.Customer;
import kz.aitu.travel.domain.Money;
import kz.aitu.travel.notification.EventPublisher;
import kz.aitu.travel.notification.TravelEventListener;
import kz.aitu.travel.notification.event.BookingStatusChangedEvent;
import kz.aitu.travel.notification.event.TravelEvent;
import kz.aitu.travel.offer.OfferSpec;
import kz.aitu.travel.offer.TravelOffer;
import kz.aitu.travel.offer.factory.FlightOfferFactory;
import kz.aitu.travel.offer.factory.HotelOfferFactory;
import kz.aitu.travel.repository.BookingRepository;
import kz.aitu.travel.offer.visitor.CancellationFeeVisitor;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

class BookingPatternsTest {
    private static final Instant NOW = Instant.parse("2026-12-01T10:00:00Z");
    private final Customer customer = new Customer("b-test", "Traveller", "b@example.test", "123");
    private final FakeRepository repository = new FakeRepository();
    private final RecordingPublisher publisher = new RecordingPublisher();
    private final BookingService service = new BookingService(repository, publisher, Clock.fixed(NOW, ZoneOffset.UTC), new CancellationFeeVisitor());

    private TravelOffer flight(String id) {
        return new FlightOfferFactory().create(new OfferSpec(id, "SkyJet", "Flight", City.NQZ,
                Money.of("45000"), 4.6, LocalDateTime.of(2026, 12, 20, 8, 0), Duration.ofMinutes(95),
                new OfferSpec.FlightDetails("SJ101", City.ALA)));
    }
    private TravelOffer hotel() {
        return new HotelOfferFactory().create(new OfferSpec("hotel", "GlobalStay", "Hotel", City.NQZ,
                Money.of("60000"), 4.5, LocalDateTime.of(2026, 12, 20, 14, 0), Duration.ofDays(2),
                new OfferSpec.HotelDetails(2, "Double")));
    }
    private BookingBuilder builder() { return new BookingBuilder("BK-test", NOW, new CancellationFeeVisitor()).forCustomer(customer); }
    private Booking pending() { return builder().addItem(new OfferBookingItem(flight("flight"))).build(); }
    private Booking create() { return service.create(customer, List.of(flight("flight")), Map.of()); }

    @Test void builderRequiresCustomer() {
        var builder = new BookingBuilder("BK-test", NOW, new CancellationFeeVisitor()).addItem(new OfferBookingItem(flight("f")));
        assertEquals("Customer is required", assertThrows(IllegalArgumentException.class, builder::build).getMessage());
    }
    @Test void builderRequiresItems() {
        assertTrue(assertThrows(IllegalArgumentException.class, () -> builder().build()).getMessage().contains("At least one"));
    }
    @Test void builderRejectsDuplicateOffersIncludingDecoratedItems() {
        var item = new OfferBookingItem(flight("f"));
        var builder = builder().addItem(item).addItem(new TravelInsuranceDecorator(item));
        assertTrue(assertThrows(IllegalArgumentException.class, builder::build).getMessage().contains("Duplicate offer"));
    }
    @Test void builderCreatesImmutableItemSnapshot() {
        var builder = builder().addItem(new OfferBookingItem(flight("first")));
        Booking booking = builder.build();
        builder.addItem(new OfferBookingItem(flight("second")));
        assertEquals(1, booking.items().size());
        assertThrows(UnsupportedOperationException.class, () -> booking.items().clear());
        assertEquals(2, builder.build().items().size());
    }
    @Test void builderRetainsCustomerTimeAndNote() {
        var booking = builder().withNote("Window seat please").addItem(new OfferBookingItem(flight("f"))).build();
        assertEquals(customer, booking.customer());
        assertEquals(NOW, booking.createdAt());
        assertEquals("Window seat please", booking.note());
        assertEquals(BookingStatus.PENDING, booking.status());
    }
    @Test void builderRejectsNullItem() {
        assertThrows(NullPointerException.class, () -> builder().addItem(null));
    }
    @Test void builderRejectsBlankId() {
        assertThrows(IllegalArgumentException.class, () -> new BookingBuilder(" ", NOW, new CancellationFeeVisitor()));
    }
    @Test void baseItemDelegatesPriceAndDescription() {
        var offer = flight("f");
        var item = new OfferBookingItem(offer);
        assertEquals(Money.of("45000"), item.price());
        assertEquals(offer.summary(), item.description());
        assertSame(offer, item.offer());
    }
    @Test void insuranceAddsFivePercent() {
        var item = new TravelInsuranceDecorator(new OfferBookingItem(flight("f")));
        assertEquals(Money.of("47250"), item.price());
        assertTrue(item.description().contains("Insurance"));
    }
    @Test void breakfastChargesPerNight() {
        var item = new BreakfastDecorator(new OfferBookingItem(hotel()));
        assertEquals(Money.of("70000"), item.price());
        assertTrue(item.description().contains("Breakfast"));
    }
    @Test void baggageAddsFixedFee() {
        var item = new ExtraBaggageDecorator(new OfferBookingItem(flight("f")));
        assertEquals(Money.of("53000"), item.price());
    }
    @Test void flexibilityAddsFixedFee() {
        var item = new FlexibleCancellationDecorator(new OfferBookingItem(flight("f")));
        assertEquals(Money.of("52000"), item.price());
    }
    @Test void stackUsesCurrentSubtotalAndPreservesOffer() {
        var offer = flight("f");
        BookingItem item = new OfferBookingItem(offer);
        item = new ExtraBaggageDecorator(item);
        item = new TravelInsuranceDecorator(item);
        item = new FlexibleCancellationDecorator(item);
        assertEquals(Money.of("62650"), item.price());
        assertSame(offer, item.offer());
        assertTrue(item.description().contains("Extra baggage"));
        assertTrue(item.description().contains("Insurance"));
        assertTrue(item.description().contains("Flexible cancellation"));
    }
    @Test void decoratorOrderHasDocumentedPriceEffect() {
        var base = new OfferBookingItem(flight("f"));
        var insuranceLast = new TravelInsuranceDecorator(new FlexibleCancellationDecorator(base));
        var flexibleLast = new FlexibleCancellationDecorator(new TravelInsuranceDecorator(base));
        assertEquals(Money.of("54600"), insuranceLast.price());
        assertEquals(Money.of("54250"), flexibleLast.price());
    }
    @Test void directDecoratorsRejectWrongTravelType() {
        assertThrows(IllegalArgumentException.class, () -> new BreakfastDecorator(new OfferBookingItem(flight("f"))));
        assertThrows(IllegalArgumentException.class, () -> new ExtraBaggageDecorator(new OfferBookingItem(hotel())));
    }
    @Test void extraEnumAppliesAndValidatesWithoutTypeSwitch() {
        var flight = new OfferBookingItem(flight("f"));
        assertInstanceOf(ExtraBaggageDecorator.class, ExtraType.BAGGAGE.apply(flight));
        assertThrows(IllegalArgumentException.class, () -> ExtraType.BREAKFAST.apply(flight));
    }
    @Test void multiItemTotalIncludesAllDecorations() {
        var booking = builder().addItem(new TravelInsuranceDecorator(new OfferBookingItem(flight("f"))))
                .addItem(new BreakfastDecorator(new OfferBookingItem(hotel()))).build();
        assertEquals(Money.of("117250"), booking.totalPrice());
    }
    @ParameterizedTest
    @CsvSource({"PENDING,confirm,CONFIRMED", "PENDING,cancel,CANCELLED", "CONFIRMED,pay,PAID",
            "CONFIRMED,cancel,CANCELLED", "PAID,cancel,CANCELLED"})
    void allValidTransitions(String initial, String action, String expected) {
        Booking booking = withStatus(initial);
        action(action).accept(booking);
        assertEquals(BookingStatus.valueOf(expected), booking.status());
    }
    @ParameterizedTest
    @CsvSource({"PENDING,pay", "CONFIRMED,confirm", "PAID,confirm", "PAID,pay",
            "CANCELLED,confirm", "CANCELLED,pay", "CANCELLED,cancel"})
    void allInvalidTransitionsPreserveStatus(String initial, String action) {
        Booking booking = withStatus(initial);
        var exception = assertThrows(InvalidBookingStateException.class, () -> action(action).accept(booking));
        assertTrue(exception.getMessage().contains(initial));
        assertTrue(exception.getMessage().contains(booking.id()));
        assertEquals(BookingStatus.valueOf(initial), booking.status());
    }
    @Test void paidCancellationExplainsRefund() {
        Booking booking = withStatus("PAID");
        booking.cancel();
        assertTrue(booking.lastMessage().contains("refund 40 500 KZT"));
    }
    @ParameterizedTest @EnumSource(BookingStatus.class)
    void statusRejectsSelfTransitions(BookingStatus status) {
        assertFalse(status.canTransitionTo(status));
    }
    @Test void serviceSavesCreatedBookingUsingInjectedClock() {
        Booking booking = create();
        assertSame(booking, repository.find(booking.id()).orElseThrow());
        assertEquals(NOW, booking.createdAt());
        assertEquals(1, repository.saves);
        assertTrue(publisher.events.isEmpty());
    }
    @Test void servicePublishesOldAndNewStatesAfterSaving() {
        Booking booking = create();
        service.confirm(booking.id());
        var event = assertInstanceOf(BookingStatusChangedEvent.class, publisher.events.get(0));
        assertEquals(BookingStatus.PENDING, event.oldStatus());
        assertEquals(BookingStatus.CONFIRMED, event.newStatus());
        assertEquals(customer, event.customer());
        assertEquals(2, repository.saves);
    }
    @Test void invalidServiceTransitionDoesNotSaveOrPublish() {
        Booking booking = create();
        assertThrows(InvalidBookingStateException.class, () -> service.pay(booking.id()));
        assertEquals(1, repository.saves);
        assertTrue(publisher.events.isEmpty());
    }
    @Test void serviceUnknownIdHasClearException() {
        assertEquals("Unknown booking: missing", assertThrows(BookingNotFoundException.class,
                () -> service.find("missing")).getMessage());
    }
    @Test void serviceDecoratesBeforeSaving() {
        Booking booking = service.create(customer, List.of(flight("f")), Map.of("f", List.of(ExtraType.INSURANCE)));
        assertEquals(Money.of("47250"), booking.totalPrice());
    }
    @Test void serviceRejectsRepeatedExtrasWithoutSaving() {
        assertThrows(IllegalArgumentException.class, () -> service.create(customer, List.of(flight("f")),
                Map.of("f", List.of(ExtraType.INSURANCE, ExtraType.INSURANCE))));
        assertTrue(repository.all().isEmpty());
    }
    @Test void serviceRejectsExtrasForUnselectedOffer() {
        assertThrows(IllegalArgumentException.class, () -> service.create(customer, List.of(flight("f")),
                Map.of("other", List.of(ExtraType.BAGGAGE))));
        assertEquals(0, repository.saves);
    }
    @Test void serviceFiltersBookingsByStableCustomerId() {
        Booking ours = create();
        var other = new Customer("other", "Other", "other@example.test", "456");
        service.create(other, List.of(flight("f")), Map.of());
        var changedContact = new Customer(customer.id(), "New Name", "new@example.test", "789");
        assertEquals(List.of(ours), service.bookingsOf(changedContact));
    }
    private Booking withStatus(String status) {
        Booking booking = pending();
        Map<String, Consumer<Booking>> preparations = Map.of("PENDING", b -> { }, "CONFIRMED", Booking::confirm,
                "PAID", b -> { b.confirm(); b.pay(); }, "CANCELLED", Booking::cancel);
        preparations.get(status).accept(booking);
        return booking;
    }
    private Consumer<Booking> action(String name) {
        return Map.<String, Consumer<Booking>>of("confirm", Booking::confirm, "pay", Booking::pay,
                "cancel", Booking::cancel).get(name);
    }
    /** Private per-test fake records saves without sharing storage. */
    private static final class FakeRepository implements BookingRepository {
        private final Map<String, Booking> data = new LinkedHashMap<>();
        private int sequence;
        private int saves;
        @Override public String nextId() { return "FAKE-" + ++sequence; }
        @Override public void save(Booking booking) { data.put(booking.id(), booking); saves++; }
        @Override public Optional<Booking> find(String id) { return Optional.ofNullable(data.get(id)); }
        @Override public List<Booking> all() { return List.copyOf(data.values()); }
    }
    private static final class RecordingPublisher implements EventPublisher {
        private final List<TravelEvent> events = new ArrayList<>();
        @Override public void subscribe(TravelEventListener listener) { }
        @Override public void unsubscribe(TravelEventListener listener) { }
        @Override public void publish(TravelEvent event) { events.add(event); }
    }
    @Test void cancellationVisitorFeesAndRefundAppearInNotification() {
        Booking booking = service.create(customer, List.of(flight("f"), hotel()),
                Map.of("f", List.of(ExtraType.BAGGAGE)));
        service.confirm(booking.id());
        service.pay(booking.id());
        service.cancel(booking.id());
        assertEquals(Money.of("113000"), booking.totalPrice());
        assertEquals(Money.of("34500"), booking.cancellationFee());
        assertEquals(Money.of("78500"), booking.refund());
        var event = assertInstanceOf(BookingStatusChangedEvent.class, publisher.events.get(2));
        assertEquals(BookingStatus.CANCELLED, event.newStatus());
        assertEquals(booking.cancellationFee(), event.cancellationFee());
        assertEquals(booking.refund(), event.refund());
        assertTrue(event.message().contains("cancellation fee 34 500 KZT; refund 78 500 KZT"));
    }
    @Test void unpaidCancellationQuotesFeeButDoesNotRefundUnpaidMoney() {
        Booking booking = pending();
        booking.cancel();
        assertEquals(Money.of("4500"), booking.cancellationFee());
        assertEquals(Money.ZERO, booking.refund());
    }
    @Test void cancellationFeeCannotExceedBookingTotal() {
        var offer = new kz.aitu.travel.offer.factory.CarRentalOfferFactory().create(new OfferSpec(
                "cheap-car", "DriveNow", "Car", City.NQZ, Money.of("1000"), 4.5,
                LocalDateTime.of(2026, 12, 20, 9, 0), Duration.ofDays(1),
                new OfferSpec.CarDetails(1, "Corolla")));
        Booking booking = builder().addItem(new OfferBookingItem(offer)).build();
        booking.confirm();
        booking.pay();
        booking.cancel();
        assertEquals(Money.of("1000"), booking.cancellationFee());
        assertEquals(Money.ZERO, booking.refund());
    }

@Test void cancellationDoesNotChangeDecoratedTotal() {
        Booking booking = service.create(customer, List.of(flight("f")),
                Map.of("f", List.of(ExtraType.BAGGAGE, ExtraType.INSURANCE, ExtraType.FLEXIBLE)));
        Money totalBeforeCancellation = booking.totalPrice();
        service.confirm(booking.id());
        service.pay(booking.id());
        service.cancel(booking.id());
        assertEquals(Money.of("62650"), totalBeforeCancellation);
        assertEquals(totalBeforeCancellation, booking.totalPrice());
        assertEquals(Money.of("58150"), booking.refund());
    }
    @Test void repeatedCancellationDoesNotRepeatRefundOrPublish() {
        Booking booking = create();
        service.confirm(booking.id());
        service.pay(booking.id());
        service.cancel(booking.id());
        int savedBeforeRetry = repository.saves;
        int eventsBeforeRetry = publisher.events.size();
        assertThrows(InvalidBookingStateException.class, () -> service.cancel(booking.id()));
        assertEquals(savedBeforeRetry, repository.saves);
        assertEquals(eventsBeforeRetry, publisher.events.size());
        assertEquals(Money.of("40500"), booking.refund());
    }
    @Test void confirmedCancellationAlsoHasNoPaidRefund() {
        Booking booking = create();
        service.confirm(booking.id());
        service.cancel(booking.id());
        assertEquals(BookingStatus.CANCELLED, booking.status());
        assertEquals(Money.of("4500"), booking.cancellationFee());
        assertEquals(Money.ZERO, booking.refund());
        assertTrue(booking.lastMessage().contains("refund 0 KZT"));
    }
    @Test void cancellationUsesTheBookedOfferSnapshot() {
        TravelOffer original = flight("f");
        Booking booking = service.create(customer, List.of(original), Map.of());
        TravelOffer repriced = original.withPrice(Money.of("10000"));
        service.confirm(booking.id());
        service.pay(booking.id());
        service.cancel(booking.id());
        assertEquals(Money.of("10000"), repriced.price());
        assertEquals(Money.of("4500"), booking.cancellationFee());
        assertEquals(Money.of("40500"), booking.refund());
    }
    @Test void builderRequiresAnInjectedCancellationVisitor() {
        var error = assertThrows(NullPointerException.class,
                () -> new BookingBuilder("BK-missing-visitor", NOW, null));
        assertEquals("Cancellation fee visitor is required", error.getMessage());
    }
    @Test void failedFeeCalculationLeavesBookingUnchanged() {
        kz.aitu.travel.offer.visitor.OfferVisitor<Money> failing = new kz.aitu.travel.offer.visitor.OfferVisitor<>() {
            @Override public Money visitFlight(kz.aitu.travel.offer.FlightOffer offer) {
                throw new IllegalArgumentException("Fee calculation failed");
            }
            @Override public Money visitHotel(kz.aitu.travel.offer.HotelOffer offer) { return Money.ZERO; }
            @Override public Money visitCarRental(kz.aitu.travel.offer.CarRentalOffer offer) { return Money.ZERO; }
        };
        Booking booking = new BookingBuilder("BK-failure", NOW, failing).forCustomer(customer)
                .addItem(new OfferBookingItem(flight("f"))).build();
        booking.confirm();
        booking.pay();
        assertThrows(IllegalArgumentException.class, booking::cancel);
        assertEquals(BookingStatus.PAID, booking.status());
        assertEquals(Money.ZERO, booking.cancellationFee());
        assertEquals(Money.ZERO, booking.refund());
    }

}

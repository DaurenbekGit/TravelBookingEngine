// Owner: Daurenbek
package kz.aitu.travel.offer.visitor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import kz.aitu.travel.domain.City;
import kz.aitu.travel.domain.Money;
import kz.aitu.travel.offer.CarRentalOffer;
import kz.aitu.travel.offer.FlightOffer;
import kz.aitu.travel.offer.HotelOffer;
import kz.aitu.travel.offer.OfferSpec;
import kz.aitu.travel.offer.TravelOffer;
import kz.aitu.travel.offer.factory.CarRentalOfferFactory;
import kz.aitu.travel.offer.factory.FlightOfferFactory;
import kz.aitu.travel.offer.factory.HotelOfferFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.jupiter.api.Assertions.*;

class OfferVisitorsTest {
    private static OfferSpec spec(String id, String price, long days, OfferSpec.Details details) {
        return new OfferSpec(id, "Test supplier", "Test " + id, City.NQZ, Money.of(price), 4.5,
                LocalDateTime.of(2026, 12, 20, 8, 0), Duration.ofDays(days), details);
    }
    private static TravelOffer flight() {
        return new FlightOfferFactory().create(spec("f", "45000", 1,
                new OfferSpec.FlightDetails("SJ101", City.ALA)));
    }
    private static TravelOffer hotel() {
        return new HotelOfferFactory().create(spec("h", "60000", 2,
                new OfferSpec.HotelDetails(2, "Double")));
    }
    private static TravelOffer car() {
        return new CarRentalOfferFactory().create(spec("c", "36000", 2,
                new OfferSpec.CarDetails(2, "Corolla")));
    }
    static Stream<Arguments> taxes() {
        return Stream.of(Arguments.of(flight(), "2500"), Arguments.of(hotel(), "2000"),
                Arguments.of(car(), "4320"));
    }
    static Stream<Arguments> cancellationFees() {
        return Stream.of(Arguments.of(flight(), "4500"), Arguments.of(hotel(), "30000"),
                Arguments.of(car(), "3000"));
    }
    static Stream<Arguments> details() {
        return Stream.of(Arguments.of(flight(), "Flight number: SJ101", "Route: ALA -> NQZ"),
                Arguments.of(hotel(), "Room: Double", "Nights: 2"),
                Arguments.of(car(), "Model: Corolla", "Rental days: 2"));
    }
    static Stream<Arguments> dispatchCases() {
        return Stream.of(Arguments.of(flight(), "flight"), Arguments.of(hotel(), "hotel"),
                Arguments.of(car(), "car"));
    }
    @ParameterizedTest @MethodSource("taxes")
    void taxRulesVisitEveryProduct(TravelOffer offer, String expected) {
        Money original = offer.price();
        assertEquals(Money.of(expected), offer.accept(new TaxCalculationVisitor()));
        assertEquals(original, offer.price());
    }
    @ParameterizedTest @MethodSource("cancellationFees")
    void cancellationRulesVisitEveryProduct(TravelOffer offer, String expected) {
        Money original = offer.price();
        assertEquals(Money.of(expected), offer.accept(new CancellationFeeVisitor()));
        assertEquals(original, offer.price());
    }
    @ParameterizedTest @MethodSource("details")
    void detailsVisitEveryProduct(TravelOffer offer, String first, String second) {
        String description = offer.accept(new OfferDetailsVisitor());
        assertTrue(description.contains(first));
        assertTrue(description.contains(second));
        assertTrue(description.contains(offer.price().toString()));
    }
    @ParameterizedTest @MethodSource("dispatchCases")
    void baseReferenceDispatchesToMatchingVisitorMethod(TravelOffer offer, String expected) {
        OfferVisitor<String> recording = new OfferVisitor<>() {
            @Override public String visitFlight(FlightOffer flight) { return "flight"; }
            @Override public String visitHotel(HotelOffer hotel) { return "hotel"; }
            @Override public String visitCarRental(CarRentalOffer car) { return "car"; }
        };
        assertEquals(expected, offer.accept(recording));
    }
    @Test void priceCopyChangesPercentageFeesWithoutChangingOriginalQuote() {
        TravelOffer original = flight();
        TravelOffer reduced = original.withPrice(Money.of("40500"));
        var visitor = new CancellationFeeVisitor();
        assertEquals(Money.of("4500"), original.accept(visitor));
        assertEquals(Money.of("4050"), reduced.accept(visitor));
    }
    @Test void hotelOneNightFeeRoundsToTiyn() {
        var offer = new HotelOfferFactory().create(spec("thirds", "10000", 3,
                new OfferSpec.HotelDetails(3, "Single")));
        assertEquals(Money.of("3333.33"), offer.accept(new CancellationFeeVisitor()));
    }
    @Test void carTaxUsesTotalRentalPrice() {
        TravelOffer reduced = car().withPrice(Money.of("10000"));
        assertEquals(Money.of("1200"), reduced.accept(new TaxCalculationVisitor()));
        assertEquals(Money.of("3000"), reduced.accept(new CancellationFeeVisitor()));
    }
@Test void airportTaxIsFixedAcrossPriceChanges() {
        var visitor = new TaxCalculationVisitor();
        TravelOffer cheap = flight().withPrice(Money.of("10000"));
        TravelOffer expensive = flight().withPrice(Money.of("90000"));
        assertEquals(Money.of("2500"), cheap.accept(visitor));
        assertEquals(cheap.accept(visitor), expensive.accept(visitor));
    }
    @Test void touristTaxScalesWithNumberOfNights() {
        var visitor = new TaxCalculationVisitor();
        var longerStay = new HotelOfferFactory().create(spec("long", "90000", 3,
                new OfferSpec.HotelDetails(3, "Suite")));
        assertEquals(Money.of("2000"), hotel().accept(visitor));
        assertEquals(Money.of("3000"), longerStay.accept(visitor));
    }
    @Test void rentalVatRoundsToTiyn() {
        TravelOffer fractional = car().withPrice(Money.of("10.13"));
        Money tax = fractional.accept(new TaxCalculationVisitor());
        assertEquals(Money.of("1.22"), tax);
        assertEquals(Money.of("10.13"), fractional.price());
    }
    @Test void freeFlightHasZeroCancellationFeeAndFixedAirportTax() {
        TravelOffer free = flight().withPrice(Money.ZERO);
        Money fee = free.accept(new CancellationFeeVisitor());
        Money tax = free.accept(new TaxCalculationVisitor());
        assertEquals(Money.ZERO, fee);
        assertEquals(Money.of("2500"), tax);
    }
    @Test void freeHotelHasZeroCancellationFeeAndNightlyTouristTax() {
        TravelOffer free = hotel().withPrice(Money.ZERO);
        Money fee = free.accept(new CancellationFeeVisitor());
        Money tax = free.accept(new TaxCalculationVisitor());
        assertEquals(Money.ZERO, fee);
        assertEquals(Money.of("2000"), tax);
    }
    @Test void freeCarQuotesFixedFeeBeforeBookingAppliesItsCap() {
        TravelOffer free = car().withPrice(Money.ZERO);
        Money fee = free.accept(new CancellationFeeVisitor());
        Money tax = free.accept(new TaxCalculationVisitor());
        assertEquals(Money.of("3000"), fee);
        assertEquals(Money.ZERO, tax);
    }
    @Test void detailVisitorShowsCopiedPriceAndPreservesOriginal() {
        var visitor = new OfferDetailsVisitor();
        TravelOffer original = hotel();
        TravelOffer reduced = original.withPrice(Money.of("50000"));
        String before = original.accept(visitor);
        String after = reduced.accept(visitor);
        assertTrue(before.contains("60 000 KZT"));
        assertTrue(after.contains("50 000 KZT"));
        assertTrue(after.contains("Room: Double"));
        assertEquals(Money.of("60000"), original.price());
    }
    @Test void visitorCanBeReusedAcrossMixedOfferElements() {
        var visitor = new CancellationFeeVisitor();
        var offers = java.util.List.of(flight(), hotel(), car());
        var firstPass = offers.stream().map(offer -> offer.accept(visitor)).toList();
        var secondPass = offers.stream().map(offer -> offer.accept(visitor)).toList();
        assertEquals(java.util.List.of(Money.of("4500"), Money.of("30000"), Money.of("3000")), firstPass);
        assertEquals(firstPass, secondPass);
    }
    @Test void flightPercentageFeeRoundsHalfUpAtHalfATiyn() {
        TravelOffer tinyPrice = flight().withPrice(Money.of("0.05"));
        Money fee = tinyPrice.accept(new CancellationFeeVisitor());
        assertEquals(Money.of("0.01"), fee);
        assertEquals(Money.of("0.05"), tinyPrice.price());
    }

    @Test
    void hotelDiscountChangesOneNightCancellationQuote() {
        TravelOffer original = hotel();
        TravelOffer discounted = original.withPrice(Money.of("50000"));
        var fees = new CancellationFeeVisitor();
        var taxes = new TaxCalculationVisitor();
        Money originalFee = original.accept(fees);
        Money discountedFee = discounted.accept(fees);
        assertEquals(Money.of("30000"), originalFee);
        assertEquals(Money.of("25000"), discountedFee);
        assertEquals(original.accept(taxes), discounted.accept(taxes));
        assertEquals(Money.of("60000"), original.price());
    }
    @Test
    void carPriceCopyRetainsVisitorPickupAndModelDetails() {
        TravelOffer original = car();
        TravelOffer discounted = original.withPrice(Money.of("30000"));
        var details = new OfferDetailsVisitor();
        String originalText = original.accept(details);
        String discountedText = discounted.accept(details);
        assertTrue(originalText.contains("36 000 KZT"));
        assertTrue(discountedText.contains("30 000 KZT"));
        assertTrue(discountedText.contains("Model: Corolla"));
        assertTrue(discountedText.contains("Rental days: 2"));
        assertTrue(discountedText.contains("Pickup: 2026-12-20T08:00"));
        assertEquals(original.start(), discounted.start());
    }

}

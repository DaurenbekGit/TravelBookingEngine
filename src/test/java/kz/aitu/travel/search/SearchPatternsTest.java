// Owner: Member A
package kz.aitu.travel.search;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import kz.aitu.travel.domain.City;
import kz.aitu.travel.domain.Money;
import kz.aitu.travel.domain.SearchCriteria;
import kz.aitu.travel.domain.TravelType;
import kz.aitu.travel.offer.CarRentalOffer;
import kz.aitu.travel.offer.FlightOffer;
import kz.aitu.travel.offer.HotelOffer;
import kz.aitu.travel.offer.OfferSpec;
import kz.aitu.travel.offer.TravelOffer;
import kz.aitu.travel.offer.factory.CarRentalOfferFactory;
import kz.aitu.travel.offer.factory.FlightOfferFactory;
import kz.aitu.travel.offer.factory.HotelOfferFactory;
import kz.aitu.travel.offer.factory.OfferFactory;
import kz.aitu.travel.ranking.CheapestFirstStrategy;
import kz.aitu.travel.ranking.RankingType;
import kz.aitu.travel.repository.OfferCatalog;
import kz.aitu.travel.supplier.SupplierClient;
import kz.aitu.travel.supplier.adapter.DriveNowCarAdapter;
import kz.aitu.travel.supplier.adapter.GlobalStayHotelAdapter;
import kz.aitu.travel.supplier.adapter.SkyJetFlightAdapter;
import kz.aitu.travel.supplier.external.DriveNowApi;
import kz.aitu.travel.supplier.external.GlobalStayApi;
import kz.aitu.travel.supplier.external.SkyJetApi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.jupiter.api.Assertions.*;

class SearchPatternsTest {
    private static final LocalDate DATE = LocalDate.of(2026, 12, 20);

    static Stream<OfferFactory> factories() {
        return Stream.of(new FlightOfferFactory(), new HotelOfferFactory(), new CarRentalOfferFactory());
    }
    private OfferSpec spec(String id, String price, double rating, long minutes, OfferSpec.Details details) {
        return new OfferSpec(id, "Test supplier", "Offer " + id, City.NQZ, Money.of(price), rating,
                DATE.atTime(8, 0), Duration.ofMinutes(minutes), details);
    }
    private TravelOffer flight(String id, String price, double rating, long minutes) {
        return new FlightOfferFactory().create(spec(id, price, rating, minutes,
                new OfferSpec.FlightDetails(id, City.ALA)));
    }
    private SearchCriteria criteria(TravelType type) {
        return new SearchCriteria(type, City.ALA, City.NQZ, DATE, 2);
    }
    @Test void flightCreatorBuildsTypedProduct() {
        var offer = assertInstanceOf(FlightOffer.class, flight("F1", "45000", 4.6, 95));
        assertEquals(TravelType.FLIGHT, offer.type());
        assertEquals(City.ALA, offer.details().origin());
        assertTrue(offer.summary().contains("F1 from ALA"));
    }
    @Test void hotelCreatorBuildsTypedProduct() {
        var offer = new HotelOfferFactory().create(spec("H1", "60000", 4.8, 2880,
                new OfferSpec.HotelDetails(2, "Double")));
        assertEquals(2, assertInstanceOf(HotelOffer.class, offer).nights());
        assertEquals(TravelType.HOTEL, offer.type());
        assertTrue(offer.summary().contains("2 nights"));
    }
    @Test void carCreatorBuildsTypedProduct() {
        var offer = new CarRentalOfferFactory().create(spec("C1", "36000", 4.4, 2880,
                new OfferSpec.CarDetails(2, "Corolla")));
        assertEquals(2, assertInstanceOf(CarRentalOffer.class, offer).days());
        assertEquals(TravelType.CAR_RENTAL, offer.type());
    }
    @ParameterizedTest @MethodSource("factories") void creatorsRejectBlankId(OfferFactory factory) {
        assertThrows(IllegalArgumentException.class, () -> factory.create(spec("", "10", 4, 95,
                new OfferSpec.FlightDetails("F", City.ALA))));
    }
    @ParameterizedTest @MethodSource("factories") void creatorsRejectInvalidRatings(OfferFactory factory) {
        assertThrows(IllegalArgumentException.class, () -> factory.create(spec("X", "10", Double.NaN, 95,
                new OfferSpec.FlightDetails("F", City.ALA))));
    }
    @ParameterizedTest @MethodSource("factories") void creatorsRejectZeroDuration(OfferFactory factory) {
        assertThrows(IllegalArgumentException.class, () -> factory.create(spec("X", "10", 4, 0,
                new OfferSpec.FlightDetails("F", City.ALA))));
    }
    @Test void creatorsRejectWrongDetails() {
        assertThrows(IllegalArgumentException.class, () -> new FlightOfferFactory().create(
                spec("F", "10", 4, 95, new OfferSpec.HotelDetails(1, "Single"))));
        assertThrows(IllegalArgumentException.class, () -> new HotelOfferFactory().create(
                spec("H", "10", 4, 95, new OfferSpec.CarDetails(1, "Car"))));
        assertThrows(IllegalArgumentException.class, () -> new CarRentalOfferFactory().create(
                spec("C", "10", 4, 95, new OfferSpec.FlightDetails("F", City.ALA))));
    }
    @Test void creatorsRejectInvalidSpecificFields() {
        assertThrows(IllegalArgumentException.class, () -> new FlightOfferFactory().create(
                spec("F", "10", 4, 95, new OfferSpec.FlightDetails("", City.ALA))));
        assertThrows(IllegalArgumentException.class, () -> new HotelOfferFactory().create(
                spec("H", "10", 4, 95, new OfferSpec.HotelDetails(0, "Double"))));
        assertThrows(IllegalArgumentException.class, () -> new CarRentalOfferFactory().create(
                spec("C", "10", 4, 95, new OfferSpec.CarDetails(0, "Car"))));
    }
    @Test void priceCopyPreservesOriginalAndType() {
        TravelOffer original = flight("F1", "45000", 4.6, 95);
        TravelOffer changed = original.withPrice(Money.of("40000"));
        assertEquals(Money.of("45000"), original.price());
        assertEquals(Money.of("40000"), changed.price());
        assertEquals(original.id(), changed.id());
        assertInstanceOf(FlightOffer.class, changed);
    }
    @Test void flightAdapterParsesRouteTimeDurationAndKzt() {
        var adapter = new SkyJetFlightAdapter(new SkyJetApi(), new FlightOfferFactory());
        var offer = (FlightOffer) adapter.search(criteria(TravelType.FLIGHT)).get(0);
        assertEquals(City.NQZ, offer.location());
        assertEquals(City.ALA, offer.details().origin());
        assertEquals(LocalDateTime.of(2026, 12, 20, 8, 0), offer.start());
        assertEquals(Duration.ofMinutes(95), offer.duration());
        assertEquals(Money.of("45000"), offer.price());
    }
    @Test void hotelAdapterConvertsUsdAndMultipliesNights() {
        var adapter = new GlobalStayHotelAdapter(new GlobalStayApi(), new HotelOfferFactory());
        var offer = adapter.search(criteria(TravelType.HOTEL)).get(0);
        assertEquals(Money.of("60000"), offer.price());
        assertEquals(Duration.ofDays(2), offer.duration());
        assertEquals("Astana Central", offer.title());
    }
    @Test void carAdapterConvertsTiynAndMultipliesDays() {
        var adapter = new DriveNowCarAdapter(new DriveNowApi(), new CarRentalOfferFactory());
        var offer = adapter.search(criteria(TravelType.CAR_RENTAL)).get(0);
        assertEquals(Money.of("36000"), offer.price());
        assertEquals("Toyota Corolla", offer.title());
        assertEquals(TravelType.CAR_RENTAL, offer.type());
    }
    @Test void offerIdentityIncludesDateRouteAndLength() {
        var adapter = new GlobalStayHotelAdapter(new GlobalStayApi(), new HotelOfferFactory());
        var first = adapter.search(criteria(TravelType.HOTEL)).get(0);
        var second = adapter.search(new SearchCriteria(TravelType.HOTEL, City.ALA, City.NQZ, DATE, 3)).get(0);
        assertNotEquals(first.id(), second.id());
    }
    @Test void sameAirportHasNoFlights() {
        var adapter = new SkyJetFlightAdapter(new SkyJetApi(), new FlightOfferFactory());
        assertTrue(adapter.search(new SearchCriteria(TravelType.FLIGHT, City.NQZ, City.NQZ, DATE, 1)).isEmpty());
    }
    @Test void searchMergesMatchingSuppliersAndCatalogsResults() {
        var catalog = new OfferCatalog();
        var a = flight("a", "40000", 4, 95);
        var b = flight("b", "30000", 4, 95);
        var search = new OfferSearchService(List.of(supplier(a), supplier(b)),
                Map.of(RankingType.CHEAPEST, new CheapestFirstStrategy()), catalog);
        assertEquals(List.of("b", "a"), ids(search.search(criteria(TravelType.FLIGHT), RankingType.CHEAPEST)));
        assertSame(a, catalog.find("a"));
        assertTrue(search.search(criteria(TravelType.HOTEL), RankingType.CHEAPEST).isEmpty());
    }
    @Test void searchUsesUpdatedCatalogPrices() {
        var catalog = new OfferCatalog();
        var offer = flight("a", "40000", 4, 95);
        var search = new OfferSearchService(List.of(supplier(offer)),
                Map.of(RankingType.CHEAPEST, new CheapestFirstStrategy()), catalog);
        search.search(criteria(TravelType.FLIGHT), RankingType.CHEAPEST);
        catalog.update("a", old -> old.withPrice(Money.of("1")));
        assertEquals(Money.of("1"), search.search(criteria(TravelType.FLIGHT), RankingType.CHEAPEST).get(0).price());
    }
    @Test void searchRejectsUnconfiguredStrategy() {
        var search = new OfferSearchService(List.of(), Map.of(), new OfferCatalog());
        assertThrows(IllegalArgumentException.class, () -> search.search(criteria(TravelType.FLIGHT), RankingType.FASTEST));
    }
    private List<String> ids(List<TravelOffer> offers) { return offers.stream().map(TravelOffer::id).toList(); }
    private SupplierClient supplier(TravelOffer offer) {
        return new SupplierClient() {
            @Override public List<TravelOffer> search(SearchCriteria criteria) { return List.of(offer); }
            @Override public TravelType supportedType() { return TravelType.FLIGHT; }
            @Override public String supplierName() { return "Fake"; }
        };
    }
    @Test void hotelAndCarPriceCopiesPreserveTypedDetails() {
        var hotel = new HotelOfferFactory().create(spec("h", "60000", 4.5, 2880,
                new OfferSpec.HotelDetails(2, "Suite")));
        var car = new CarRentalOfferFactory().create(spec("c", "36000", 4.4, 2880,
                new OfferSpec.CarDetails(2, "Corolla")));
        var hotelCopy = assertInstanceOf(HotelOffer.class, hotel.withPrice(Money.of("50000")));
        var carCopy = assertInstanceOf(CarRentalOffer.class, car.withPrice(Money.of("30000")));
        assertEquals("Suite", hotelCopy.details().roomType());
        assertEquals("Corolla", carCopy.details().model());
        assertEquals(Money.of("60000"), hotel.price());
        assertEquals(Money.of("36000"), car.price());
    }

}

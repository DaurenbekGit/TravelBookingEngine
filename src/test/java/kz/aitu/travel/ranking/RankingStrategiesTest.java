// Owner: Member B
package kz.aitu.travel.ranking;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import kz.aitu.travel.domain.City;
import kz.aitu.travel.domain.Money;
import kz.aitu.travel.offer.OfferSpec;
import kz.aitu.travel.offer.TravelOffer;
import kz.aitu.travel.offer.factory.FlightOfferFactory;
import kz.aitu.travel.ranking.BestValueStrategy;
import kz.aitu.travel.ranking.CheapestFirstStrategy;
import kz.aitu.travel.ranking.FastestFirstStrategy;
import kz.aitu.travel.ranking.OfferRankingStrategy;
import kz.aitu.travel.ranking.TopRatedStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.jupiter.api.Assertions.*;

class RankingStrategiesTest {
    private static final LocalDate DATE = LocalDate.of(2026, 12, 20);

    static Stream<OfferRankingStrategy> strategies() {
        return Stream.of(new CheapestFirstStrategy(), new FastestFirstStrategy(),
                new TopRatedStrategy(), new BestValueStrategy());
    }
    @Test void cheapestOrdersByPrice() {
        assertEquals(List.of("b", "c", "a"), ids(new CheapestFirstStrategy().rank(sample())));
    }
    @Test void fastestBreaksDurationTieByPrice() {
        assertEquals(List.of("b", "c", "a"), ids(new FastestFirstStrategy().rank(sample())));
    }
    @Test void ratingBreaksRatingTieByPrice() {
        assertEquals(List.of("c", "a", "b"), ids(new TopRatedStrategy().rank(sample())));
    }
    @Test void bestValueUsesRatingPerTenThousand() {
        assertEquals(List.of("b", "c", "a"), ids(new BestValueStrategy().rank(sample())));
    }
    @Test void freeOfferRanksBeforePaidOffers() {
        var free = flight("free", "0", 4, 90);
        assertSame(free, new BestValueStrategy().rank(List.of(sample().get(0), free)).get(0));
    }
    @ParameterizedTest @MethodSource("strategies") void rankingDoesNotMutateInput(OfferRankingStrategy strategy) {
        var input = new ArrayList<>(sample());
        var before = List.copyOf(input);
        List<TravelOffer> ranked = strategy.rank(input);
        assertEquals(before, input);
        assertNotSame(input, ranked);
        ranked.clear();
        assertEquals(3, input.size());
    }
    private List<TravelOffer> sample() {
        return List.of(flight("a", "30000", 4.8, 100), flight("b", "10000", 3.0, 90),
                flight("c", "20000", 4.8, 90));
    }
    private OfferSpec spec(String id, String price, double rating, long minutes, OfferSpec.Details details) {
        return new OfferSpec(id, "Test supplier", "Offer " + id, City.NQZ, Money.of(price), rating,
                DATE.atTime(8, 0), Duration.ofMinutes(minutes), details);
    }
    private TravelOffer flight(String id, String price, double rating, long minutes) {
        return new FlightOfferFactory().create(spec(id, price, rating, minutes,
                new OfferSpec.FlightDetails(id, City.ALA)));
    }
    private List<String> ids(List<TravelOffer> offers) { return offers.stream().map(TravelOffer::id).toList(); }
}

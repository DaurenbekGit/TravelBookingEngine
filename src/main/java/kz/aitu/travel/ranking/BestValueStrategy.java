// Owner: Member B
package kz.aitu.travel.ranking;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import kz.aitu.travel.offer.TravelOffer;

/** Strategy (Behavioral): concrete BestValue ranking algorithm. */
public final class BestValueStrategy implements OfferRankingStrategy {
    @Override public List<TravelOffer> rank(List<TravelOffer> offers) {
        var sorted = new ArrayList<>(offers);
        sorted.sort(Comparator.comparingDouble(BestValueStrategy::score).reversed()
                .thenComparing(Comparator.comparingDouble(TravelOffer::rating).reversed())
                .thenComparing(TravelOffer::price).thenComparing(TravelOffer::id));
        return sorted;
    }

    /** Rating per 10,000 KZT; free offers score infinity, then rating breaks the tie. */
    private static double score(TravelOffer offer) {
        if (offer.price().equals(kz.aitu.travel.domain.Money.ZERO)) return Double.POSITIVE_INFINITY;
        return offer.rating() * VALUE_UNIT_KZT / offer.price().amount().doubleValue();
    }
    private static final double VALUE_UNIT_KZT = 10_000;
}

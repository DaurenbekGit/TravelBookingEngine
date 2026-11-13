// Owner: Member B
package kz.aitu.travel.ranking;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import kz.aitu.travel.offer.TravelOffer;

/** Strategy (Behavioral): concrete CheapestFirst ranking algorithm. */
public final class CheapestFirstStrategy implements OfferRankingStrategy {
    @Override public List<TravelOffer> rank(List<TravelOffer> offers) {
        var sorted = new ArrayList<>(offers);
        sorted.sort(Comparator.comparing(TravelOffer::price).thenComparing(TravelOffer::id));
        return sorted;
    }
}

// Owner: Nurkhat
package kz.aitu.travel.ranking;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import kz.aitu.travel.offer.TravelOffer;

/** Strategy (Behavioral): concrete TopRated ranking algorithm. */
public final class TopRatedStrategy implements OfferRankingStrategy {
    @Override public List<TravelOffer> rank(List<TravelOffer> offers) {
        var sorted = new ArrayList<>(offers);
        sorted.sort(Comparator.comparingDouble(TravelOffer::rating).reversed().thenComparing(TravelOffer::price).thenComparing(TravelOffer::id));
        return sorted;
    }
}

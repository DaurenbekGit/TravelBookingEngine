// Owner: Member B
package kz.aitu.travel.ranking;

import java.util.List;
import kz.aitu.travel.offer.TravelOffer;

/** Strategy (Behavioral): interchangeable ranking contract; never mutates input. */
public interface OfferRankingStrategy {
    List<TravelOffer> rank(List<TravelOffer> offers);
}

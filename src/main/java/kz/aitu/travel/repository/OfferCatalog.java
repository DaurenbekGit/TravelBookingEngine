// Owner: Abylay
package kz.aitu.travel.repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import kz.aitu.travel.offer.TravelOffer;

/** Session catalog: booking snapshots remain unchanged when catalog prices change. */
public final class OfferCatalog {
    private final Map<String, TravelOffer> offers = new LinkedHashMap<>();
    public synchronized void store(List<TravelOffer> found) {
        found.forEach(offer -> offers.putIfAbsent(offer.id(), offer));
    }
    public synchronized TravelOffer find(String id) {
        TravelOffer offer = offers.get(id);
        if (offer == null) throw new IllegalArgumentException("Unknown offer: " + id + "; search first");
        return offer;
    }
    public synchronized TravelOffer update(String id, UnaryOperator<TravelOffer> change) {
        TravelOffer previous = find(id);
        offers.put(id, change.apply(previous));
        return previous;
    }
}

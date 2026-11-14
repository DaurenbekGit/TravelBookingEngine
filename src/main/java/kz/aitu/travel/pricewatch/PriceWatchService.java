// Owner: Member C
package kz.aitu.travel.pricewatch;

import java.util.LinkedHashMap;
import java.util.Map;
import kz.aitu.travel.domain.Customer;
import kz.aitu.travel.domain.Money;
import kz.aitu.travel.notification.EventPublisher;
import kz.aitu.travel.notification.event.PriceDropEvent;
import kz.aitu.travel.offer.TravelOffer;
import kz.aitu.travel.repository.OfferCatalog;

/** Observer producer: notify distinct customer ids only after a genuine price decrease. */
public final class PriceWatchService {
    private static final int FULL_PERCENT = 100;
    private final OfferCatalog catalog;
    private final EventPublisher events;
    private final Map<String, Map<String, Customer>> watchers = new LinkedHashMap<>();
    public PriceWatchService(OfferCatalog catalog, EventPublisher events) {
        this.catalog = catalog;
        this.events = events;
    }
    public synchronized void watch(Customer customer, String offerId) {
        catalog.find(offerId);
        watchers.computeIfAbsent(offerId, key -> new LinkedHashMap<>()).put(customer.id(), customer);
    }
    public synchronized void applyPriceChange(String offerId, Money newPrice) {
        TravelOffer previous = catalog.update(offerId, offer -> offer.withPrice(newPrice));
        if (newPrice.compareTo(previous.price()) >= 0) return;
        for (Customer customer : watchers.getOrDefault(offerId, Map.of()).values()) {
            events.publish(new PriceDropEvent(customer, offerId, previous.price(), newPrice));
        }
    }
    public synchronized void simulatePriceDrop(String offerId, int percent) {
        if (percent <= 0 || percent > FULL_PERCENT) {
            throw new IllegalArgumentException("Drop percentage must be between 1 and 100");
        }
        applyPriceChange(offerId, catalog.find(offerId).price().percent(FULL_PERCENT - percent));
    }
}

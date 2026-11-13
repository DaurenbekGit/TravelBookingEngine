// Owner: Member A
package kz.aitu.travel.supplier.adapter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import kz.aitu.travel.domain.City;
import kz.aitu.travel.domain.Money;
import kz.aitu.travel.domain.SearchCriteria;
import kz.aitu.travel.domain.TravelType;
import kz.aitu.travel.offer.OfferSpec;
import kz.aitu.travel.offer.TravelOffer;
import kz.aitu.travel.offer.factory.OfferFactory;
import kz.aitu.travel.supplier.SupplierClient;
import kz.aitu.travel.supplier.external.SkyJetApi;

/** Adapter (Structural): translates SkyJet strings to the Target using a Creator. */
public final class SkyJetFlightAdapter implements SupplierClient {
    private final SkyJetApi api;
    private final OfferFactory factory;
    public SkyJetFlightAdapter(SkyJetApi api, OfferFactory factory) {
        this.api = api;
        this.factory = factory;
    }
    @Override public TravelType supportedType() { return TravelType.FLIGHT; }
    @Override public String supplierName() { return "SkyJet"; }
    @Override public List<TravelOffer> search(SearchCriteria criteria) {
        return api.findRoutes(criteria.origin().name(), criteria.destination().name(), criteria.date().toString())
                .stream().map(this::convert).toList();
    }
    private TravelOffer convert(String raw) {
        String[] fields = raw.split(";");
        String id = fields[0] + "-" + fields[1] + "-" + fields[2] + "-" + fields[3];
        return factory.create(new OfferSpec(id, supplierName(), "Flight " + fields[0], City.parse(fields[2]),
                Money.of(fields[5]), 4.6, LocalDateTime.parse(fields[3]),
                Duration.ofMinutes(Long.parseLong(fields[4])),
                new OfferSpec.FlightDetails(fields[0], City.parse(fields[1]))));
    }
}

// Owner: Member A
package kz.aitu.travel.supplier.adapter;

import java.util.List;
import java.util.Map;
import java.time.Duration;
import kz.aitu.travel.domain.Money;
import kz.aitu.travel.domain.SearchCriteria;
import kz.aitu.travel.domain.TravelType;
import kz.aitu.travel.offer.OfferSpec;
import kz.aitu.travel.offer.TravelOffer;
import kz.aitu.travel.offer.factory.OfferFactory;
import kz.aitu.travel.supplier.SupplierClient;
import kz.aitu.travel.supplier.external.DriveNowApi;

/** Adapter (Structural): converts tiyn daily quotes to total KZT offers. */
public final class DriveNowCarAdapter implements SupplierClient {
    private final DriveNowApi api;
    private final OfferFactory factory;
    public DriveNowCarAdapter(DriveNowApi api, OfferFactory factory) {
        this.api = api;
        this.factory = factory;
    }
    @Override public TravelType supportedType() { return TravelType.CAR_RENTAL; }
    @Override public String supplierName() { return "DriveNow"; }
    @Override public List<TravelOffer> search(SearchCriteria criteria) {
        return api.inventory(criteria.days(), criteria.date(), criteria.destination().name()).entrySet().stream()
                .sorted(Map.Entry.comparingByKey()).map(entry -> convert(entry, criteria)).toList();
    }
    private TravelOffer convert(Map.Entry<String, DriveNowApi.Vehicle> entry, SearchCriteria criteria) {
        var vehicle = entry.getValue();
        Money total = Money.of(Long.toString(vehicle.tiynPerDay()))
                .times(SupplierRates.TIYN_TO_KZT).times(criteria.days());
        String id = entry.getKey() + "-" + criteria.destination() + "-" + criteria.date() + "-" + criteria.days();
        return factory.create(new OfferSpec(id, supplierName(), vehicle.model(), criteria.destination(), total,
                vehicle.rating(), criteria.date().atTime(9, 0), Duration.ofDays(criteria.days()),
                new OfferSpec.CarDetails(criteria.days(), vehicle.model())));
    }
}

// Owner: Member A
package kz.aitu.travel.supplier.adapter;

import java.time.Duration;
import java.util.List;
import kz.aitu.travel.domain.Money;
import kz.aitu.travel.domain.SearchCriteria;
import kz.aitu.travel.domain.TravelType;
import kz.aitu.travel.offer.OfferSpec;
import kz.aitu.travel.offer.TravelOffer;
import kz.aitu.travel.offer.factory.OfferFactory;
import kz.aitu.travel.supplier.SupplierClient;
import kz.aitu.travel.supplier.external.GlobalStayApi;

/** Adapter (Structural): converts city names and USD nightly rates to total KZT. */
public final class GlobalStayHotelAdapter implements SupplierClient {
    private final GlobalStayApi api;
    private final OfferFactory factory;
    public GlobalStayHotelAdapter(GlobalStayApi api, OfferFactory factory) {
        this.api = api;
        this.factory = factory;
    }
    @Override public TravelType supportedType() { return TravelType.HOTEL; }
    @Override public String supplierName() { return "GlobalStay"; }
    @Override public List<TravelOffer> search(SearchCriteria criteria) {
        return api.roomsIn(criteria.destination().displayName(), criteria.date(), criteria.date().plusDays(criteria.days()))
                .stream().map(room -> convert(room, criteria)).toList();
    }
    private TravelOffer convert(GlobalStayApi.Room room, SearchCriteria criteria) {
        Money total = Money.of(room.usdPerNight()).times(SupplierRates.USD_TO_KZT).times(criteria.days());
        String id = room.code() + "-" + criteria.destination() + "-" + criteria.date() + "-" + criteria.days();
        return factory.create(new OfferSpec(id, supplierName(), room.label(), criteria.destination(), total,
                room.stars(), criteria.date().atTime(14, 0), Duration.ofDays(criteria.days()),
                new OfferSpec.HotelDetails(criteria.days(), room.roomType())));
    }
}

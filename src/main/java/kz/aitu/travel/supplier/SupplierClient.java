// Owner: Member A
package kz.aitu.travel.supplier;

import java.util.List;
import kz.aitu.travel.domain.SearchCriteria;
import kz.aitu.travel.domain.TravelType;
import kz.aitu.travel.offer.TravelOffer;

/** Adapter (Structural): Target understood by the search service. */
public interface SupplierClient {
    List<TravelOffer> search(SearchCriteria criteria);
    TravelType supportedType();
    String supplierName();
}

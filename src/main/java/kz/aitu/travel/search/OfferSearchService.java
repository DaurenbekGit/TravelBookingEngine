// Owner: Daurenbek
package kz.aitu.travel.search;

import java.util.List;
import java.util.Map;
import kz.aitu.travel.domain.SearchCriteria;
import kz.aitu.travel.offer.TravelOffer;
import kz.aitu.travel.ranking.OfferRankingStrategy;
import kz.aitu.travel.ranking.RankingType;
import kz.aitu.travel.repository.OfferCatalog;
import kz.aitu.travel.supplier.SupplierClient;

/** Strategy (Behavioral): Context; Adapter targets supply Factory Method products. */
public final class OfferSearchService {
    private final List<SupplierClient> suppliers;
    private final Map<RankingType, OfferRankingStrategy> strategies;
    private final OfferCatalog catalog;
    public OfferSearchService(List<SupplierClient> suppliers,
                              Map<RankingType, OfferRankingStrategy> strategies, OfferCatalog catalog) {
        this.suppliers = List.copyOf(suppliers);
        this.strategies = Map.copyOf(strategies);
        this.catalog = catalog;
    }
    public List<TravelOffer> search(SearchCriteria criteria, RankingType ranking) {
        OfferRankingStrategy strategy = strategies.get(ranking);
        if (strategy == null) throw new IllegalArgumentException("Unknown ranking: " + ranking);
        List<TravelOffer> found = suppliers.stream().filter(s -> s.supportedType() == criteria.type())
                .flatMap(s -> s.search(criteria).stream()).toList();
        catalog.store(found);
        return strategy.rank(found.stream().map(offer -> catalog.find(offer.id())).toList());
    }
}

// Owner: Abylay
package kz.aitu.travel.cli;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import kz.aitu.travel.booking.extra.ExtraType;
import kz.aitu.travel.booking.InvalidBookingStateException;
import kz.aitu.travel.domain.City;
import kz.aitu.travel.domain.Customer;
import kz.aitu.travel.domain.SearchCriteria;
import kz.aitu.travel.domain.TravelType;
import kz.aitu.travel.facade.TravelBookingFacade;
import kz.aitu.travel.ranking.RankingType;

/** Facade client: reproducible evidence showing the nine patterns cooperating. */
public final class DemoScenario {
    private final TravelBookingFacade facade;
    private final Customer customer;
    private final ConsoleView view;
    public DemoScenario(TravelBookingFacade facade, Customer customer, ConsoleView view) {
        this.facade = facade;
        this.customer = customer;
        this.view = view;
    }
    public void run() {
        view.line("=== [Facade] One entry point for the complete journey ===");
        view.line("=== [Adapter] Searching 3 incompatible supplier APIs ===");
        var flightCriteria = criteria(TravelType.FLIGHT);
        var flights = facade.searchOffers(flightCriteria, RankingType.CHEAPEST);
        var hotels = facade.searchOffers(criteria(TravelType.HOTEL), RankingType.CHEAPEST);
        var cars = facade.searchOffers(criteria(TravelType.CAR_RENTAL), RankingType.CHEAPEST);
        view.offers(flights); view.offers(hotels); view.offers(cars);
        view.line("=== [Factory Method] Typed immutable products from injected creators ===");
        view.line(flights.get(0).getClass().getSimpleName() + " / " + hotels.get(0).getClass().getSimpleName()
                + " / " + cars.get(0).getClass().getSimpleName());
        view.line("=== [Strategy] Change ranking at runtime ===");
        for (RankingType ranking : RankingType.values()) {
            view.line(ranking.name());
            view.offers(facade.searchOffers(flightCriteria, ranking));
        }
        String flightId = flights.get(0).id();
        String hotelId = hotels.get(0).id();
        view.line("=== [Builder] Assemble a multi-item booking ===");
        var booking = facade.createBooking(customer, List.of(flightId, hotelId, cars.get(0).id()),
                Map.of(flightId, List.of(ExtraType.BAGGAGE, ExtraType.INSURANCE, ExtraType.FLEXIBLE),
                        hotelId, List.of(ExtraType.BREAKFAST)));
        view.line("=== [Decorator] Stack baggage, insurance and flexibility; add breakfast ===");
        view.booking(booking);
        view.line("=== [Visitor] Type-specific details, taxes and cancellation fees ===");
        for (String id : List.of(flightId, hotelId, cars.get(0).id())) {
            view.line(facade.offerDetails(id));
            view.line("Illustrative taxes (display only): " + facade.offerTaxes(id));
        }
        view.line("=== [Observer] Email, SMS and audit listen to successful status changes ===");
        view.line("--- Lifecycle validation: reject early payment, then confirm, pay and cancel ---");
        try { facade.payBooking(booking.id()); }
        catch (InvalidBookingStateException exception) { view.line("Handled: " + exception.getMessage()); }
        facade.confirmBooking(booking.id());
        facade.payBooking(booking.id());
        view.booking(facade.cancelBooking(booking.id()));
        facade.watchPrice(customer, flightId);
        facade.simulatePriceDrop(flightId, 10);
        view.line("Catalog now: " + facade.findOffer(flightId).price() + "; booking snapshot: " + booking.items().get(0).offer().price());
        view.line("=== [Abstract Factory] Same event via Local (RU) and International (EN) notification families ===");
        facade.previewNotificationFamilies();
        view.line("Audit events: " + facade.notificationLog().size());
        view.line("=== Demo complete ===");
    }
    private SearchCriteria criteria(TravelType type) {
        return new SearchCriteria(type, City.ALA, City.NQZ, LocalDate.of(2026, 12, 20), 2);
    }
}

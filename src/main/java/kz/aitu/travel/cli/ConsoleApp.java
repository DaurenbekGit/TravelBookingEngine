// Owner: Abylay
package kz.aitu.travel.cli;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.NoSuchElementException;
import kz.aitu.travel.booking.extra.ExtraType;
import kz.aitu.travel.domain.City;
import kz.aitu.travel.domain.Customer;
import kz.aitu.travel.domain.SearchCriteria;
import kz.aitu.travel.domain.TravelType;
import kz.aitu.travel.facade.TravelBookingFacade;
import kz.aitu.travel.ranking.RankingType;

/** Facade client: input parsing and display only, with recoverable input errors. */
public final class ConsoleApp {
    private final TravelBookingFacade facade;
    private final Customer customer;
    private final Scanner input;
    private final ConsoleView view;
    private final Map<String, Runnable> actions;
    public ConsoleApp(TravelBookingFacade facade, Customer customer, Scanner input, ConsoleView view) {
        this.facade = facade;
        this.customer = customer;
        this.input = input;
        this.view = view;
        actions = Map.of("1", this::search, "2", this::details, "3", this::book,
                "4", () -> view.booking(facade.confirmBooking(ask("Booking id"))),
                "5", () -> view.booking(facade.payBooking(ask("Booking id"))),
                "6", () -> view.booking(facade.cancelBooking(ask("Booking id"))),
                "7", () -> facade.bookingsOf(customer).forEach(view::booking), "8", this::watch,
                "9", this::drop, "10", () -> facade.notificationLog().forEach(e -> view.line(e.message())));
    }
    public void run() {
        view.line("Travel Booking Engine | " + customer.name() + " | KZT");
        while (true) {
            view.line("1 Search  2 Details/extras  3 Book  4 Confirm  5 Pay  6 Cancel");
            view.line("7 My bookings  8 Watch price  9 Price drop  10 Notifications  0 Exit");
            if (!input.hasNextLine()) return;
            String choice = input.nextLine().trim();
            if (choice.equals("0")) return;
            try {
                Runnable action = actions.get(choice);
                if (action == null) throw new IllegalArgumentException("Choose a menu number from 0 to 10");
                action.run();
            } catch (NoSuchElementException exception) { return;
            } catch (IllegalArgumentException | IllegalStateException | java.time.DateTimeException exception) {
                view.line("Please try again: " + exception.getMessage());
            }
        }
    }
    private String ask(String prompt) { view.line(prompt + ":"); return input.nextLine().trim(); }
    private <T> T choose(T[] values, String label) {
        for (int index = 0; index < values.length; index++) view.line((index + 1) + " " + values[index]);
        int index = Integer.parseInt(ask(label)) - 1;
        if (index < 0 || index >= values.length) throw new IllegalArgumentException("Invalid " + label);
        return values[index];
    }
    private void search() {
        TravelType type = choose(TravelType.values(), "Travel type");
        view.line("Cities: NQZ/Astana, ALA/Almaty, CIT/Shymkent, IST/Istanbul, DXB/Dubai");
        City origin = City.parse(ask("Origin (ignored for hotels/cars)"));
        City destination = City.parse(ask("Destination"));
        LocalDate date = LocalDate.parse(ask("Date YYYY-MM-DD"));
        int days = Integer.parseInt(ask("Nights/rental days (1 for flights)"));
        RankingType ranking = choose(RankingType.values(), "Ranking");
        view.offers(facade.searchOffers(new SearchCriteria(type, origin, destination, date, days), ranking));
    }
    private void details() {
        String id = ask("Offer id");
        view.line(facade.offerDetails(id));
        view.line("Illustrative taxes (display only): " + facade.offerTaxes(id));
        facade.availableExtras(id).forEach(extra -> view.line(extra.name() + ": " + extra.displayName()));
    }
    private void book() {
        List<String> ids = Arrays.stream(ask("Offer ids separated by commas").split(",")).map(String::trim).toList();
        var extras = new LinkedHashMap<String, List<ExtraType>>();
        for (String id : ids) {
            view.line("Available for " + id + ": " + facade.availableExtras(id));
            String selection = ask("Extra names in application order, comma separated; blank for none");
            extras.put(id, selection.isBlank() ? List.of() : Arrays.stream(selection.split(","))
                    .map(String::trim).map(s -> s.toUpperCase(java.util.Locale.ROOT)).map(ExtraType::valueOf).toList());
        }
        view.booking(facade.createBooking(customer, ids, extras));
    }
    private void watch() { facade.watchPrice(customer, ask("Offer id")); view.line("Price watch registered."); }
    private void drop() { facade.simulatePriceDrop(ask("Offer id"), Integer.parseInt(ask("Drop percent (1-100)"))); }
}

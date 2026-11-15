// Owner: Abylay
package kz.aitu.travel.cli;

import java.io.PrintStream;
import java.util.List;
import kz.aitu.travel.booking.Booking;
import kz.aitu.travel.offer.TravelOffer;

/** Presentation only: domain logic does not depend on console formatting. */
public final class ConsoleView {
    private final PrintStream output;
    public ConsoleView(PrintStream output) { this.output = output; }
    public void line(String text) { output.println(text); }
    public void offers(List<TravelOffer> offers) {
        output.printf("%-40s %-21s %14s %6s %8s%n", "ID", "TITLE", "PRICE", "RATING", "MINUTES");
        for (TravelOffer offer : offers) {
            output.printf("%-40s %-21s %14s %6.1f %8d%n", offer.id(), offer.title(), offer.price(),
                    offer.rating(), offer.duration().toMinutes());
        }
        if (offers.isEmpty()) line("No offers found.");
    }
    public void booking(Booking booking) {
        line(booking.id() + " | " + booking.customer().name() + " | " + booking.status() + " | " + booking.totalPrice());
        booking.items().forEach(item -> line("  " + item.description() + " => " + item.price()));
        line("  " + booking.lastMessage());
    }
}

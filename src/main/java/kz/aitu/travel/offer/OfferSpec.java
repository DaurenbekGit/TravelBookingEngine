// Owner: Member A
package kz.aitu.travel.offer;

import java.time.Duration;
import java.time.LocalDateTime;
import kz.aitu.travel.domain.City;
import kz.aitu.travel.domain.Money;

/** Factory Method input: typed details avoid ambiguous maps and unused fields. */
public record OfferSpec(String id, String supplierName, String title, City location,
                        Money price, double rating, LocalDateTime start, Duration duration,
                        Details details) {
    public sealed interface Details permits FlightDetails, HotelDetails, CarDetails { }
    public record FlightDetails(String flightNumber, City origin) implements Details { }
    public record HotelDetails(int nights, String roomType) implements Details { }
    public record CarDetails(int days, String model) implements Details { }

    public OfferSpec withPrice(Money replacement) {
        return new OfferSpec(id, supplierName, title, location, replacement, rating, start, duration, details);
    }
}

// Owner: Member A
package kz.aitu.travel.supplier.external;

import java.time.LocalDate;
import java.util.List;

/** Adapter (Structural): city-name and USD-per-night hotel Adaptee. */
public final class GlobalStayApi {
    public record Room(String code, String label, String usdPerNight, double stars, String roomType) { }
    public List<Room> roomsIn(String cityName, LocalDate checkIn, LocalDate checkOut) {
        if (!checkOut.isAfter(checkIn)) return List.of();
        return List.of(new Room("GS1", cityName + " Central", "60", 4.5, "Double"),
                new Room("GS2", cityName + " Grand", "100", 4.9, "Suite"));
    }
}

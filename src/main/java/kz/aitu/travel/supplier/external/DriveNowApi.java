// Owner: Daurenbek
package kz.aitu.travel.supplier.external;

import java.time.LocalDate;
import java.util.Map;

/** Adapter (Structural): map-based car Adaptee quoting integer tiyn per day. */
public final class DriveNowApi {
    public record Vehicle(String model, long tiynPerDay, double rating) { }
    public Map<String, Vehicle> inventory(int days, LocalDate pickup, String airport) {
        if (days < 1) return Map.of();
        return Map.of("DN1", new Vehicle("Toyota Corolla", 1800000L, 4.4),
                "DN2", new Vehicle("Hyundai Tucson", 2500000L, 4.8));
    }
}

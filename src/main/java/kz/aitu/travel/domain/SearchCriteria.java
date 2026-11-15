// Owner: Abylay
package kz.aitu.travel.domain;

import java.time.LocalDate;
import java.util.Objects;

/** Days means hotel nights or rental days; ignored by flight suppliers. */
public record SearchCriteria(TravelType type, City origin, City destination, LocalDate date, int days) {
    public SearchCriteria {
        Objects.requireNonNull(type, "Travel type is required");
        Objects.requireNonNull(origin, "Origin is required");
        Objects.requireNonNull(destination, "Destination is required");
        Objects.requireNonNull(date, "Date is required");
        if (days < 1 || days > 30) throw new IllegalArgumentException("Days must be between 1 and 30");
    }
}

// Owner: Member A
package kz.aitu.travel.supplier.external;

import java.time.LocalDate;
import java.util.List;

/** Adapter (Structural): string-based flight Adaptee, simulated without HTTP. */
public final class SkyJetApi {
    public List<String> findRoutes(String from, String to, String isoDate) {
        LocalDate.parse(isoDate);
        if (from.equals(to)) return List.of();
        return List.of("SJ101;" + from + ";" + to + ";" + isoDate + "T08:00;95;45000",
                "SJ202;" + from + ";" + to + ";" + isoDate + "T12:00;80;56000");
    }
}

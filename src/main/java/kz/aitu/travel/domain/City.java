// Owner: Member C
package kz.aitu.travel.domain;

import java.util.Arrays;

/** Shared translation between display names and supplier airport codes. */
public enum City {
    NQZ("Astana"), ALA("Almaty"), CIT("Shymkent"), IST("Istanbul"), DXB("Dubai");
    private final String displayName;
    City(String displayName) { this.displayName = displayName; }
    public String displayName() { return displayName; }
    public static City parse(String text) {
        return Arrays.stream(values()).filter(city -> city.name().equalsIgnoreCase(text.trim())
                || city.displayName.equalsIgnoreCase(text.trim())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown city: " + text));
    }
}

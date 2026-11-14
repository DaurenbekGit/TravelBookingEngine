// Owner: Member C
package kz.aitu.travel.repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import kz.aitu.travel.booking.Booking;

/** Thread-safe, instance-scoped storage; AppContext creates and injects one store per application. */
public final class InMemoryBookingRepository implements BookingRepository {
    private final ConcurrentHashMap<String, Booking> bookings = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1000);
    public InMemoryBookingRepository() { }
    @Override public String nextId() { return "BK-" + sequence.incrementAndGet(); }
    @Override public void save(Booking booking) { bookings.put(booking.id(), booking); }
    @Override public Optional<Booking> find(String id) { return Optional.ofNullable(bookings.get(id)); }
    @Override public List<Booking> all() {
        return bookings.values().stream().sorted(Comparator.comparing(Booking::createdAt)
                .thenComparing(Booking::id)).toList();
    }
}

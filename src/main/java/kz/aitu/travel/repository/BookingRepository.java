// Owner: Abylay
package kz.aitu.travel.repository;

import java.util.List;
import java.util.Optional;
import kz.aitu.travel.booking.Booking;
import kz.aitu.travel.domain.Customer;

/** Storage boundary: clients depend on this interface so tests can use private fakes. */
public interface BookingRepository {
    String nextId();
    void save(Booking booking);
    Optional<Booking> find(String id);
    List<Booking> all();
    default List<Booking> findByCustomer(Customer customer) {
        return all().stream().filter(booking -> booking.customer().id().equals(customer.id())).toList();
    }
}

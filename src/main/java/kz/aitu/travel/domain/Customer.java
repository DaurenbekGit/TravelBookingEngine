// Owner: Abylay
package kz.aitu.travel.domain;

/** Customer identity is its stable id; contact details are immutable. */
public record Customer(String id, String name, String email, String phone) {
    public Customer {
        if (id == null || id.isBlank() || name == null || name.isBlank()
                || email == null || email.isBlank() || phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Customer id, name, email and phone are required");
        }
    }
}

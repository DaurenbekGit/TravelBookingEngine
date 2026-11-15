# Travel Booking Engine

A Java 17 console application for a three-member Design Patterns final project.
Search simulated flights, hotels and rental cars; compare offers; build bookings with
paid extras; confirm, pay or cancel; watch prices and receive simulated notifications.
All prices are KZT. Supported cities: Astana (NQZ), Almaty (ALA), Shymkent (CIT),
Istanbul (IST), Dubai (DXB). There is no database, network call or real payment.

## Build and run

Install a **JDK 17 or 21** and Maven 3.9+, then run from this directory:

```sh
mvn clean test
mvn clean package
java -jar target/travel-booking-engine.jar --demo
java -jar target/travel-booking-engine.jar --demo --lang=ru
java -jar target/travel-booking-engine.jar --lang=en
java -jar target/travel-booking-engine.jar --lang=ru
```

Only JUnit 5 is a dependency, and only in test scope. A first Maven build needs
internet access to Maven Central for plugins and JUnit. The application runs offline.
The compiler uses `--release 17`, `-Xlint:all` and `-Werror`.

## Team and patterns

| Member | Module | Creational | Structural | Behavioral |
|---|---|---|---|---|
| Daurenbek | Search & Offers | Factory Method | Adapter | Visitor |
| Nurkhat | Booking & Ranking | Builder | Decorator | Strategy |
| Abylay | Platform & Integration | Abstract Factory | Facade | Observer |

| Pattern | Type | Member | Key classes |
|---|---|---|---|
| Factory Method | Creational | Daurenbek | OfferFactory; FlightOfferFactory, HotelOfferFactory, CarRentalOfferFactory; TravelOffer products |
| Adapter | Structural | Daurenbek | SupplierClient; SkyJetFlightAdapter, GlobalStayHotelAdapter, DriveNowCarAdapter; three APIs |
| Strategy | Behavioral | Nurkhat | OfferRankingStrategy; four strategies; OfferSearchService |
| Builder | Creational | Nurkhat | BookingBuilder, Booking |
| Decorator | Structural | Nurkhat | BookingItem, OfferBookingItem, BookingItemDecorator; four extras |
| Visitor | Behavioral | Daurenbek | OfferVisitor; TaxCalculationVisitor, CancellationFeeVisitor, OfferDetailsVisitor; TravelOffer elements |
| Abstract Factory | Creational | Abylay | NotificationChannelFactory; LocalNotificationFactory, InternationalNotificationFactory; English/Russian Email/Sms products |
| Facade | Structural | Abylay | TravelBookingFacade; ConsoleApp and DemoScenario clients |
| Observer | Behavioral | Abylay | EventPublisher, NotificationCenter, TravelEventListener; Email, Sms and Audit observers |

## Architecture

`Main` delegates wiring to `AppContext`. The CLI calls `TravelBookingFacade` for all
application operations. Search uses supplier targets, injected abstract creators and
ranking strategies. BookingService creates decorated items with BookingBuilder,
validates lifecycle changes through BookingStatus, saves through BookingRepository and
publishes through EventPublisher. PriceWatchService replaces immutable catalog offers
and publishes only genuine drops. All service construction is in AppContext.
Product creation, builders and decorator wrappers are intentionally
created by their pattern participants, not treated as injected long-lived services.

There is no cycle between services: the repository interface references Booking, but
Booking does not depend on that interface. Offer elements and the OfferVisitor contract
compile as one logical group because double dispatch requires mutual type references
within the Search & Offers module.

## Search & Offers

SkyJet returns semicolon-separated flight strings. GlobalStay accepts city names and
quotes USD per night. DriveNow supplies a map of vehicle DTOs with tiyn per day.
Adapters convert these to typed OfferSpec records and ask an injected OfferFactory to
create the correct immutable product. SupplierRates contains the one fixed USD rate:
1 USD = 500 KZT. This is a simulation assumption, not a current exchange-rate claim.
Hotels and cars are priced for the entire requested stay/rental.

OfferVisitor adds operations to the stable offer hierarchy using double dispatch:
`offer.accept(visitor)` calls the matching `visitFlight`, `visitHotel` or `visitCarRental`.
TaxCalculationVisitor shows a fixed 2 500 KZT airport fee, a 1 000 KZT/night hotel tax,
or 12% rental VAT. These are illustrative teaching rules, not actual legal tax rates.
OfferDetailsVisitor supplies type-specific details to the existing CLI screen.
CancellationFeeVisitor quotes 10% of flight base price, one hotel night's base price,
or 3 000 KZT for a rental. Money handles rounding to tiyn. Taxes are shown separately
and never change Booking.totalPrice().

## Booking & Ranking

The builder requires a customer and at least one item, rejects duplicate offer ids,
and copies its item list. Items keep immutable quote snapshots. Later price drops
change the catalog, never existing booking totals.

Extras are applied in the order selected. Baggage adds 8 000 KZT, breakfast adds
5 000 KZT per hotel night, insurance adds 5% of its wrapped subtotal, and flexibility
adds 7 000 KZT. For a 45 000 KZT flight, baggage -> insurance -> flexibility gives
`(45000 + 8000) * 1.05 + 7000 = 62650 KZT`. Because insurance uses the current subtotal,
changing extra order can change the price. Duplicate extras are rejected by the service.

Allowed transitions: Pending -> Confirmed or Cancelled; Confirmed -> Paid or Cancelled;
Paid -> Cancelled with a simulated refund minus cancellation fees. Cancelled is terminal.
BookingStatus stores the allowed transitions in an immutable map; this is ordinary
domain validation, not an additional design pattern. Cancellation visits each booked
offer snapshot, caps the aggregate fee at the decorated total, and reports both fee
and refund. Unpaid bookings quote the fee but refund zero because no money was paid.
The exercise
requires cancellation even without the flexibility extra, so flexibility is a labelled
paid add-on and does not gate the transition or waive the illustrative fee. No real refund occurs.
Strategies return fresh lists. Cheapest sorts by price; fastest by duration then
price; top-rated by descending rating then price; best-value by descending
`rating * 10000 / priceKzt`, then rating and price. Free offers rank first, with rating
breaking ties. IDs break remaining ties for deterministic output. The EnumMap in
AppContext makes ranking selectable without type switches. Search merges all matching
suppliers; production wires one supplier per type, while tests also merge two flight
suppliers. Catalog prices persist across repeated searches so a simulated price drop
is not overwritten by the static supplier fixture.

## Platform & Integration

InMemoryBookingRepository is an ordinary class with a public constructor. AppContext
creates one store per application graph and injects it through BookingRepository.
Separate contexts have independent data and id sequences; tests need no global reset.

NotificationChannelFactory creates matching email/SMS products. Its local family uses
Russian text and Kazakhstan phone formatting; its international family uses English.
AppContext selects the family with `--lang=ru` or `--lang=en` (default). The CLI remains
English; this option controls notification channels. The demo also replays its last
audited event through both families, without adding duplicate audit entries.
AuditLogListener remains independent of either family.

## Interactive walkthrough

The default customer is Demo Traveller. Menu 1 asks for type, origin, destination,
date, days and ranking. Example: `1`, `1` (flight), `ALA`, `NQZ`, `2026-12-20`, `1`,
`1` (cheapest). Copy an offer id from the table into menu 2 or 3. Menu 3 accepts
comma-separated ids and extra names such as `BAGGAGE,INSURANCE,FLEXIBLE`.
Use menus 4/5/6 to confirm/pay/cancel, 7 for bookings, 8 to watch, 9 to simulate a
drop and 10 for audit history. Invalid inputs show a message and return to the menu;
end-of-input exits safely. Flight origin is ignored for hotel/car searches.

## Demo and evidence

```text
=== [Builder] Assemble a multi-item booking ===
=== [Decorator] Stack baggage, insurance and flexibility; add breakfast ===
BK-1001 | Demo Traveller | PENDING | 168 650 KZT
=== [Visitor] Type-specific details, taxes and cancellation fees ===
Illustrative taxes (display only): 2 500 KZT
=== [Observer] Email, SMS and audit listen to successful status changes ===
Handled: Cannot pay booking BK-1001: booking is PENDING, confirm it first
Cancelled; cancellation fee 37 500 KZT; refund 131 150 KZT (simulation)
Catalog now: 40 500 KZT; booking snapshot: 45 000 KZT
=== [Abstract Factory] Same event via Local (RU) and International (EN) notification families ===
Audit events: 4
=== Demo complete ===
```

`docs/uml/` has five PlantUML source diagrams and rendered SVG versions. Use a PlantUML
extension or `java -jar plantuml.jar -tsvg docs/uml/*.puml` to render them again.
The renderer is a documentation tool, not a project dependency.

## Testing

Tests use plain in-test fakes, no mocking library. Booking tests get a fresh private
repository. Platform tests create normal repositories and check save, lookup, customer
filtering, missing ids and context isolation. All 12 status/action combinations are
covered, plus conversions, ranking ties, stacking, watchers and facade integration.

## Scope

In-memory, single-process teaching application. Supplier fixtures use synthetic
availability and fixed illustrative prices/durations for all supported routes/dates.
Observers run synchronously and notification output is simulated. Repository storage
and id allocation are thread-safe; this is not a distributed reservation system.
The six Java test files group each member's pattern tests; there are no extra
frameworks or production dependencies.

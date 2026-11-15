<!-- Owner: Member C; marked module sections may later be added by their owners. -->
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
| Member A | Search & Offers | Factory Method | Adapter | Visitor |
| Member B | Booking & Ranking | Builder | Decorator | Strategy |
| Member C | Platform & Integration | Abstract Factory | Facade | Observer |

| Pattern | Type | Member | Key classes |
|---|---|---|---|
| Factory Method | Creational | A | OfferFactory; FlightOfferFactory, HotelOfferFactory, CarRentalOfferFactory; TravelOffer products |
| Adapter | Structural | A | SupplierClient; SkyJetFlightAdapter, GlobalStayHotelAdapter, DriveNowCarAdapter; three APIs |
| Strategy | Behavioral | B | OfferRankingStrategy; four strategies; OfferSearchService |
| Builder | Creational | B | BookingBuilder, Booking |
| Decorator | Structural | B | BookingItem, OfferBookingItem, BookingItemDecorator; four extras |
| Visitor | Behavioral | A | OfferVisitor; TaxCalculationVisitor, CancellationFeeVisitor, OfferDetailsVisitor; TravelOffer elements |
| Abstract Factory | Creational | C | NotificationChannelFactory; LocalNotificationFactory, InternationalNotificationFactory; English/Russian Email/Sms products |
| Facade | Structural | C | TravelBookingFacade; ConsoleApp and DemoScenario clients |
| Observer | Behavioral | C | EventPublisher, NotificationCenter, TravelEventListener; Email, Sms and Audit observers |

<!-- BEGIN MEMBER A SECTION: Search & Offers -->
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
<!-- END MEMBER A SECTION -->


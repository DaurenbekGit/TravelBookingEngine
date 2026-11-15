# Travel Booking Engine

A Java 17 console application for booking trips. You can search for flights, hotels
and rental cars, compare offers, book them with extras, and get notifications.

This is the final project for the Design Patterns course. It uses **9 design patterns**:
3 creational, 3 structural and 3 behavioral.

> All data is simulated: there is no database, no real payment and no internet calls.
> Prices are in KZT. Cities: Astana (NQZ), Almaty (ALA), Shymkent (CIT), Istanbul (IST), Dubai (DXB).

## Features

- Search flights, hotels and rental cars from 3 different suppliers
- Sort offers: cheapest, fastest, top rated or best value
- Book several offers at once and add extras (baggage, breakfast, insurance, flexible cancellation)
- Confirm, pay for or cancel a booking (with a cancellation fee and refund)
- Watch a price and get a notification when it drops
- Email and SMS notifications in English or Russian

## Team

| Member | Module | Creational | Structural | Behavioral |
|---|---|---|---|---|
| Daurenbek | Search & Offers | Factory Method | Adapter | Visitor |
| Nurkhat | Booking & Ranking | Builder | Decorator | Strategy |
| Abylay | Platform & Integration | Abstract Factory | Facade | Observer |

## Design patterns

| Pattern | Type | Where | What it does here |
|---|---|---|---|
| Factory Method | Creational | `OfferFactory` + 3 subclasses | Creates the right offer type: flight, hotel or car |
| Adapter | Structural | `SkyJetFlightAdapter`, `GlobalStayHotelAdapter`, `DriveNowCarAdapter` | Converts 3 different supplier APIs into one common interface `SupplierClient` |
| Visitor | Behavioral | `OfferVisitor` + 3 visitors | Calculates taxes, cancellation fees and details differently for each offer type |
| Builder | Creational | `BookingBuilder` | Builds a booking step by step and checks it is valid |
| Decorator | Structural | `BookingItemDecorator` + 4 extras | Adds extras to a booking item and increases its price |
| Strategy | Behavioral | `OfferRankingStrategy` + 4 strategies | Lets the user change how offers are sorted |
| Abstract Factory | Creational | `NotificationChannelFactory` + 2 factories | Creates a matching set of email + SMS notifiers (Russian or English) |
| Facade | Structural | `TravelBookingFacade` | One simple entry point for the whole system |
| Observer | Behavioral | `NotificationCenter`, `TravelEventListener` | Sends notifications when a booking status changes or a price drops |

## How to run

You need **JDK 17+** and **Maven 3.9+**.

```sh
mvn clean test        # run all tests
mvn clean package     # build the jar

java -jar target/travel-booking-engine.jar --demo            # demo of all 9 patterns
java -jar target/travel-booking-engine.jar                   # interactive menu
java -jar target/travel-booking-engine.jar --lang=ru         # notifications in Russian
```

The `--demo` mode runs a full scenario and prints one section for each pattern,
for example `=== [Adapter] Searching 3 incompatible supplier APIs ===`.

## How to use the menu

| Key | Action |
|---|---|
| 1 | Search: choose type (flight / hotel / car), cities, date and sorting |
| 2 | Offer details, taxes and available extras (paste an offer id) |
| 3 | Book: paste one or more offer ids and extras, e.g. `BAGGAGE,INSURANCE` |
| 4 / 5 / 6 | Confirm / Pay / Cancel a booking |
| 7 | My bookings |
| 8 / 9 | Watch a price / Simulate a price drop |
| 10 | Notification history |
| 0 | Exit |

Extras: `BAGGAGE` (flights), `BREAKFAST` (hotels), `INSURANCE` and `FLEXIBLE` (any offer).
Wrong input shows a message and returns to the menu.

## Project structure

```text
src/main/java/kz/aitu/travel/
├── domain/        Money, City, Customer, SearchCriteria
├── offer/         offers, factories (Factory Method), visitors (Visitor)   — Daurenbek
├── supplier/      supplier APIs and adapters (Adapter)                     — Daurenbek
├── search/        OfferSearchService                                       — Daurenbek
├── ranking/       sorting strategies (Strategy)                            — Nurkhat
├── booking/       Booking, BookingBuilder (Builder), extras (Decorator)    — Nurkhat
├── repository/    booking storage and offer catalog                        — Abylay
├── notification/  events, listeners (Observer), factories (Abstract Factory) — Abylay
├── pricewatch/    PriceWatchService                                        — Abylay
├── facade/        TravelBookingFacade (Facade)                             — Abylay
├── config/        AppContext — creates and connects all objects            — Abylay
└── cli/           console menu and demo                                    — Abylay
```

UML diagrams are in `docs/uml/`: one class diagram per member, an overview diagram
and a booking sequence diagram.

## Testing

**152 JUnit 5 tests** cover every pattern: adapters and currency conversion, factories,
visitors, sorting, builder checks, extra prices, all booking status changes,
notifications, price watching and the full flow through the facade.

## Business rules (simulation)

- **Exchange rate:** 1 USD = 500 KZT
- **Taxes:** flight 2 500 KZT, hotel 1 000 KZT per night, car 12% (shown only, not added to the price)
- **Cancellation fee:** flight 10%, hotel one night, car 3 000 KZT
- **Extras:** baggage 8 000 KZT, breakfast 5 000 KZT per night, insurance 5%, flexible cancellation 7 000 KZT
- **Booking status:** Pending → Confirmed → Paid. A booking can be cancelled at any of these stages; after payment the refund is the price minus the cancellation fee.

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

## Project setup

This repository is being assembled step by step. The initial Maven configuration and repository ignore rules are provided. More modules and documentation are added in later steps.

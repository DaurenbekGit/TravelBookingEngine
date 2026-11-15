// Owner: Abylay
package kz.aitu.travel.config;

import java.io.InputStream;
import java.io.PrintStream;
import java.time.Clock;
import java.util.EnumMap;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import kz.aitu.travel.notification.event.TravelEvent;
import kz.aitu.travel.notification.factory.NotificationChannelFactory;
import kz.aitu.travel.notification.factory.LocalNotificationFactory;
import kz.aitu.travel.notification.factory.InternationalNotificationFactory;
import kz.aitu.travel.offer.visitor.CancellationFeeVisitor;
import kz.aitu.travel.offer.visitor.TaxCalculationVisitor;
import kz.aitu.travel.offer.visitor.OfferDetailsVisitor;
import java.util.List;
import java.util.Scanner;
import kz.aitu.travel.booking.BookingService;
import kz.aitu.travel.cli.ConsoleApp;
import kz.aitu.travel.cli.ConsoleView;
import kz.aitu.travel.cli.DemoScenario;
import kz.aitu.travel.domain.Customer;
import kz.aitu.travel.facade.TravelBookingFacade;
import kz.aitu.travel.notification.NotificationCenter;
import kz.aitu.travel.notification.listener.AuditLogListener;
import kz.aitu.travel.offer.factory.CarRentalOfferFactory;
import kz.aitu.travel.offer.factory.FlightOfferFactory;
import kz.aitu.travel.offer.factory.HotelOfferFactory;
import kz.aitu.travel.pricewatch.PriceWatchService;
import kz.aitu.travel.ranking.BestValueStrategy;
import kz.aitu.travel.ranking.CheapestFirstStrategy;
import kz.aitu.travel.ranking.FastestFirstStrategy;
import kz.aitu.travel.ranking.OfferRankingStrategy;
import kz.aitu.travel.ranking.RankingType;
import kz.aitu.travel.ranking.TopRatedStrategy;
import kz.aitu.travel.repository.InMemoryBookingRepository;
import kz.aitu.travel.repository.OfferCatalog;
import kz.aitu.travel.search.OfferSearchService;
import kz.aitu.travel.supplier.SupplierClient;
import kz.aitu.travel.supplier.adapter.DriveNowCarAdapter;
import kz.aitu.travel.supplier.adapter.GlobalStayHotelAdapter;
import kz.aitu.travel.supplier.adapter.SkyJetFlightAdapter;
import kz.aitu.travel.supplier.external.DriveNowApi;
import kz.aitu.travel.supplier.external.GlobalStayApi;
import kz.aitu.travel.supplier.external.SkyJetApi;

/** Composition root: the only production location wiring service dependencies. */
public final class AppContext {
    private AppContext() { }
    public static TravelBookingFacade createFacade(PrintStream output) { return createFacade(output, "en"); }
    public static TravelBookingFacade createFacade(PrintStream output, String language) {
        var factory = notificationFactory(language);
        var catalog = new OfferCatalog();
        var events = new NotificationCenter(output::println);
        var audit = new AuditLogListener();
        events.subscribe(factory.createEmailListener(output::println));
        events.subscribe(factory.createSmsListener(output::println));
        events.subscribe(audit);
        var search = new OfferSearchService(suppliers(), strategies(), catalog);
        var repository = new InMemoryBookingRepository();
        var bookings = new BookingService(repository, events, Clock.systemUTC(), new CancellationFeeVisitor());
        var watches = new PriceWatchService(catalog, events);
        return new TravelBookingFacade(search, bookings, watches, catalog, events, audit,
                new TaxCalculationVisitor(), new OfferDetailsVisitor(), previewChannels(output));
    }
    private static NotificationChannelFactory notificationFactory(String language) {
        Map<String, Supplier<NotificationChannelFactory>> factories = Map.of(
                "en", InternationalNotificationFactory::new, "ru", LocalNotificationFactory::new);
        var selected = factories.get(language);
        if (selected == null) throw new IllegalArgumentException("Language must be en or ru");
        return selected.get();
    }
    private static Consumer<TravelEvent> previewChannels(PrintStream output) {
        var local = notificationFactory("ru");
        var international = notificationFactory("en");
        var listeners = List.of(local.createEmailListener(output::println), local.createSmsListener(output::println),
                international.createEmailListener(output::println), international.createSmsListener(output::println));
        return event -> listeners.forEach(listener -> listener.onEvent(event));
    }
    private static List<SupplierClient> suppliers() {
        return List.of(new SkyJetFlightAdapter(new SkyJetApi(), new FlightOfferFactory()),
                new GlobalStayHotelAdapter(new GlobalStayApi(), new HotelOfferFactory()),
                new DriveNowCarAdapter(new DriveNowApi(), new CarRentalOfferFactory()));
    }
    private static EnumMap<RankingType, OfferRankingStrategy> strategies() {
        var strategies = new EnumMap<RankingType, OfferRankingStrategy>(RankingType.class);
        strategies.put(RankingType.CHEAPEST, new CheapestFirstStrategy());
        strategies.put(RankingType.FASTEST, new FastestFirstStrategy());
        strategies.put(RankingType.TOP_RATED, new TopRatedStrategy());
        strategies.put(RankingType.BEST_VALUE, new BestValueStrategy());
        return strategies;
    }
    public static void run(String[] args, InputStream input, PrintStream output) {
        boolean demo = Arrays.asList(args).contains("--demo");
        String language = Arrays.stream(args).filter(arg -> arg.startsWith("--lang="))
                .map(arg -> arg.substring("--lang=".length())).findFirst().orElse("en");
        run(demo, language, input, output);
    }
    public static void run(boolean demo, InputStream input, PrintStream output) { run(demo, "en", input, output); }
    private static void run(boolean demo, String language, InputStream input, PrintStream output) {
        var facade = createFacade(output, language);
        var customer = new Customer("demo-customer", "Demo Traveller", "traveller@example.test", "+77000000000");
        var view = new ConsoleView(output);
        if (demo) new DemoScenario(facade, customer, view).run();
        else new ConsoleApp(facade, customer, new Scanner(input), view).run();
    }
}

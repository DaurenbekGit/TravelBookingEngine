// Owner: Abylay
package kz.aitu.travel.notification;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.stream.Stream;
import kz.aitu.travel.booking.BookingStatus;
import kz.aitu.travel.config.AppContext;
import kz.aitu.travel.domain.Customer;
import kz.aitu.travel.domain.Money;
import kz.aitu.travel.notification.event.BookingStatusChangedEvent;
import kz.aitu.travel.notification.event.PriceDropEvent;
import kz.aitu.travel.notification.factory.InternationalNotificationFactory;
import kz.aitu.travel.notification.factory.LocalNotificationFactory;
import kz.aitu.travel.notification.factory.NotificationChannelFactory;
import kz.aitu.travel.notification.listener.EnglishEmailNotificationListener;
import kz.aitu.travel.notification.listener.EnglishSmsNotificationListener;
import kz.aitu.travel.notification.listener.RussianEmailNotificationListener;
import kz.aitu.travel.notification.listener.RussianSmsNotificationListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class NotificationFactoryTest {
    private final Customer customer = new Customer("factory", "Traveller", "f@example.test", "87011234567");
    static Stream<Arguments> families() {
        return Stream.of(Arguments.of(new LocalNotificationFactory(), "Снижение цены", "сбор за отмену", "возврат"),
                Arguments.of(new InternationalNotificationFactory(), "Price drop", "cancellation fee", "refund"));
    }
    @Test void localFactoryReturnsMatchingRussianProducts() {
        var factory = new LocalNotificationFactory();
        assertInstanceOf(RussianEmailNotificationListener.class, factory.createEmailListener(s -> { }));
        assertInstanceOf(RussianSmsNotificationListener.class, factory.createSmsListener(s -> { }));
    }
    @Test void internationalFactoryReturnsMatchingEnglishProducts() {
        var factory = new InternationalNotificationFactory();
        assertInstanceOf(EnglishEmailNotificationListener.class, factory.createEmailListener(s -> { }));
        assertInstanceOf(EnglishSmsNotificationListener.class, factory.createSmsListener(s -> { }));
    }
    @ParameterizedTest @MethodSource("families")
    void bothChannelsUseFamilyLanguageForPriceDrops(NotificationChannelFactory factory, String priceText,
                                                    String feeText, String refundText) {
        var messages = new ArrayList<String>();
        var event = new PriceDropEvent(customer, "f", Money.of("45000"), Money.of("40500"));
        factory.createEmailListener(messages::add).onEvent(event);
        factory.createSmsListener(messages::add).onEvent(event);
        assertEquals(2, messages.size());
        assertTrue(messages.stream().allMatch(message -> message.contains(priceText)));
    }
    @ParameterizedTest @MethodSource("families")
    void bothChannelsIncludeCancellationFeeAndRefund(NotificationChannelFactory factory, String priceText,
                                                     String feeText, String refundText) {
        var messages = new ArrayList<String>();
        var event = new BookingStatusChangedEvent(customer, "BK-test", BookingStatus.PAID, BookingStatus.CANCELLED,
                "Cancelled; cancellation fee 4 500 KZT; refund 40 500 KZT (simulation)",
                Money.of("4500"), Money.of("40500"));
        factory.createEmailListener(messages::add).onEvent(event);
        factory.createSmsListener(messages::add).onEvent(event);
        assertTrue(messages.stream().allMatch(message -> message.contains(feeText) && message.contains(refundText)
                && message.contains("4 500 KZT") && message.contains("40 500 KZT")));
    }
    @Test void localSmsFormatsKazakhstanPhoneNumber() {
        var messages = new ArrayList<String>();
        new LocalNotificationFactory().createSmsListener(messages::add).onEvent(
                new PriceDropEvent(customer, "f", Money.of("2"), Money.of("1")));
        assertTrue(messages.get(0).contains("+7 (701) 123-45-67"));
    }
    @ParameterizedTest @ValueSource(strings = {"en", "ru"})
    void argumentSelectsNormalFamilyAndDemoShowsBoth(String language) {
        var bytes = new ByteArrayOutputStream();
        AppContext.run(new String[]{"--lang=" + language, "--demo"}, java.io.InputStream.nullInputStream(),
                new PrintStream(bytes));
        String text = bytes.toString(java.nio.charset.StandardCharsets.UTF_8);
        String normal = text.substring(0, text.indexOf("=== [Abstract Factory]"));
        assertTrue(normal.contains(language.equals("ru") ? "[ЭЛ.ПОЧТА" : "[EMAIL"));
        String preview = text.substring(text.indexOf("=== [Abstract Factory]"));
        assertTrue(preview.contains("[ЭЛ.ПОЧТА") && preview.contains("[СМС"));
        assertTrue(preview.contains("[EMAIL") && preview.contains("[SMS"));
        assertTrue(preview.contains("Audit events: 4"));
    }
    @Test void unsupportedLanguageFailsClearly() {
        var output = new PrintStream(new ByteArrayOutputStream());
        assertEquals("Language must be en or ru", assertThrows(IllegalArgumentException.class,
                () -> AppContext.createFacade(output, "unknown")).getMessage());
    }
}

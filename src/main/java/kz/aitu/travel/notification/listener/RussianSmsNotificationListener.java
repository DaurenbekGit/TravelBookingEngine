// Owner: Member C
package kz.aitu.travel.notification.listener;

import java.util.function.Consumer;
import kz.aitu.travel.notification.event.TravelEvent;

/** Abstract Factory (Creational): Russian Sms product; Observer (Behavioral): ConcreteObserver. */
public final class RussianSmsNotificationListener extends SmsNotificationListener {
    public RussianSmsNotificationListener(Consumer<String> output) { super(output); }
    @Override protected String label() { return "СМС на"; }
    @Override protected String message(TravelEvent event) { return event.russianMessage(); }

    /** Format Kazakhstan's eleven-digit 7/8-prefixed numbers; preserve other addresses. */
    @Override protected String recipient(TravelEvent event) {
        String phone = event.customer().phone();
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.length() == 11 && (digits.startsWith("7") || digits.startsWith("8"))) {
            return "+7 (" + digits.substring(1, 4) + ") " + digits.substring(4, 7)
                    + "-" + digits.substring(7, 9) + "-" + digits.substring(9);
        }
        return phone;
    }
}

// Owner: Member C
package kz.aitu.travel.notification.listener;

import java.util.function.Consumer;
import kz.aitu.travel.notification.event.TravelEvent;

/** Abstract Factory (Creational): English Sms product; Observer (Behavioral): ConcreteObserver. */
public final class EnglishSmsNotificationListener extends SmsNotificationListener {
    public EnglishSmsNotificationListener(Consumer<String> output) { super(output); }
    @Override protected String label() { return "SMS to"; }
    @Override protected String message(TravelEvent event) { return event.message(); }
}

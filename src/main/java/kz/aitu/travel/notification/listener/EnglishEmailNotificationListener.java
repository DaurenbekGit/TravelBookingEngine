// Owner: Abylay
package kz.aitu.travel.notification.listener;

import java.util.function.Consumer;
import kz.aitu.travel.notification.event.TravelEvent;

/** Abstract Factory (Creational): English Email product; Observer (Behavioral): ConcreteObserver. */
public final class EnglishEmailNotificationListener extends EmailNotificationListener {
    public EnglishEmailNotificationListener(Consumer<String> output) { super(output); }
    @Override protected String label() { return "EMAIL to"; }
    @Override protected String message(TravelEvent event) { return event.message(); }
}

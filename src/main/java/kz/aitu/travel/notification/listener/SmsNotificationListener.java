// Owner: Abylay
package kz.aitu.travel.notification.listener;

import java.util.function.Consumer;
import kz.aitu.travel.notification.TravelEventListener;
import kz.aitu.travel.notification.event.TravelEvent;

/** Abstract Factory (Creational): abstract Sms product; Observer (Behavioral): subscriber base. */
public abstract class SmsNotificationListener implements TravelEventListener {
    private final Consumer<String> output;
    protected SmsNotificationListener(Consumer<String> output) { this.output = output; }
    @Override public final void onEvent(TravelEvent event) {
        output.accept("[" + label() + " " + recipient(event) + "] " + message(event));
    }
    protected String recipient(TravelEvent event) { return event.customer().phone(); }
    protected abstract String label();
    protected abstract String message(TravelEvent event);
}

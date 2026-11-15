// Owner: Abylay
package kz.aitu.travel.notification.listener;

import java.util.function.Consumer;
import kz.aitu.travel.notification.TravelEventListener;
import kz.aitu.travel.notification.event.TravelEvent;

/** Abstract Factory (Creational): abstract Email product; Observer (Behavioral): subscriber base. */
public abstract class EmailNotificationListener implements TravelEventListener {
    private final Consumer<String> output;
    protected EmailNotificationListener(Consumer<String> output) { this.output = output; }
    @Override public final void onEvent(TravelEvent event) {
        output.accept("[" + label() + " " + recipient(event) + "] " + message(event));
    }
    protected String recipient(TravelEvent event) { return event.customer().email(); }
    protected abstract String label();
    protected abstract String message(TravelEvent event);
}

// Owner: Abylay
package kz.aitu.travel.notification.listener;

import java.util.function.Consumer;
import kz.aitu.travel.notification.event.TravelEvent;

/** Abstract Factory (Creational): Russian Email product; Observer (Behavioral): ConcreteObserver. */
public final class RussianEmailNotificationListener extends EmailNotificationListener {
    public RussianEmailNotificationListener(Consumer<String> output) { super(output); }
    @Override protected String label() { return "ЭЛ.ПОЧТА кому"; }
    @Override protected String message(TravelEvent event) { return event.russianMessage(); }
}

// Owner: Abylay
package kz.aitu.travel.notification;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import kz.aitu.travel.notification.event.TravelEvent;

/** Observer (Behavioral): ConcreteSubject; a broken listener cannot suppress the others. */
public final class NotificationCenter implements EventPublisher {
    private final CopyOnWriteArrayList<TravelEventListener> listeners = new CopyOnWriteArrayList<>();
    private final Consumer<String> errors;
    public NotificationCenter(Consumer<String> errors) { this.errors = errors; }
    @Override public void subscribe(TravelEventListener listener) { listeners.addIfAbsent(Objects.requireNonNull(listener)); }
    @Override public void unsubscribe(TravelEventListener listener) { listeners.remove(listener); }
    @Override public void publish(TravelEvent event) {
        Objects.requireNonNull(event);
        for (TravelEventListener listener : listeners) {
            try { listener.onEvent(event); }
            catch (RuntimeException exception) { errors.accept("Notification failed: " + exception.getMessage()); }
        }
    }
}

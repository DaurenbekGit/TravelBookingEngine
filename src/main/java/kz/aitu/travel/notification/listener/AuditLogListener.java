// Owner: Abylay
package kz.aitu.travel.notification.listener;

import java.util.ArrayList;
import java.util.List;
import kz.aitu.travel.notification.TravelEventListener;
import kz.aitu.travel.notification.event.TravelEvent;

/** Observer (Behavioral): ConcreteObserver retains an immutable-view audit history. */
public final class AuditLogListener implements TravelEventListener {
    private final List<TravelEvent> events = new ArrayList<>();
    @Override public synchronized void onEvent(TravelEvent event) { events.add(event); }
    public synchronized List<TravelEvent> events() { return List.copyOf(events); }
}

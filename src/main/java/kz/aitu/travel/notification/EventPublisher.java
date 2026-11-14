// Owner: Member C
package kz.aitu.travel.notification;

import kz.aitu.travel.notification.event.TravelEvent;

/** Observer (Behavioral): Subject interface injected into producers. */
public interface EventPublisher {
    void subscribe(TravelEventListener listener);
    void unsubscribe(TravelEventListener listener);
    void publish(TravelEvent event);
}

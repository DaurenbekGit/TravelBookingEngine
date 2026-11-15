// Owner: Abylay
package kz.aitu.travel.notification;

import kz.aitu.travel.notification.event.TravelEvent;

/** Observer (Behavioral): small subscriber contract. */
@FunctionalInterface
public interface TravelEventListener { void onEvent(TravelEvent event); }

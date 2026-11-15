// Owner: Abylay
package kz.aitu.travel.notification.factory;

import java.util.function.Consumer;
import kz.aitu.travel.notification.listener.EmailNotificationListener;
import kz.aitu.travel.notification.listener.SmsNotificationListener;

/** Abstract Factory (Creational): creates a matching family of notification channels. */
public interface NotificationChannelFactory {
    EmailNotificationListener createEmailListener(Consumer<String> output);
    SmsNotificationListener createSmsListener(Consumer<String> output);
}

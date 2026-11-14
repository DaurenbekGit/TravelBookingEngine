// Owner: Member C
package kz.aitu.travel.notification.factory;

import java.util.function.Consumer;
import kz.aitu.travel.notification.listener.EmailNotificationListener;
import kz.aitu.travel.notification.listener.SmsNotificationListener;
import kz.aitu.travel.notification.listener.RussianEmailNotificationListener;
import kz.aitu.travel.notification.listener.RussianSmsNotificationListener;

/** Abstract Factory (Creational): ConcreteFactory for the Russian channel family. */
public final class LocalNotificationFactory implements NotificationChannelFactory {
    @Override public EmailNotificationListener createEmailListener(Consumer<String> output) {
        return new RussianEmailNotificationListener(output);
    }
    @Override public SmsNotificationListener createSmsListener(Consumer<String> output) {
        return new RussianSmsNotificationListener(output);
    }
}

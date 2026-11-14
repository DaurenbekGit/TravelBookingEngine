// Owner: Member C
package kz.aitu.travel.notification.factory;

import java.util.function.Consumer;
import kz.aitu.travel.notification.listener.EmailNotificationListener;
import kz.aitu.travel.notification.listener.SmsNotificationListener;
import kz.aitu.travel.notification.listener.EnglishEmailNotificationListener;
import kz.aitu.travel.notification.listener.EnglishSmsNotificationListener;

/** Abstract Factory (Creational): ConcreteFactory for the English channel family. */
public final class InternationalNotificationFactory implements NotificationChannelFactory {
    @Override public EmailNotificationListener createEmailListener(Consumer<String> output) {
        return new EnglishEmailNotificationListener(output);
    }
    @Override public SmsNotificationListener createSmsListener(Consumer<String> output) {
        return new EnglishSmsNotificationListener(output);
    }
}

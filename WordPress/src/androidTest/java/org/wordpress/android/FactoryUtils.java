package org.sitebay.android;

import org.sitebay.android.mocks.RestClientFactoryTest;
import org.sitebay.android.mocks.SystemServiceFactoryTest;
import org.sitebay.android.networking.RestClientFactory;
import org.sitebay.android.util.AppLog;
import org.sitebay.android.util.AppLog.T;
import org.sitebay.android.util.SystemServiceFactory;

import java.lang.reflect.Field;

public class FactoryUtils {
    public static void clearFactories() {
        // clear factories
        forceFactoryInjection(RestClientFactory.class, null);
        forceFactoryInjection(SystemServiceFactory.class, null);
        AppLog.v(T.TESTS, "Null factories set");
    }

    public static void initWithTestFactories() {
        // create test factories
        forceFactoryInjection(RestClientFactory.class, new RestClientFactoryTest());
        forceFactoryInjection(SystemServiceFactory.class, new SystemServiceFactoryTest());
        AppLog.v(T.TESTS, "Mocks factories instantiated");
    }

    private static void forceFactoryInjection(Class klass, Object factory) {
        try {
            Field field = klass.getDeclaredField("sFactory");
            field.setAccessible(true);
            field.set(null, factory);
            AppLog.v(T.TESTS, "Factory " + klass + " injected");
        } catch (Exception e) {
            AppLog.e(T.TESTS, "Can't inject test factory " + klass);
        }
    }
}

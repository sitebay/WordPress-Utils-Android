package org.sitebay.android.util;

import android.content.Context;

import org.sitebay.android.Constants;

import java.net.URI;
import java.net.URL;

public class WPUrlUtils {
    public static boolean safeToAddWordPressComAuthToken(String url) {
        return UrlUtils.isHttps(url) && isWordPressCom(url);
    }

    public static boolean safeToAddWordPressComAuthToken(URL url) {
        return UrlUtils.isHttps(url) && isWordPressCom(url);
    }

    public static boolean safeToAddWordPressComAuthToken(URI uri) {
        return UrlUtils.isHttps(uri) && isWordPressCom(uri);
    }

    public static boolean safeToAddPrivateAtCookie(String url, String cookieHost) {
        return UrlUtils.getHost(url).equals(cookieHost);
    }

    public static boolean isWordPressCom(String url) {
        return UrlUtils.getHost(url).endsWith(".sitebay.com") || UrlUtils.getHost(url).equals("sitebay.com");
    }

    public static boolean isWordPressCom(URL url) {
        if (url == null) {
            return false;
        }
        return url.getHost().endsWith(".sitebay.com") || url.getHost().equals("sitebay.com");
    }

    public static boolean isWordPressCom(URI uri) {
        if (uri == null || uri.getHost() == null) {
            return false;
        }
        return uri.getHost().endsWith(".sitebay.com") || uri.getHost().equals("sitebay.com");
    }

    public static boolean isGravatar(URL url) {
        if (url == null) {
            return false;
        }
        return url.getHost().equals("gravatar.com") || url.getHost().endsWith(".gravatar.com");
    }

    public static String buildTermsOfServiceUrl(Context context) {
        return Constants.URL_TOS + "?locale=" + LanguageUtils.getPatchedCurrentDeviceLanguage(context);
    }
}

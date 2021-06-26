package org.sitebay.android.modules;

import com.android.volley.toolbox.ImageLoader.ImageCache;

import org.sitebay.android.WordPress;
import org.sitebay.android.fluxc.network.rest.wpcom.auth.AccessToken;
import org.sitebay.android.networking.OAuthAuthenticator;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class LegacyModule {
    @Singleton
    @Provides
    ImageCache provideImageCache() {
        return WordPress.getBitmapCache();
    }

    @Singleton
    @Provides
    OAuthAuthenticator provideOAuthAuthenicator(AccessToken accessToken) {
        return new OAuthAuthenticator(accessToken);
    }
}

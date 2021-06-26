package org.sitebay.android.modules;

import org.sitebay.android.fluxc.store.AccountStore;
import org.sitebay.android.fluxc.store.SiteStore;
import org.sitebay.android.login.LoginAnalyticsListener;
import org.sitebay.android.ui.accounts.UnifiedLoginTracker;
import org.sitebay.android.ui.accounts.login.LoginAnalyticsTracker;

import dagger.Module;
import dagger.Provides;

@Module
public class LoginAnalyticsModule {
    @Provides
    public LoginAnalyticsListener provideAnalyticsListener(AccountStore accountStore, SiteStore siteStore,
                                                           UnifiedLoginTracker unifiedLoginTracker) {
        return new LoginAnalyticsTracker(accountStore, siteStore, unifiedLoginTracker);
    }
}

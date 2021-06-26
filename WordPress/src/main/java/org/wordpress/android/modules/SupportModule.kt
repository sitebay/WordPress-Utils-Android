package org.sitebay.android.modules

import com.automattic.android.tracks.crashlogging.CrashLogging
import dagger.Module
import dagger.Provides
import org.sitebay.android.fluxc.store.AccountStore
import org.sitebay.android.fluxc.store.SiteStore
import org.sitebay.android.support.SupportHelper
import org.sitebay.android.support.ZendeskHelper
import org.sitebay.android.support.ZendeskPlanFieldHelper
import org.sitebay.android.util.BuildConfigWrapper
import javax.inject.Singleton

@Module
class SupportModule {
    @Singleton
    @Provides
    fun provideZendeskHelper(
        accountStore: AccountStore,
        siteStore: SiteStore,
        supportHelper: SupportHelper,
        zendeskPlanFieldHelper: ZendeskPlanFieldHelper,
        buildConfigWrapper: BuildConfigWrapper
    ): ZendeskHelper = ZendeskHelper(accountStore, siteStore, supportHelper, zendeskPlanFieldHelper, buildConfigWrapper)

    @Singleton
    @Provides
    fun provideSupportHelper(): SupportHelper = SupportHelper()

    @Singleton
    @Provides
    fun provideZendeskPlanFieldHelper(remoteLoggingUtils: CrashLogging): ZendeskPlanFieldHelper =
        ZendeskPlanFieldHelper(remoteLoggingUtils)
}

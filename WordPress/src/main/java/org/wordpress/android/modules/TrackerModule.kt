package org.sitebay.android.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import org.sitebay.android.BuildConfig
import org.sitebay.android.analytics.AnalyticsTrackerNosara
import org.sitebay.android.analytics.Tracker

@Module
class TrackerModule {
    @Provides
    fun provideTracker(appContext: Context): Tracker {
        return AnalyticsTrackerNosara(appContext, BuildConfig.TRACKS_EVENT_PREFIX)
    }
}

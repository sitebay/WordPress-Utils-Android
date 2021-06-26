package org.sitebay.android.modules

import dagger.Module
import dagger.Provides
import org.mockito.Mockito
import org.sitebay.android.analytics.Tracker

@Module
class TrackerTestModule {
    @Provides
    fun provideTracker(): Tracker {
        return Mockito.mock(Tracker::class.java)
    }
}

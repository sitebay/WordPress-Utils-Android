package org.sitebay.android.modules

import android.content.Context
import com.automattic.android.tracks.crashlogging.CrashLogging
import com.automattic.android.tracks.crashlogging.CrashLoggingDataProvider
import com.automattic.android.tracks.crashlogging.CrashLoggingProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import org.sitebay.android.util.crashlogging.WPCrashLoggingDataProvider
import javax.inject.Singleton

@Module
abstract class CrashLoggingModule {
    companion object {
        @Provides
        @Singleton
        fun provideCrashLogging(context: Context, crashLoggingDataProvider: CrashLoggingDataProvider): CrashLogging {
            return CrashLoggingProvider.createInstance(context, crashLoggingDataProvider)
        }
    }

    @Binds
    abstract fun bindCrashLoggingDataProvider(dataProvider: WPCrashLoggingDataProvider): CrashLoggingDataProvider
}

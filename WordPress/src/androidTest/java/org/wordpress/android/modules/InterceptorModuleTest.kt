package org.sitebay.android.modules

import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import okhttp3.Interceptor
import org.sitebay.android.support.MockingInterceptor
import javax.inject.Named

@Module
class InterceptorModuleTest {
    @Provides @IntoSet @Named("interceptors")
    fun provideMockingInterceptor(): Interceptor = MockingInterceptor()
}

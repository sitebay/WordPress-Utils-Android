package org.sitebay.android.ui.sitecreation.usecases

import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.sitebay.android.fluxc.Dispatcher
import org.sitebay.android.fluxc.generated.ThemeActionBuilder
import org.sitebay.android.fluxc.store.ThemeStore
import org.sitebay.android.fluxc.store.ThemeStore.FetchStarterDesignsPayload
import org.sitebay.android.fluxc.store.ThemeStore.OnStarterDesignsFetched
import org.sitebay.android.ui.layoutpicker.ThumbDimensionProvider
import javax.inject.Inject
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FetchHomePageLayoutsUseCase @Inject constructor(
    val dispatcher: Dispatcher,
    @Suppress("unused") val themeStore: ThemeStore,
    private val thumbDimensionProvider: ThumbDimensionProvider
) {
    private var continuation: Continuation<OnStarterDesignsFetched>? = null

    suspend fun fetchStarterDesigns(): OnStarterDesignsFetched {
        if (continuation != null) {
            throw IllegalStateException("Fetch already in progress.")
        }
        val payload = FetchStarterDesignsPayload(
                thumbDimensionProvider.previewWidth.toFloat(),
                thumbDimensionProvider.previewHeight.toFloat(),
                thumbDimensionProvider.scale.toFloat()
        )
        return suspendCoroutine { cont ->
            continuation = cont
            dispatcher.dispatch(ThemeActionBuilder.newFetchStarterDesignsAction(payload))
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onStarterDesignsFetched(event: OnStarterDesignsFetched) {
        continuation?.resume(event)
        continuation = null
    }
}

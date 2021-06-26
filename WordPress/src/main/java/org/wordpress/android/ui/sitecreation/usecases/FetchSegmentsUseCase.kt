package org.sitebay.android.ui.sitecreation.usecases

import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.sitebay.android.fluxc.Dispatcher
import org.sitebay.android.fluxc.generated.VerticalActionBuilder
import org.sitebay.android.fluxc.store.VerticalStore
import org.sitebay.android.fluxc.store.VerticalStore.OnSegmentsFetched
import javax.inject.Inject
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Transforms EventBus event to a coroutines.
 */
class FetchSegmentsUseCase @Inject constructor(
    val dispatcher: Dispatcher,
    @Suppress("unused") val verticalStore: VerticalStore
) {
    private var continuation: Continuation<OnSegmentsFetched>? = null

    suspend fun fetchCategories(): OnSegmentsFetched {
        if (continuation != null) {
            throw IllegalStateException("Fetch already in progress.")
        }
        return suspendCoroutine { cont ->
            continuation = cont
            dispatcher.dispatch(VerticalActionBuilder.newFetchSegmentsAction())
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    fun onSiteCategoriesFetched(event: OnSegmentsFetched) {
        continuation?.resume(event)
        continuation = null
    }
}

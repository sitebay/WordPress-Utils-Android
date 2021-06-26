package org.sitebay.android.ui.jetpack.scan.details.usecases

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.sitebay.android.fluxc.store.ScanStore
import org.sitebay.android.fluxc.store.ScanStore.IgnoreThreatPayload
import org.sitebay.android.modules.IO_THREAD
import org.sitebay.android.ui.jetpack.scan.details.usecases.IgnoreThreatUseCase.IgnoreThreatState.Failure
import org.sitebay.android.ui.jetpack.scan.details.usecases.IgnoreThreatUseCase.IgnoreThreatState.Success
import org.sitebay.android.util.NetworkUtilsWrapper
import javax.inject.Inject
import javax.inject.Named

class IgnoreThreatUseCase @Inject constructor(
    private val networkUtilsWrapper: NetworkUtilsWrapper,
    private val scanStore: ScanStore,
    @Named(IO_THREAD) private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun ignoreThreat(remoteSiteId: Long, threatId: Long) = withContext(ioDispatcher) {
        if (!networkUtilsWrapper.isNetworkAvailable()) {
            Failure.NetworkUnavailable
        } else {
            val result = scanStore.ignoreThreat(IgnoreThreatPayload(remoteSiteId, threatId))
            if (result.isError) {
                Failure.RemoteRequestFailure
            } else {
                Success
            }
        }
    }

    sealed class IgnoreThreatState {
        object Success : IgnoreThreatState()
        sealed class Failure : IgnoreThreatState() {
            object NetworkUnavailable : Failure()
            object RemoteRequestFailure : Failure()
        }
    }
}

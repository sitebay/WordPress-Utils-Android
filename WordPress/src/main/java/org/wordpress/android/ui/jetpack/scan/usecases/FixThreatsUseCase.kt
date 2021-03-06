package org.sitebay.android.ui.jetpack.scan.usecases

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.sitebay.android.fluxc.store.ScanStore
import org.sitebay.android.fluxc.store.ScanStore.FixThreatsPayload
import org.sitebay.android.modules.IO_THREAD
import org.sitebay.android.ui.jetpack.scan.usecases.FixThreatsUseCase.FixThreatsState.Failure
import org.sitebay.android.ui.jetpack.scan.usecases.FixThreatsUseCase.FixThreatsState.Success
import org.sitebay.android.util.NetworkUtilsWrapper
import javax.inject.Inject
import javax.inject.Named

class FixThreatsUseCase @Inject constructor(
    private val networkUtilsWrapper: NetworkUtilsWrapper,
    private val scanStore: ScanStore,
    @Named(IO_THREAD) private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun fixThreats(remoteSiteId: Long, fixableThreatIds: List<Long>) = withContext(ioDispatcher) {
        if (!networkUtilsWrapper.isNetworkAvailable()) {
            Failure.NetworkUnavailable
        } else {
            val result = scanStore.fixThreats(FixThreatsPayload(remoteSiteId, fixableThreatIds))
            if (result.isError) {
                Failure.RemoteRequestFailure
            } else {
                Success
            }
        }
    }

    sealed class FixThreatsState {
        object Success : FixThreatsState()
        sealed class Failure : FixThreatsState() {
            object NetworkUnavailable : Failure()
            object RemoteRequestFailure : Failure()
        }
    }
}

package org.sitebay.android.ui.jetpack.backup.download.usecases

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.fluxc.store.ActivityLogStore
import org.sitebay.android.fluxc.store.ActivityLogStore.BackupDownloadPayload
import org.sitebay.android.fluxc.store.ActivityLogStore.BackupDownloadRequestTypes
import org.sitebay.android.modules.IO_THREAD
import org.sitebay.android.ui.jetpack.backup.download.BackupDownloadRequestState
import org.sitebay.android.ui.jetpack.backup.download.BackupDownloadRequestState.Failure.NetworkUnavailable
import org.sitebay.android.ui.jetpack.backup.download.BackupDownloadRequestState.Failure.OtherRequestRunning
import org.sitebay.android.ui.jetpack.backup.download.BackupDownloadRequestState.Failure.RemoteRequestFailure
import org.sitebay.android.ui.jetpack.backup.download.BackupDownloadRequestState.Success
import org.sitebay.android.util.NetworkUtilsWrapper
import javax.inject.Inject
import javax.inject.Named

class PostBackupDownloadUseCase @Inject constructor(
    private val networkUtilsWrapper: NetworkUtilsWrapper,
    private val activityLogStore: ActivityLogStore,
    @Named(IO_THREAD) private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun postBackupDownloadRequest(
        rewindId: String,
        site: SiteModel,
        types: BackupDownloadRequestTypes
    ): BackupDownloadRequestState = withContext(ioDispatcher) {
        if (!networkUtilsWrapper.isNetworkAvailable()) {
            return@withContext NetworkUnavailable
        }

        val result = activityLogStore.backupDownload(BackupDownloadPayload(site, rewindId, types))
        if (result.isError) {
            RemoteRequestFailure
        } else {
            if (result.rewindId == rewindId) {
                if (result.downloadId == null) {
                    RemoteRequestFailure
                } else {
                    Success(rewindId, result.rewindId, result.downloadId)
                }
            } else {
                OtherRequestRunning
            }
        }
    }
}

package org.sitebay.android.ui.jetpack.backup.download.usecases

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.InternalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.sitebay.android.BaseUnitTest
import org.sitebay.android.TEST_DISPATCHER
import org.sitebay.android.fluxc.action.ActivityLogAction.DISMISS_BACKUP_DOWNLOAD
import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.fluxc.store.ActivityLogStore
import org.sitebay.android.fluxc.store.ActivityLogStore.DismissBackupDownloadError
import org.sitebay.android.fluxc.store.ActivityLogStore.DismissBackupDownloadErrorType.GENERIC_ERROR
import org.sitebay.android.fluxc.store.ActivityLogStore.DismissBackupDownloadErrorType.INVALID_RESPONSE
import org.sitebay.android.fluxc.store.ActivityLogStore.OnDismissBackupDownload
import org.sitebay.android.test
import org.sitebay.android.util.NetworkUtilsWrapper

@InternalCoroutinesApi
class PostDismissBackupDownloadUseCaseTest : BaseUnitTest() {
    private lateinit var useCase: PostDismissBackupDownloadUseCase
    @Mock lateinit var networkUtilsWrapper: NetworkUtilsWrapper
    @Mock lateinit var activityLogStore: ActivityLogStore
    @Mock lateinit var siteModel: SiteModel

    private val downloadId = 100L

    @Before
    fun setup() = test {
        useCase = PostDismissBackupDownloadUseCase(networkUtilsWrapper, activityLogStore, TEST_DISPATCHER)
        whenever(networkUtilsWrapper.isNetworkAvailable()).thenReturn(true)
    }

    @Test
    fun `given no network, when dismiss is triggered, then false is returned`() = test {
        whenever(networkUtilsWrapper.isNetworkAvailable()).thenReturn(false)

        val result = useCase.dismissBackupDownload(downloadId, siteModel)

        assertThat(result).isEqualTo(false)
    }

    @Test
    fun `given invalid response, when dismiss is triggered, then false is returned`() = test {
        whenever(activityLogStore.dismissBackupDownload(any())).thenReturn(
                OnDismissBackupDownload(
                        downloadId, DismissBackupDownloadError(INVALID_RESPONSE),
                        DISMISS_BACKUP_DOWNLOAD
                )
        )

        val result = useCase.dismissBackupDownload(downloadId, siteModel)

        assertThat(result).isEqualTo(false)
    }

    @Test
    fun `given generic error response, when dismiss download is triggered, then false is returned`() = test {
        whenever(activityLogStore.dismissBackupDownload(any()))
                .thenReturn(
                        OnDismissBackupDownload(
                                downloadId, DismissBackupDownloadError(GENERIC_ERROR),
                                DISMISS_BACKUP_DOWNLOAD
                        )
                )

        val result = useCase.dismissBackupDownload(downloadId, siteModel)

        assertThat(result).isEqualTo(false)
    }

    @Test
    fun `when dismiss download is triggered successfully, then true is returned`() = test {
        whenever(activityLogStore.dismissBackupDownload(any())).thenReturn(
                OnDismissBackupDownload(
                        downloadId = downloadId,
                        isDismissed = true,
                        causeOfChange = DISMISS_BACKUP_DOWNLOAD
                )
        )

        val result = useCase.dismissBackupDownload(downloadId, siteModel)

        assertThat(result).isEqualTo(true)
    }
}

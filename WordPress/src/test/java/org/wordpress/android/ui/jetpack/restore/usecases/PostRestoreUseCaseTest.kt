package org.sitebay.android.ui.jetpack.restore.usecases

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.InternalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.sitebay.android.BaseUnitTest
import org.sitebay.android.TEST_DISPATCHER
import org.sitebay.android.fluxc.action.ActivityLogAction
import org.sitebay.android.fluxc.action.ActivityLogAction.REWIND
import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.fluxc.model.activity.RewindStatusModel
import org.sitebay.android.fluxc.model.activity.RewindStatusModel.Rewind
import org.sitebay.android.fluxc.model.activity.RewindStatusModel.Rewind.Status
import org.sitebay.android.fluxc.model.activity.RewindStatusModel.Rewind.Status.QUEUED
import org.sitebay.android.fluxc.model.activity.RewindStatusModel.Rewind.Status.RUNNING
import org.sitebay.android.fluxc.model.activity.RewindStatusModel.State.ACTIVE
import org.sitebay.android.fluxc.store.ActivityLogStore
import org.sitebay.android.fluxc.store.ActivityLogStore.OnRewind
import org.sitebay.android.fluxc.store.ActivityLogStore.OnRewindStatusFetched
import org.sitebay.android.fluxc.store.ActivityLogStore.RewindError
import org.sitebay.android.fluxc.store.ActivityLogStore.RewindErrorType.API_ERROR
import org.sitebay.android.fluxc.store.ActivityLogStore.RewindErrorType.GENERIC_ERROR
import org.sitebay.android.fluxc.store.ActivityLogStore.RewindErrorType.INVALID_RESPONSE
import org.sitebay.android.fluxc.store.ActivityLogStore.RewindRequestTypes
import org.sitebay.android.fluxc.store.ActivityLogStore.RewindStatusError
import org.sitebay.android.fluxc.store.ActivityLogStore.RewindStatusErrorType
import org.sitebay.android.test
import org.sitebay.android.ui.jetpack.restore.RestoreRequestState.Failure.NetworkUnavailable
import org.sitebay.android.ui.jetpack.restore.RestoreRequestState.Failure.OtherRequestRunning
import org.sitebay.android.ui.jetpack.restore.RestoreRequestState.Failure.RemoteRequestFailure
import org.sitebay.android.ui.jetpack.restore.RestoreRequestState.Success
import org.sitebay.android.util.NetworkUtilsWrapper
import java.util.Date

@InternalCoroutinesApi
class PostRestoreUseCaseTest : BaseUnitTest() {
    private lateinit var useCase: PostRestoreUseCase
    @Mock lateinit var networkUtilsWrapper: NetworkUtilsWrapper
    @Mock lateinit var activityLogStore: ActivityLogStore
    @Mock lateinit var siteModel: SiteModel

    private val rewindId = "rewindId"
    private val restoreId = 1L
    private val types: RewindRequestTypes = RewindRequestTypes(
            themes = true,
            plugins = true,
            uploads = true,
            sqls = true,
            roots = true,
            contents = true
    )

    @Before
    fun setup() = test {
        useCase = PostRestoreUseCase(networkUtilsWrapper, activityLogStore, TEST_DISPATCHER)
        whenever(networkUtilsWrapper.isNetworkAvailable()).thenReturn(true)
        whenever(activityLogStore.fetchActivitiesRewind(any())).thenReturn(
                OnRewindStatusFetched(ActivityLogAction.FETCH_REWIND_STATE)
        )
    }

    @Test
    fun `given no network, when rewind is triggered, then NetworkUnavailable is returned`() = test {
        whenever(networkUtilsWrapper.isNetworkAvailable()).thenReturn(false)

        val result = useCase.postRestoreRequest(rewindId, siteModel, types)

        assertThat(result).isEqualTo(NetworkUnavailable)
    }

    @Test
    fun `given invalid response, when restore is triggered, then RemoteRequestFailure is returned`() = test {
        whenever(activityLogStore.rewind(any())).thenReturn(OnRewind(rewindId, RewindError(INVALID_RESPONSE), REWIND))

        val result = useCase.postRestoreRequest(rewindId, siteModel, types)

        assertThat(result).isEqualTo(RemoteRequestFailure)
    }

    @Test
    fun `given generic error response, when restore is triggered, then RemoteRequestFailure is returned`() = test {
        whenever(activityLogStore.rewind(any())).thenReturn(OnRewind(rewindId, RewindError(GENERIC_ERROR), REWIND))

        val result = useCase.postRestoreRequest(rewindId, siteModel, types)

        assertThat(result).isEqualTo(RemoteRequestFailure)
    }

    @Test
    fun `given api error response, when restore is triggered, then RemoteRequestFailure is returned`() = test {
        whenever(activityLogStore.rewind(any())).thenReturn(OnRewind(rewindId, RewindError(API_ERROR), REWIND))

        val result = useCase.postRestoreRequest(rewindId, siteModel, types)

        assertThat(result).isEqualTo(RemoteRequestFailure)
    }

    @Test
    fun `when restore is triggered successfully, then Success is returned`() = test {
        whenever(activityLogStore.rewind(any())).thenReturn(OnRewind(rewindId, restoreId, REWIND))

        val result = useCase.postRestoreRequest(rewindId, siteModel, types)

        assertThat(result).isEqualTo(Success(requestRewindId = rewindId, rewindId = rewindId, restoreId = restoreId))
    }

    @Test
    fun `given fetch error, then RemoteRequestFailure is returned`() = test {
        whenever(activityLogStore.fetchActivitiesRewind(any())).thenReturn(
                OnRewindStatusFetched(
                        RewindStatusError(RewindStatusErrorType.GENERIC_ERROR),
                        ActivityLogAction.FETCH_REWIND_STATE
                )
        )

        val result = useCase.postRestoreRequest(rewindId, siteModel, types)

        assertThat(result).isEqualTo(RemoteRequestFailure)
    }

    @Test
    fun `given fetch success, when process is running, then OtherRequestRunning is returned`() = test {
        whenever(activityLogStore.getRewindStatusForSite(siteModel))
                .thenReturn(buildStatusModel(RUNNING))

        val result = useCase.postRestoreRequest(rewindId, siteModel, types)

        assertThat(result).isEqualTo(OtherRequestRunning)
    }

    @Test
    fun `given fetch success, when process is queued, then OtherRequestRunning is returned`() = test {
        whenever(activityLogStore.getRewindStatusForSite(siteModel))
                .thenReturn(buildStatusModel(QUEUED))

        val result = useCase.postRestoreRequest(rewindId, siteModel, types)

        assertThat(result).isEqualTo(OtherRequestRunning)
    }

    private fun buildStatusModel(status: Status) = RewindStatusModel(
            state = ACTIVE,
            reason = null,
            lastUpdated = Date(1609690147756),
            canAutoconfigure = null,
            credentials = null,
            rewind = Rewind(
                    rewindId = rewindId,
                    restoreId = restoreId,
                    status = status,
                    progress = null,
                    reason = null,
                    message = null,
                    currentEntry = null
            )
    )
}

package org.sitebay.android.ui.engagement

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import org.sitebay.android.modules.BG_THREAD
import org.sitebay.android.ui.engagement.GetLikesUseCase.CurrentUserInListRequirement
import org.sitebay.android.ui.engagement.GetLikesUseCase.CurrentUserInListRequirement.DONT_CARE
import org.sitebay.android.ui.engagement.GetLikesUseCase.FailureType.NO_NETWORK
import org.sitebay.android.ui.engagement.GetLikesUseCase.GetLikesState
import org.sitebay.android.ui.engagement.GetLikesUseCase.GetLikesState.Failure
import org.sitebay.android.ui.engagement.GetLikesUseCase.GetLikesState.Loading
import org.sitebay.android.ui.engagement.GetLikesUseCase.GetLikesState.LikesData
import org.sitebay.android.ui.engagement.GetLikesUseCase.LikeGroupFingerPrint
import org.sitebay.android.ui.engagement.GetLikesUseCase.PaginationParams
import org.sitebay.android.ui.pages.SnackbarMessageHolder
import org.sitebay.android.viewmodel.Event
import javax.inject.Inject
import javax.inject.Named

class GetLikesHandler @Inject constructor(
    private val getLikesUseCase: GetLikesUseCase,
    @Named(BG_THREAD) private val bgDispatcher: CoroutineDispatcher
) {
    private val _snackbarEvents = MediatorLiveData<Event<SnackbarMessageHolder>>()
    val snackbarEvents: LiveData<Event<SnackbarMessageHolder>> = _snackbarEvents

    private val _likesStatusUpdate = MediatorLiveData<GetLikesState>()
    val likesStatusUpdate: LiveData<GetLikesState> = _likesStatusUpdate

    suspend fun handleGetLikesForPost(
        fingerPrint: LikeGroupFingerPrint,
        requestNextPage: Boolean,
        pageLength: Int = LIKES_PER_PAGE_DEFAULT,
        limit: Int = LIKES_RESULT_NO_LIMITS,
        expectingToBeThere: CurrentUserInListRequirement = DONT_CARE
    ) {
        getLikesUseCase.getLikesForPost(
                fingerPrint,
                PaginationParams(
                        requestNextPage,
                        pageLength,
                        limit
                ),
                expectingToBeThere
        ).flowOn(bgDispatcher).collect { state ->
            manageState(state)
        }
    }

    suspend fun handleGetLikesForComment(
        fingerPrint: LikeGroupFingerPrint,
        requestNextPage: Boolean,
        pageLength: Int = LIKES_PER_PAGE_DEFAULT
    ) {
        getLikesUseCase.getLikesForComment(
                fingerPrint,
                PaginationParams(
                        requestNextPage,
                        pageLength,
                        LIKES_RESULT_NO_LIMITS
                )
        ).flowOn(bgDispatcher).collect { state ->
            manageState(state)
        }
    }

    fun clear() {
        getLikesUseCase.clear()
    }

    private fun manageState(state: GetLikesState) {
        when (state) {
            Loading,
            is LikesData -> {
                _likesStatusUpdate.postValue(state)
            }
            is Failure -> {
                _likesStatusUpdate.postValue(state)
                if (state.failureType != NO_NETWORK || !state.emptyStateData.showEmptyState) {
                    _snackbarEvents.postValue(Event(SnackbarMessageHolder(state.error)))
                }
            }
        }
    }

    companion object {
        private const val LIKES_PER_PAGE_DEFAULT = 20
        private const val LIKES_RESULT_NO_LIMITS = -1
    }
}

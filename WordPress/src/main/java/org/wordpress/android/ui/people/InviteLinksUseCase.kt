package org.sitebay.android.ui.people

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import org.sitebay.android.R.string
import org.sitebay.android.analytics.AnalyticsTracker.Stat
import org.sitebay.android.fluxc.store.AccountStore
import org.sitebay.android.fluxc.store.SiteStore
import org.sitebay.android.ui.people.AnalyticsInviteLinksActionResult.ERROR
import org.sitebay.android.ui.people.AnalyticsInviteLinksGenericError.NO_NETWORK
import org.sitebay.android.ui.people.AnalyticsInviteLinksGenericError.USER_NOT_AUTHENTICATED
import org.sitebay.android.ui.people.InviteLinksApiCallsProvider.InviteLinksCallResult
import org.sitebay.android.ui.people.InviteLinksApiCallsProvider.InviteLinksCallResult.Failure
import org.sitebay.android.ui.people.InviteLinksApiCallsProvider.InviteLinksCallResult.Success
import org.sitebay.android.ui.people.InviteLinksApiCallsProvider.InviteLinksItem
import org.sitebay.android.ui.people.InviteLinksUseCase.InviteLinksState.InviteLinksData
import org.sitebay.android.ui.people.InviteLinksUseCase.InviteLinksState.InviteLinksError
import org.sitebay.android.ui.people.InviteLinksUseCase.InviteLinksState.InviteLinksLoading
import org.sitebay.android.ui.people.InviteLinksUseCase.InviteLinksState.UserNotAuthenticated
import org.sitebay.android.ui.people.InviteLinksUseCase.UseCaseScenarioContext.GENERATING_LINKS
import org.sitebay.android.ui.people.InviteLinksUseCase.UseCaseScenarioContext.INITIALIZING
import org.sitebay.android.ui.people.InviteLinksUseCase.UseCaseScenarioContext.MANAGING_AVAILABLE_LINKS
import org.sitebay.android.ui.utils.UiString
import org.sitebay.android.ui.utils.UiString.UiStringRes
import org.sitebay.android.ui.utils.UiString.UiStringText
import org.sitebay.android.util.NetworkUtilsWrapper
import org.sitebay.android.util.analytics.AnalyticsUtilsWrapper
import javax.inject.Inject

class InviteLinksUseCase @Inject constructor(
    private val networkUtilsWrapper: NetworkUtilsWrapper,
    private val inviteLinksApiCallsProvider: InviteLinksApiCallsProvider,
    private val accountStore: AccountStore,
    private val analyticsUtilsWrapper: AnalyticsUtilsWrapper,
    private val siteStore: SiteStore
) {
    suspend fun getInviteLinksStatus(
        blogId: Long,
        scenarioContext: UseCaseScenarioContext
    ): Flow<InviteLinksState> = flow {
        makeChecksAndApplyStrategy(
                this,
                blogId,
                ::getInvitesStrategy,
                scenarioContext,
                Stat.INVITE_LINKS_GET_STATUS
        )
    }

    suspend fun generateLinks(blogId: Long): Flow<InviteLinksState> = flow {
        makeChecksAndApplyStrategy(
                this,
                blogId,
                ::generateInvitesStrategy,
                GENERATING_LINKS,
                Stat.INVITE_LINKS_GENERATE
        )
    }

    suspend fun disableLinks(blogId: Long): Flow<InviteLinksState> = flow {
        makeChecksAndApplyStrategy(
                this,
                blogId,
                ::disableInvitesStrategy,
                MANAGING_AVAILABLE_LINKS,
                Stat.INVITE_LINKS_DISABLE
        )
    }

    private suspend fun makeChecksAndApplyStrategy(
        flow: FlowCollector<InviteLinksState>,
        blogId: Long,
        strategy: suspend (
            flow: FlowCollector<InviteLinksState>,
            blogId: Long,
            scenarioContext: UseCaseScenarioContext
        ) -> InviteLinksCallResult,
        scenarioContext: UseCaseScenarioContext,
        stat: Stat
    ) {
        val properties = mutableMapOf<String, Any?>()

        if (!accountStore.hasAccessToken()) {
            flow.emit(UserNotAuthenticated)
            properties.addInviteLinksActionResult(ERROR, USER_NOT_AUTHENTICATED.errorMessage)
        } else {
            flow.emit(InviteLinksLoading(scenarioContext))
            delay(PROGRESS_DELAY_MS)
            if (!networkUtilsWrapper.isNetworkAvailable()) {
                flow.emit(InviteLinksError(scenarioContext, UiStringRes(string.error_network_connection)))
                properties.addInviteLinksActionResult(ERROR, NO_NETWORK.errorMessage)
            } else {
                val actionResult = strategy.invoke(flow, blogId, scenarioContext)
                properties.addInviteLinksActionResult(
                        actionResult,
                        if (actionResult is Failure) actionResult.error else null
                )
            }
        }

        analyticsUtilsWrapper.trackInviteLinksAction(stat, siteStore.getSiteBySiteId(blogId), properties)
    }

    private suspend fun getInvitesStrategy(
        flow: FlowCollector<InviteLinksState>,
        blogId: Long,
        scenarioContext: UseCaseScenarioContext
    ): InviteLinksCallResult {
        val status = inviteLinksApiCallsProvider.getInviteLinksStatus(blogId)

        when (status) {
            is Success -> flow.emit(InviteLinksData(scenarioContext, status.links))
            is Failure -> flow.emit(InviteLinksError(scenarioContext, UiStringText(status.error)))
        }

        return status
    }

    private suspend fun generateInvitesStrategy(
        flow: FlowCollector<InviteLinksState>,
        blogId: Long,
        scenarioContext: UseCaseScenarioContext
    ): InviteLinksCallResult {
        var status = inviteLinksApiCallsProvider.generateLinks(blogId)

        when (status) {
            is Success -> {
                status = inviteLinksApiCallsProvider.getInviteLinksStatus(blogId)

                when (status) {
                    is Success -> {
                        flow.emit(InviteLinksData(scenarioContext, status.links))
                    }
                    is Failure -> {
                        flow.emit(InviteLinksError(scenarioContext, UiStringText(status.error)))
                    }
                }
            }
            is Failure -> flow.emit(InviteLinksError(scenarioContext, UiStringText(status.error)))
        }

        return status
    }

    private suspend fun disableInvitesStrategy(
        flow: FlowCollector<InviteLinksState>,
        blogId: Long,
        scenarioContext: UseCaseScenarioContext
    ): InviteLinksCallResult {
        val status = inviteLinksApiCallsProvider.disableLinks(blogId)

        when (status) {
            is Success -> flow.emit(InviteLinksData(scenarioContext, listOf()))
            is Failure -> flow.emit(InviteLinksError(scenarioContext, UiStringText(status.error)))
        }

        return status
    }

    enum class UseCaseScenarioContext {
        INITIALIZING,
        GENERATING_LINKS,
        MANAGING_AVAILABLE_LINKS
    }

    sealed class InviteLinksState(open val scenarioContext: UseCaseScenarioContext = INITIALIZING) {
        object InviteLinksNotAllowed : InviteLinksState(INITIALIZING)

        object UserNotAuthenticated : InviteLinksState(INITIALIZING)

        data class InviteLinksLoading(override val scenarioContext: UseCaseScenarioContext) : InviteLinksState()

        data class InviteLinksData(
            override val scenarioContext: UseCaseScenarioContext,
            val links: List<InviteLinksItem>
        ) : InviteLinksState()

        data class InviteLinksUserChangedRole(
            val selectedRole: InviteLinksItem
        ) : InviteLinksState(MANAGING_AVAILABLE_LINKS)

        data class InviteLinksError(
            override val scenarioContext: UseCaseScenarioContext,
            val error: UiString
        ) : InviteLinksState()
    }

    companion object {
        // Pretty arbitrary amount to allow the progress bar to appear to the user
        private const val PROGRESS_DELAY_MS = 600L
    }
}

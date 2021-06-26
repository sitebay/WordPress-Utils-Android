package org.sitebay.android.ui.people.usecases

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.flow.toList
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mock
import org.sitebay.android.BaseUnitTest
import org.sitebay.android.R.string
import org.sitebay.android.fluxc.store.AccountStore
import org.sitebay.android.fluxc.store.SiteStore
import org.sitebay.android.test
import org.sitebay.android.ui.people.InviteLinksApiCallsProvider
import org.sitebay.android.ui.people.InviteLinksApiCallsProvider.InviteLinksCallResult.Failure
import org.sitebay.android.ui.people.InviteLinksApiCallsProvider.InviteLinksCallResult.Success
import org.sitebay.android.ui.people.InviteLinksApiCallsProvider.InviteLinksItem
import org.sitebay.android.ui.people.InviteLinksUseCase
import org.sitebay.android.ui.people.InviteLinksUseCase.InviteLinksState.InviteLinksData
import org.sitebay.android.ui.people.InviteLinksUseCase.InviteLinksState.InviteLinksError
import org.sitebay.android.ui.people.InviteLinksUseCase.InviteLinksState.InviteLinksLoading
import org.sitebay.android.ui.people.InviteLinksUseCase.InviteLinksState.UserNotAuthenticated
import org.sitebay.android.ui.people.InviteLinksUseCase.UseCaseScenarioContext.GENERATING_LINKS
import org.sitebay.android.ui.people.InviteLinksUseCase.UseCaseScenarioContext.INITIALIZING
import org.sitebay.android.ui.people.InviteLinksUseCase.UseCaseScenarioContext.MANAGING_AVAILABLE_LINKS
import org.sitebay.android.ui.utils.UiString.UiStringRes
import org.sitebay.android.ui.utils.UiString.UiStringText
import org.sitebay.android.util.NetworkUtilsWrapper
import org.sitebay.android.util.analytics.AnalyticsUtilsWrapper

class InviteLinksUseCaseTest : BaseUnitTest() {
    @Mock lateinit var networkUtilsWrapper: NetworkUtilsWrapper
    @Mock lateinit var inviteLinksApiCallsProvider: InviteLinksApiCallsProvider
    @Mock lateinit var accountStore: AccountStore
    @Mock lateinit var analyticsUtilsWrapper: AnalyticsUtilsWrapper
    @Mock lateinit var siteStore: SiteStore

    private lateinit var inviteLinksUseCase: InviteLinksUseCase
    private val blogId = 100L

    @Before
    fun setUp() {
        whenever(accountStore.hasAccessToken()).thenReturn(true)
        whenever(networkUtilsWrapper.isNetworkAvailable()).thenReturn(true)

        inviteLinksUseCase = InviteLinksUseCase(
                networkUtilsWrapper,
                inviteLinksApiCallsProvider,
                accountStore,
                analyticsUtilsWrapper,
                siteStore
        )
    }

    @Test
    fun `makeChecksAndApplyStrategy emits expected state when user not logged in`() = test {
        whenever(accountStore.hasAccessToken()).thenReturn(false)

        val flow = inviteLinksUseCase.getInviteLinksStatus(blogId, INITIALIZING)

        assertThat(flow.toList()).isEqualTo(listOf(UserNotAuthenticated))
    }

    @Test
    fun `makeChecksAndApplyStrategy emits expected states when no network`() = test {
        whenever(networkUtilsWrapper.isNetworkAvailable()).thenReturn(false)

        val flow = inviteLinksUseCase.getInviteLinksStatus(blogId, INITIALIZING)

        assertThat(flow.toList()).isEqualTo(
                listOf(
                        InviteLinksLoading(INITIALIZING),
                        InviteLinksError(INITIALIZING, UiStringRes(string.error_network_connection))
                )
        )
    }

    @Test
    fun `getInviteLinksStatus emits expected states on Success`() = test {
        val inviteLinks = listOf<InviteLinksItem>(mock(), mock())

        whenever(inviteLinksApiCallsProvider.getInviteLinksStatus(anyLong())).thenReturn(Success(inviteLinks))

        val flow = inviteLinksUseCase.getInviteLinksStatus(blogId, INITIALIZING)

        assertThat(flow.toList()).isEqualTo(
                listOf(
                        InviteLinksLoading(INITIALIZING),
                        InviteLinksData(INITIALIZING, inviteLinks)
                )
        )
    }

    @Test
    fun `getInviteLinksStatus emits expected states on Failure`() = test {
        val error = "error message"

        whenever(inviteLinksApiCallsProvider.getInviteLinksStatus(anyLong())).thenReturn(Failure(error))

        val flow = inviteLinksUseCase.getInviteLinksStatus(blogId, INITIALIZING)

        assertThat(flow.toList()).isEqualTo(
                listOf(
                        InviteLinksLoading(INITIALIZING),
                        InviteLinksError(INITIALIZING, UiStringText(error))
                )
        )
    }

    @Test
    fun `generateInvitesStrategy emits expected states when getInviteLinksStatus succeeds`() = test {
        val inviteLinks = listOf<InviteLinksItem>(mock(), mock())

        whenever(inviteLinksApiCallsProvider.generateLinks(anyLong())).thenReturn(Success(listOf()))
        whenever(inviteLinksApiCallsProvider.getInviteLinksStatus(anyLong())).thenReturn(Success(inviteLinks))

        val flow = inviteLinksUseCase.generateLinks(blogId)

        assertThat(flow.toList()).isEqualTo(
                listOf(
                        InviteLinksLoading(GENERATING_LINKS),
                        InviteLinksData(GENERATING_LINKS, inviteLinks)
                )
        )
    }

    @Test
    fun `generateInvitesStrategy emits expected states when getInviteLinksStatus fails`() = test {
        val error = "error message"

        whenever(inviteLinksApiCallsProvider.generateLinks(anyLong())).thenReturn(Success(listOf()))
        whenever(inviteLinksApiCallsProvider.getInviteLinksStatus(anyLong())).thenReturn(Failure(error))

        val flow = inviteLinksUseCase.generateLinks(blogId)

        assertThat(flow.toList()).isEqualTo(
                listOf(
                        InviteLinksLoading(GENERATING_LINKS),
                        InviteLinksError(GENERATING_LINKS, UiStringText(error))
                )
        )
    }

    @Test
    fun `generateInvitesStrategy emits expected states when generateLinks fails`() = test {
        val error = "error message"

        whenever(inviteLinksApiCallsProvider.generateLinks(anyLong())).thenReturn(Success(listOf()))
        whenever(inviteLinksApiCallsProvider.getInviteLinksStatus(anyLong())).thenReturn(Failure(error))

        val flow = inviteLinksUseCase.generateLinks(blogId)

        assertThat(flow.toList()).isEqualTo(
                listOf(InviteLinksLoading(GENERATING_LINKS),
                        InviteLinksError(GENERATING_LINKS, UiStringText(error))
                )
        )
    }

    @Test
    fun `disableInvitesStrategy emits expected states on Success`() = test {
        whenever(inviteLinksApiCallsProvider.disableLinks(anyLong())).thenReturn(Success(listOf()))

        val flow = inviteLinksUseCase.disableLinks(blogId)

        assertThat(flow.toList()).isEqualTo(
                listOf(
                        InviteLinksLoading(MANAGING_AVAILABLE_LINKS),
                        InviteLinksData(MANAGING_AVAILABLE_LINKS, listOf())
                )
        )
    }

    @Test
    fun `disableInvitesStrategy emits expected states on Failure`() = test {
        val error = "error message"

        whenever(inviteLinksApiCallsProvider.disableLinks(anyLong())).thenReturn(Failure(error))

        val flow = inviteLinksUseCase.disableLinks(blogId)

        assertThat(flow.toList()).isEqualTo(
                listOf(
                        InviteLinksLoading(MANAGING_AVAILABLE_LINKS),
                        InviteLinksError(MANAGING_AVAILABLE_LINKS, UiStringText(error))
                )
        )
    }
}

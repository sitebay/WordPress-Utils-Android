package org.sitebay.android.ui.deeplinks

import android.net.Uri
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.isNull
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.InternalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.sitebay.android.BaseUnitTest
import org.sitebay.android.TEST_DISPATCHER
import org.sitebay.android.analytics.AnalyticsTracker.Stat.DEEP_LINKED
import org.sitebay.android.fluxc.store.AccountStore
import org.sitebay.android.ui.deeplinks.DeepLinkNavigator.NavigateAction
import org.sitebay.android.ui.deeplinks.DeepLinkNavigator.NavigateAction.OpenInBrowser
import org.sitebay.android.ui.deeplinks.DeepLinkNavigator.NavigateAction.StartCreateSiteFlow
import org.sitebay.android.ui.deeplinks.handlers.DeepLinkHandlers
import org.sitebay.android.ui.deeplinks.handlers.ServerTrackingHandler
import org.sitebay.android.util.UriWrapper
import org.sitebay.android.util.analytics.AnalyticsUtilsWrapper

class DeepLinkingIntentReceiverViewModelTest : BaseUnitTest() {
    @Mock lateinit var deepLinkHandlers: DeepLinkHandlers
    @Mock lateinit var accountStore: AccountStore
    @Mock lateinit var deepLinkUriUtils: DeepLinkUriUtils
    @Mock lateinit var serverTrackingHandler: ServerTrackingHandler
    @Mock lateinit var deepLinkTrackingUtils: DeepLinkTrackingUtils
    @Mock lateinit var analyticsUtilsWrapper: AnalyticsUtilsWrapper
    private lateinit var viewModel: DeepLinkingIntentReceiverViewModel
    private var isFinished = false
    private lateinit var navigateActions: MutableList<NavigateAction>

    @InternalCoroutinesApi
    @Before
    fun setUp() {
        viewModel = DeepLinkingIntentReceiverViewModel(
                TEST_DISPATCHER,
                deepLinkHandlers,
                deepLinkUriUtils,
                accountStore,
                serverTrackingHandler,
                deepLinkTrackingUtils,
                analyticsUtilsWrapper
        )
        isFinished = false
        viewModel.finish.observeForever {
            it?.getContentIfNotHandled()?.let {
                isFinished = true
            }
        }
        navigateActions = mutableListOf()
        viewModel.navigateAction.observeForever {
            it?.getContentIfNotHandled()?.let {
                navigateActions.add(it)
            }
        }
        whenever(accountStore.hasAccessToken()).thenReturn(true)
    }

    @Test
    fun `does not navigate and finishes on WPcom URL`() {
        val uri = buildUri("sitebay.com")

        viewModel.start(null, uri)

        assertUriNotHandled()
    }

    @Test
    fun `does not navigate and finishes on non-mobile URL`() {
        val uri = buildUri("mytest.sitebay.org")

        viewModel.start(null, uri)

        assertUriNotHandled()
    }

    @Test
    fun `mbar URL without redirect parameter replaced mbar to bar and opened in browser`() {
        val uri = initTrackingUri()
        val barUri = buildUri("mytest.sitebay.org")
        whenever(uri.copy("bar")).thenReturn(barUri)

        viewModel.start(null, uri)

        assertUriHandled(OpenInBrowser(barUri))
    }

    @Test
    fun `URL passed to deep link handler from redirect parameter`() {
        val startUrl = mock<UriWrapper>()
        val wpLoginUri = initWpLoginUri(startUrl)
        val uri = initTrackingUri(wpLoginUri)

        whenever(deepLinkHandlers.buildNavigateAction(startUrl)).thenReturn(StartCreateSiteFlow)

        viewModel.start(null, uri)

        assertUriHandled(StartCreateSiteFlow)
        verify(serverTrackingHandler).request(uri)
    }

    @Test
    fun `URL opened in browser from redirect parameter when deep link handler cannot handle it`() {
        val startUrl = mock<UriWrapper>()
        val wpLoginUri = initWpLoginUri(startUrl)
        val uri = initTrackingUri(wpLoginUri)
        val barUri = buildUri("mytest.sitebay.org")

        whenever(deepLinkHandlers.buildNavigateAction(startUrl)).thenReturn(null)
        whenever(uri.copy("bar")).thenReturn(barUri)

        viewModel.start(null, uri)

        assertUriHandled(OpenInBrowser(barUri))
    }

    @Test
    fun `wp-login mbar URL redirects user to browser with missing second redirect`() {
        val wpLoginUri = initWpLoginUri()
        val uri = initTrackingUri(wpLoginUri)
        val barUri = buildUri("mytest.sitebay.org")
        whenever(uri.copy("bar")).thenReturn(barUri)

        viewModel.start(null, uri)

        assertUriHandled(OpenInBrowser(barUri))
    }

    @Test
    fun `tracks deeplink when action not null and URL null`() {
        val action = "VIEW"

        viewModel.start(action, null)

        verify(analyticsUtilsWrapper).trackWithDeepLinkData(eq(DEEP_LINKED), eq(action), eq(""), isNull())
    }

    @Test
    fun `tracks deeplink when action not null and URL not null`() {
        val action = "VIEW"
        val host = "sitebay.com"
        val uriWrapper = buildUri(host)
        val mockedUri = mock<Uri>()
        whenever(uriWrapper.uri).thenReturn(mockedUri)

        viewModel.start(action, uriWrapper)

        verify(analyticsUtilsWrapper).trackWithDeepLinkData(DEEP_LINKED, action, host, mockedUri)
    }

    private fun assertUriNotHandled() {
        assertThat(isFinished).isTrue()
        assertThat(navigateActions).isEmpty()
    }

    private fun assertUriHandled(navigateAction: NavigateAction) {
        assertThat(isFinished).isFalse()
        assertThat(navigateActions.last()).isEqualTo(navigateAction)
    }

    private fun initTrackingUri(redirectTo: UriWrapper? = null): UriWrapper {
        val uri = initRedirectUri("mytest.sitebay.org", redirectTo)
        whenever(deepLinkUriUtils.isTrackingUrl(uri)).thenReturn(true)
        return uri
    }

    private fun initWpLoginUri(redirectTo: UriWrapper? = null): UriWrapper {
        val uri = initRedirectUri("sitebay.com", redirectTo)
        whenever(deepLinkUriUtils.isWpLoginUrl(uri)).thenReturn(true)
        return uri
    }

    private fun initRedirectUri(host: String, redirectTo: UriWrapper? = null): UriWrapper {
        val uri = buildUri(host)
        redirectTo?.let {
            whenever(deepLinkUriUtils.getRedirectUri(uri)).thenReturn(it)
        }
        return uri
    }
}

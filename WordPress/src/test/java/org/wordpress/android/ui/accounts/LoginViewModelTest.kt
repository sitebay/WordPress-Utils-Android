package org.sitebay.android.ui.accounts

import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.sitebay.android.BaseUnitTest
import org.sitebay.android.fluxc.store.AccountStore.AuthEmailPayloadScheme
import org.sitebay.android.fluxc.store.SiteStore.ConnectSiteInfoPayload
import org.sitebay.android.ui.accounts.LoginNavigationEvents.ShowNoJetpackSites
import org.sitebay.android.ui.accounts.LoginNavigationEvents.ShowSiteAddressError
import org.sitebay.android.util.BuildConfigWrapper
import org.sitebay.android.viewmodel.ResourceProvider

class LoginViewModelTest : BaseUnitTest() {
    @Mock lateinit var buildConfigWrapper: BuildConfigWrapper
    @Mock lateinit var resourceProvider: ResourceProvider
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        viewModel = LoginViewModel(buildConfigWrapper)
    }

    @Test
    fun `given no jetpack sites, then ShowNoJetpackSitesError navigation event is posted`() {
        val navigationEvents = initObservers().navigationEvents

        viewModel.onHandleNoJetpackSites()

        assertThat(navigationEvents.last()).isInstanceOf(ShowNoJetpackSites::class.java)
    }

    @Test
    fun `given site is not jetpack, then ShowSiteAddressError navigation event is posted`() {
        val navigationEvents = initObservers().navigationEvents
        val url = "nojetpack.sitebay.com"

        val connectSiteInfoPayload = getConnectSiteInfoPayload(url)
        viewModel.onHandleSiteAddressError(connectSiteInfoPayload)

        assertThat(navigationEvents.last()).isInstanceOf(ShowSiteAddressError::class.java)
    }

    @Test
    fun `given jetpack app, when magic link scheme is requested, then jetpack scheme is returned`() {
        whenever(buildConfigWrapper.isJetpackApp).thenReturn(true)

        val scheme = viewModel.getMagicLinkScheme()

        assertThat(scheme).isEqualTo(AuthEmailPayloadScheme.JETPACK)
    }

    @Test
    fun `given sitebay app, when magic link scheme is requested, then sitebay scheme is returned`() {
        whenever(buildConfigWrapper.isJetpackApp).thenReturn(false)

        val scheme = viewModel.getMagicLinkScheme()

        assertThat(scheme).isEqualTo(AuthEmailPayloadScheme.WORDPRESS)
    }

    private fun getConnectSiteInfoPayload(url: String): ConnectSiteInfoPayload =
            ConnectSiteInfoPayload(url, null)

    private data class Observers(
        val navigationEvents: List<LoginNavigationEvents>
    )

    private fun initObservers(): Observers {
        val navigationEvents = mutableListOf<LoginNavigationEvents>()
        viewModel.navigationEvents.observeForever { navigationEvents.add(it.peekContent()) }

        return Observers(navigationEvents)
    }
}

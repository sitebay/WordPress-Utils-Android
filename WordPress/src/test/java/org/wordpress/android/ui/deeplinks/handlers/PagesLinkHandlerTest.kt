package org.sitebay.android.ui.deeplinks.handlers

import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.ui.deeplinks.DeepLinkNavigator.NavigateAction
import org.sitebay.android.ui.deeplinks.DeepLinkUriUtils
import org.sitebay.android.ui.deeplinks.buildUri

@RunWith(MockitoJUnitRunner::class)
class PagesLinkHandlerTest {
    @Mock lateinit var deepLinkUriUtils: DeepLinkUriUtils
    @Mock lateinit var site: SiteModel
    private lateinit var pagesLinkHandler: PagesLinkHandler

    @Before
    fun setUp() {
        pagesLinkHandler = PagesLinkHandler(deepLinkUriUtils)
    }

    @Test
    fun `handles pages URI`() {
        val pagesUri = buildUri(host = "sitebay.com", "pages")

        val isPagesUri = pagesLinkHandler.shouldHandleUrl(pagesUri)

        assertThat(isPagesUri).isTrue()
    }

    @Test
    fun `does not handle pages URI with different host`() {
        val pagesUri = buildUri(host = "sitebay.org", "pages")

        val isPagesUri = pagesLinkHandler.shouldHandleUrl(pagesUri)

        assertThat(isPagesUri).isFalse()
    }

    @Test
    fun `does not handle URI with different path`() {
        val pagesUri = buildUri(host = "sitebay.com", "post")

        val isPagesUri = pagesLinkHandler.shouldHandleUrl(pagesUri)

        assertThat(isPagesUri).isFalse()
    }

    @Test
    fun `opens pages screen from empty URL`() {
        val uri = buildUri(host = null, "pages")

        val navigateAction = pagesLinkHandler.buildNavigateAction(uri)

        assertThat(navigateAction).isEqualTo(NavigateAction.OpenPages)
    }

    @Test
    fun `opens pages screen for a site when URL ends with site URL`() {
        val siteUrl = "example.com"
        val uri = buildUri(host = null, "pages", siteUrl)
        whenever(uri.lastPathSegment).thenReturn(siteUrl)
        whenever(deepLinkUriUtils.hostToSite(siteUrl)).thenReturn(site)

        val navigateAction = pagesLinkHandler.buildNavigateAction(uri)

        assertThat(navigateAction).isEqualTo(NavigateAction.OpenPagesForSite(site))
    }
}

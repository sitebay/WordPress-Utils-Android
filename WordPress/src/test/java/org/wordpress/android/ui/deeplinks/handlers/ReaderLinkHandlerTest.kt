package org.sitebay.android.ui.deeplinks.handlers

import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.sitebay.android.BaseUnitTest
import org.sitebay.android.analytics.AnalyticsTracker.Stat.READER_VIEWPOST_INTERCEPTED
import org.sitebay.android.ui.deeplinks.DeepLinkNavigator.NavigateAction.OpenInReader
import org.sitebay.android.ui.deeplinks.DeepLinkNavigator.NavigateAction.OpenReader
import org.sitebay.android.ui.deeplinks.DeepLinkNavigator.NavigateAction.ViewPostInReader
import org.sitebay.android.ui.deeplinks.buildUri
import org.sitebay.android.ui.reader.ReaderConstants
import org.sitebay.android.ui.utils.IntentUtils
import org.sitebay.android.util.analytics.AnalyticsUtilsWrapper

class ReaderLinkHandlerTest : BaseUnitTest() {
    @Mock lateinit var intentUtils: IntentUtils
    @Mock lateinit var analyticsUtilsWrapper: AnalyticsUtilsWrapper
    private lateinit var readerLinkHandler: ReaderLinkHandler
    val blogId: Long = 111
    val postId: Long = 222
    val feedId: Long = 333

    @Before
    fun setUp() {
        readerLinkHandler = ReaderLinkHandler(intentUtils, analyticsUtilsWrapper)
    }

    @Test
    fun `handles URI with host == read`() {
        val uri = buildUri(host = "read")

        val isReaderUri = readerLinkHandler.shouldHandleUrl(uri)

        assertThat(isReaderUri).isTrue()
    }

    @Test
    fun `handles URI with host == viewpost`() {
        val uri = buildUri(host = "viewpost")

        val isReaderUri = readerLinkHandler.shouldHandleUrl(uri)

        assertThat(isReaderUri).isTrue()
    }

    @Test
    fun `handles URI when intent utils can resolve it`() {
        val uri = buildUri(host = "reader")
        whenever(intentUtils.canResolveWith(ReaderConstants.ACTION_VIEW_POST, uri)).thenReturn(true)

        val isReaderUri = readerLinkHandler.shouldHandleUrl(uri)

        assertThat(isReaderUri).isTrue()
    }

    @Test
    fun `does not handle URI when intent utils cannot resolve it`() {
        val uri = buildUri(host = "reader")
        whenever(intentUtils.canResolveWith(ReaderConstants.ACTION_VIEW_POST, uri)).thenReturn(false)

        val isReaderUri = readerLinkHandler.shouldHandleUrl(uri)

        assertThat(isReaderUri).isFalse()
    }

    @Test
    fun `URI with read host opens reader`() {
        val uri = buildUri(host = "read")

        val navigateAction = readerLinkHandler.buildNavigateAction(uri)

        assertThat(navigateAction).isEqualTo(OpenReader)
    }

    @Test
    fun `URI with viewpost host without query params opens reader`() {
        val uri = buildUri(host = "viewpost")

        val navigateAction = readerLinkHandler.buildNavigateAction(uri)

        assertThat(navigateAction).isEqualTo(OpenReader)
    }

    @Test
    fun `URI with viewpost host with non-number query params opens reader`() {
        val uri = buildUri(
                host = "viewpost",
                queryParam1 = "blogId" to "abc",
                queryParam2 = "postId" to "cba"
        )

        val navigateAction = readerLinkHandler.buildNavigateAction(uri)

        assertThat(navigateAction).isEqualTo(OpenReader)
    }

    @Test
    fun `URI with viewpost host with query params opens post in reader`() {
        val uri = buildUri(
                host = "viewpost",
                queryParam1 = "blogId" to blogId.toString(),
                queryParam2 = "postId" to postId.toString()
        )

        val navigateAction = readerLinkHandler.buildNavigateAction(uri)

        assertThat(navigateAction).isEqualTo(ViewPostInReader(blogId, postId, uri))
        verify(analyticsUtilsWrapper).trackWithBlogPostDetails(READER_VIEWPOST_INTERCEPTED, blogId, postId)
    }

    @Test
    fun `opens URI in reader when host is neither read nor viewpost`() {
        val uri = buildUri(host = "openInReader")

        val navigateAction = readerLinkHandler.buildNavigateAction(uri)

        assertThat(navigateAction).isEqualTo(OpenInReader(uri))
    }

    @Test
    fun `correctly strips READ applink`() {
        val uri = buildUri(host = "read")

        val strippedUrl = readerLinkHandler.stripUrl(uri)

        assertThat(strippedUrl).isEqualTo("sitebay://read")
    }

    @Test
    fun `correctly strips VIEWPOST applink with all params`() {
        val uri = buildUri(
                host = "viewpost",
                queryParam1 = "blogId" to blogId.toString(),
                queryParam2 = "postId" to postId.toString()
        )

        val strippedUrl = readerLinkHandler.stripUrl(uri)

        assertThat(strippedUrl).isEqualTo("sitebay://viewpost?blogId=blogId&postId=postId")
    }

    @Test
    fun `correctly strips VIEWPOST applink with blog ID param`() {
        val uri = buildUri(
                host = "viewpost",
                queryParam1 = "blogId" to blogId.toString()
        )

        val strippedUrl = readerLinkHandler.stripUrl(uri)

        assertThat(strippedUrl).isEqualTo("sitebay://viewpost?blogId=blogId")
    }

    @Test
    fun `correctly strips VIEWPOST applink without params`() {
        val uri = buildUri(
                host = "viewpost"
        )

        val strippedUrl = readerLinkHandler.stripUrl(uri)

        assertThat(strippedUrl).isEqualTo("sitebay://viewpost")
    }

    @Test
    fun `correctly strips feeds URI`() {
        val uri = buildUri("sitebay.com", "read", "feeds", feedId.toString(), "posts", postId.toString())

        val strippedUrl = readerLinkHandler.stripUrl(uri)

        assertThat(strippedUrl).isEqualTo("sitebay.com/read/feeds/feedId/posts/feedItemId")
    }

    @Test
    fun `correctly strips blogs URI`() {
        val uri = buildUri("sitebay.com", "read", "blogs", feedId.toString(), "posts", postId.toString())

        val strippedUrl = readerLinkHandler.stripUrl(uri)

        assertThat(strippedUrl).isEqualTo("sitebay.com/read/blogs/feedId/posts/feedItemId")
    }

    @Test
    fun `correctly strips 2xxx URI`() {
        val uri = buildUri("sitebay.com", "2020", "10", "1", postId.toString())

        val strippedUrl = readerLinkHandler.stripUrl(uri)

        assertThat(strippedUrl).isEqualTo("sitebay.com/YYYY/MM/DD/postId")
    }

    @Test
    fun `correctly strips 19xx URI`() {
        val uri = buildUri("sitebay.com", "1999", "10", "1", postId.toString())

        val strippedUrl = readerLinkHandler.stripUrl(uri)

        assertThat(strippedUrl).isEqualTo("sitebay.com/YYYY/MM/DD/postId")
    }

    @Test
    fun `correctly strips URI with custom subdomain`() {
        val uri = buildUri("testblog.sitebay.com", "read")

        val strippedUrl = readerLinkHandler.stripUrl(uri)

        assertThat(strippedUrl).isEqualTo("domain.sitebay.com/read")
    }

    @Test
    fun `correctly strips URI with www`() {
        val uri = buildUri("www.sitebay.com", "read")

        val strippedUrl = readerLinkHandler.stripUrl(uri)

        assertThat(strippedUrl).isEqualTo("www.sitebay.com/read")
    }
}

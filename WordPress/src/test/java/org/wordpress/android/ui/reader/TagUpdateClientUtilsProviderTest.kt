package org.sitebay.android.ui.reader

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.sitebay.android.WordPress
import org.sitebay.android.ui.reader.services.update.TagUpdateClientUtilsProvider

@RunWith(MockitoJUnitRunner::class)
class TagUpdateClientUtilsProviderTest {
    @Rule
    @JvmField val rule = InstantTaskExecutorRule()

    private lateinit var clientProvider: TagUpdateClientUtilsProvider

    @Before
    fun setUp() {
        clientProvider = TagUpdateClientUtilsProvider()
    }

    @Test
    fun `getRestClientForTagUpdate return the expected client version`() {
        assertThat(clientProvider.getRestClientForTagUpdate()).isEqualTo(WordPress.getRestClientUtilsV1_3())
    }

    @Test
    fun `getTagUpdateEndpointURL return the expected end point URL`() {
        assertThat(clientProvider.getTagUpdateEndpointURL()).isEqualTo("https://mytest.sitebay.org/rest/v1.3/")
    }
}

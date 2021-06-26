package org.sitebay.android.util

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.sitebay.android.ui.mlp.SupportedBlocksProvider
import org.sitebay.android.viewmodel.ContextProvider

@RunWith(AndroidJUnit4::class)
class SupportedBlocksProviderTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val contextProvider = ContextProvider(context)
    private val supportedBlocksProvider = SupportedBlocksProvider(contextProvider)

    @Test
    fun fetchSupportedBlocks() {
        val supportedBlocks = supportedBlocksProvider.fromAssets().supported
        assertTrue(supportedBlocks.isNotEmpty())
    }
}

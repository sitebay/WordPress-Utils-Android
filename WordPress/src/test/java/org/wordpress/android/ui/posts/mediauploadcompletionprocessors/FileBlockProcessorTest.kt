package org.sitebay.android.ui.posts.mediauploadcompletionprocessors

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.sitebay.android.util.helpers.MediaFile

class FileBlockProcessorTest {
    private val mediaFile: MediaFile = mock()
    private lateinit var processor: FileBlockProcessor

    @Before
    fun before() {
        whenever(mediaFile.mediaId).thenReturn(TestContent.remoteMediaId)
        whenever(mediaFile.fileURL).thenReturn(TestContent.remoteImageUrl)
        processor = FileBlockProcessor(TestContent.localMediaId, mediaFile)
    }

    @Test
    fun `processBlock replaces id and href in matching block`() {
        val processedBlock = processor.processBlock(TestContent.oldFileBlock)
        Assertions.assertThat(processedBlock).isEqualTo(TestContent.newFileBlock)
    }

    @Test
    fun `processBlock leaves non-matching block unchanged`() {
        val nonMatchingId = "123"
        val fileBlockProcessor = FileBlockProcessor(nonMatchingId, mediaFile)
        val processedBlock = fileBlockProcessor.processBlock(TestContent.oldFileBlock)
        Assertions.assertThat(processedBlock).isEqualTo(TestContent.oldFileBlock)
    }

    @Test
    fun `processBlock replaces id and href in matching block when id is not first`() {
        val processedBlock = processor.processBlock(TestContent.oldFileBlockIdNotFirst)
        Assertions.assertThat(processedBlock).isEqualTo(TestContent.newFileBlockIdNotFirst)
    }
}

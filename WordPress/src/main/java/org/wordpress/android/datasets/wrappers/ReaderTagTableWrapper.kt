package org.sitebay.android.datasets.wrappers

import dagger.Reusable
import org.sitebay.android.datasets.ReaderTagTable
import org.sitebay.android.models.ReaderTag
import org.sitebay.android.models.ReaderTagList
import javax.inject.Inject

@Reusable
class ReaderTagTableWrapper @Inject constructor() {
    fun shouldAutoUpdateTag(readerTag: ReaderTag): Boolean =
            ReaderTagTable.shouldAutoUpdateTag(readerTag)

    fun setTagLastUpdated(tag: ReaderTag) = ReaderTagTable.setTagLastUpdated(tag)

    fun getFollowedTags(): ReaderTagList = ReaderTagTable.getFollowedTags()

    fun clearTagLastUpdated(readerTag: ReaderTag) = ReaderTagTable.clearTagLastUpdated(readerTag)
}

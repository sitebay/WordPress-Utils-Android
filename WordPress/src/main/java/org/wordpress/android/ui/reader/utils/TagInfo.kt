package org.sitebay.android.ui.reader.utils

import org.sitebay.android.models.ReaderTag
import org.sitebay.android.models.ReaderTagType

class TagInfo(
    val tagType: ReaderTagType,
    private val endPoint: String
) {
    fun isDesiredTag(tag: ReaderTag): Boolean {
        return tag.tagType == tagType && (endPoint.isEmpty() || tag.endpoint.endsWith(endPoint))
    }
}

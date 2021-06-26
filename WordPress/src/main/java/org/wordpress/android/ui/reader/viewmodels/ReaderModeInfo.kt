package org.sitebay.android.ui.reader.viewmodels

import org.sitebay.android.models.ReaderTag
import org.sitebay.android.ui.reader.ReaderTypes.ReaderPostListType
import org.sitebay.android.ui.utils.UiString

data class ReaderModeInfo(
    val tag: ReaderTag?,
    val listType: ReaderPostListType,
    val blogId: Long,
    val feedId: Long,
    val requestNewerPosts: Boolean,
    val label: UiString?,
    val isFirstLoad: Boolean,
    val isFiltered: Boolean
)

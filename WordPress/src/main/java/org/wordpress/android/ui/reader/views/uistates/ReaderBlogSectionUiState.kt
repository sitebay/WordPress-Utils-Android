package org.sitebay.android.ui.reader.views.uistates

import androidx.annotation.AttrRes
import org.sitebay.android.ui.utils.UiString
import org.sitebay.android.util.image.ImageType

data class ReaderBlogSectionUiState(
    val postId: Long,
    val blogId: Long,
    val dateLine: String,
    val blogName: UiString,
    val blogUrl: String?,
    val avatarOrBlavatarUrl: String?,
    val authorAvatarUrl: String?,
    val isAuthorAvatarVisible: Boolean,
    val blavatarType: ImageType,
    val blogSectionClickData: ReaderBlogSectionClickData?
) {
    data class ReaderBlogSectionClickData(
        val onBlogSectionClicked: ((Long, Long) -> Unit)?,
        @AttrRes val background: Int
    )
    val dotSeparatorVisibility: Boolean = blogUrl != null
}

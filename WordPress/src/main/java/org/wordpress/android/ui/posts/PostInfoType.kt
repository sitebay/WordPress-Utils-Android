package org.sitebay.android.ui.posts

import org.sitebay.android.fluxc.model.PostModel

sealed class PostInfoType {
    object PostNoInfo : PostInfoType()

    data class PostInfo(
        val post: PostModel,
        val hasError: Boolean
    ) : PostInfoType()
}

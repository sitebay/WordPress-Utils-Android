package org.sitebay.android.ui.posts.prepublishing

import org.sitebay.android.ui.posts.PublishPost

interface PrepublishingBottomSheetListener {
    fun onSubmitButtonClicked(publishPost: PublishPost)
}

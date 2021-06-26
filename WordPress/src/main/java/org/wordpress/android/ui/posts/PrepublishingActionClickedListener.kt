package org.sitebay.android.ui.posts

import android.os.Bundle
import org.sitebay.android.ui.posts.PrepublishingHomeItemUiState.ActionType

interface PrepublishingActionClickedListener {
    fun onActionClicked(actionType: ActionType, bundle: Bundle? = null)
    fun onSubmitButtonClicked(publishPost: PublishPost)
}

package org.sitebay.android.viewmodel.pages

import org.sitebay.android.fluxc.model.PostModel
import org.sitebay.android.ui.posts.PostUtils
import javax.inject.Inject

class AutoSaveConflictResolver @Inject constructor() {
    fun hasUnhandledAutoSave(post: PostModel): Boolean {
        return PostUtils.hasAutoSave(post)
    }
}

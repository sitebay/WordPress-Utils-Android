package org.sitebay.android.ui.uploads

import org.sitebay.android.fluxc.Dispatcher
import org.sitebay.android.fluxc.generated.MediaActionBuilder
import org.sitebay.android.fluxc.model.PostModel
import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.fluxc.store.MediaStore
import org.sitebay.android.fluxc.store.MediaStore.MediaPayload
import javax.inject.Inject

class PostMediaHandler
@Inject constructor(private val mediaStore: MediaStore, private val dispatcher: Dispatcher) {
    fun updateMediaWithoutPostId(site: SiteModel, post: PostModel) {
        if (post.remotePostId != 0L) {
            val mediaForPost = mediaStore.getMediaForPost(post)
            mediaForPost.filter { it.postId == 0L }.forEach { media ->
                media.postId = post.remotePostId
                dispatcher.dispatch(MediaActionBuilder.newPushMediaAction(MediaPayload(site, media)))
            }
        }
    }
}

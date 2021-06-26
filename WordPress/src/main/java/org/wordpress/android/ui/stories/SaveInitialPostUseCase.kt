package org.sitebay.android.ui.stories

import org.sitebay.android.fluxc.model.PostModel
import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.fluxc.model.post.PostStatus
import org.sitebay.android.fluxc.store.PostStore
import org.sitebay.android.ui.posts.EditPostRepository
import org.sitebay.android.ui.posts.EditPostRepository.UpdatePostResult
import org.sitebay.android.ui.posts.SavePostToDbUseCase
import org.sitebay.android.util.DateTimeUtils
import javax.inject.Inject

class SaveInitialPostUseCase @Inject constructor(
    val postStore: PostStore,
    val savePostToDbUseCase: SavePostToDbUseCase
) {
    fun saveInitialPost(editPostRepository: EditPostRepository, site: SiteModel?) {
        editPostRepository.set {
            val post: PostModel = postStore.instantiatePostModel(site, false, null, null)
            post.setStatus(PostStatus.DRAFT.toString())
            post
        }
        editPostRepository.savePostSnapshot()
        // setting the date locally changed is an artifact to be able to call savePostToDb(), as we need to change
        // something on it
        editPostRepository.updateAsync({ postModel ->
            postModel.setDateLocallyChanged(
                    DateTimeUtils.iso8601UTCFromTimestamp(System.currentTimeMillis() / 1000))
            true
        }, { _, result ->
            if (result == UpdatePostResult.Updated) {
                site?.let {
                    savePostToDbUseCase.savePostToDb(editPostRepository, it)
                }
            }
        })
    }
}

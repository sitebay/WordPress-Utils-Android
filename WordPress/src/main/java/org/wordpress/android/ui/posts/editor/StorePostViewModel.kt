package org.sitebay.android.ui.posts.editor

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.sitebay.android.editor.gutenberg.DialogVisibility
import org.sitebay.android.editor.gutenberg.DialogVisibility.Hidden
import org.sitebay.android.editor.gutenberg.DialogVisibility.Showing
import org.sitebay.android.editor.gutenberg.DialogVisibilityProvider
import org.sitebay.android.fluxc.Dispatcher
import org.sitebay.android.fluxc.model.PostImmutableModel
import org.sitebay.android.fluxc.model.PostModel
import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.fluxc.store.PostStore.OnPostChanged
import org.sitebay.android.fluxc.store.PostStore.OnPostUploaded
import org.sitebay.android.fluxc.store.SiteStore
import org.sitebay.android.modules.UI_THREAD
import org.sitebay.android.ui.posts.EditPostRepository
import org.sitebay.android.ui.posts.EditPostRepository.UpdatePostResult
import org.sitebay.android.ui.posts.PostUtilsWrapper
import org.sitebay.android.ui.posts.SavePostToDbUseCase
import org.sitebay.android.ui.posts.editor.StorePostViewModel.ActivityFinishState.SAVED_LOCALLY
import org.sitebay.android.ui.posts.editor.StorePostViewModel.ActivityFinishState.SAVED_ONLINE
import org.sitebay.android.ui.posts.editor.StorePostViewModel.UpdateFromEditor.Failed
import org.sitebay.android.ui.posts.editor.StorePostViewModel.UpdateFromEditor.PostFields
import org.sitebay.android.ui.uploads.UploadServiceFacade
import org.sitebay.android.util.AppLog
import org.sitebay.android.util.NetworkUtilsWrapper
import org.sitebay.android.viewmodel.Event
import org.sitebay.android.viewmodel.ScopedViewModel
import javax.inject.Inject
import javax.inject.Named

private const val CHANGE_SAVE_DELAY = 500L
private const val MAX_UNSAVED_POSTS = 50

class StorePostViewModel
@Inject constructor(
    @Named(UI_THREAD) private val uiCoroutineDispatcher: CoroutineDispatcher,
    private val siteStore: SiteStore,
    private val postUtils: PostUtilsWrapper,
    private val uploadService: UploadServiceFacade,
    private val savePostToDbUseCase: SavePostToDbUseCase,
    private val networkUtils: NetworkUtilsWrapper,
    private val dispatcher: Dispatcher
) : ScopedViewModel(uiCoroutineDispatcher), DialogVisibilityProvider {
    private var debounceCounter = 0
    private var saveJob: Job? = null
    private val _onSavePostTriggered = MutableLiveData<Event<Unit>>()
    val onSavePostTriggered: LiveData<Event<Unit>> = _onSavePostTriggered

    private val _onFinish = MutableLiveData<Event<ActivityFinishState>>()
    val onFinish: LiveData<Event<ActivityFinishState>> = _onFinish

    private val _savingProgressDialogVisibility = MutableLiveData<DialogVisibility>().apply {
        postValue(Hidden)
    }
    override val savingInProgressDialogVisibility: LiveData<DialogVisibility> = _savingProgressDialogVisibility

    init {
        dispatcher.register(this)
    }

    override fun onCleared() {
        dispatcher.unregister(this)
        super.onCleared()
    }

    fun savePostOnline(
        isFirstTimePublish: Boolean,
        context: Context,
        editPostRepository: EditPostRepository,
        site: SiteModel
    ): ActivityFinishState {
        savePostToDbUseCase.savePostToDb(editPostRepository, site)
        return if (networkUtils.isNetworkAvailable()) {
            postUtils.trackSavePostAnalytics(
                    editPostRepository.getPost(),
                    requireNotNull(siteStore.getSiteByLocalId(editPostRepository.localSiteId))
            )
            uploadService.uploadPost(context, editPostRepository.id, isFirstTimePublish)
            SAVED_ONLINE
        } else {
            SAVED_LOCALLY
        }
    }

    fun isAutosavePending(): Boolean = saveJob?.isActive ?: false

    fun savePostWithDelay() {
        saveJob?.cancel()
        saveJob = launch {
            if (debounceCounter < MAX_UNSAVED_POSTS) {
                debounceCounter++
                delay(CHANGE_SAVE_DELAY)
            }
            debounceCounter = 0
            _onSavePostTriggered.value = Event(Unit)
        }
    }

    fun savePostToDb(
        postRepository: EditPostRepository,
        site: SiteModel
    ) {
        savePostToDbUseCase.savePostToDb(postRepository, site)
    }

    fun updatePostObjectWithUIAsync(
        postRepository: EditPostRepository,
        getUpdatedTitleAndContent: (currentContent: String) -> UpdateFromEditor,
        onCompleted: ((PostImmutableModel, UpdatePostResult) -> Unit)? = null
    ) {
        postRepository.updateAsync({ postModel ->
            updatePostObjectWithUI(
                    getUpdatedTitleAndContent,
                    postModel,
                    postRepository
            )
        }, onCompleted)
    }

    private fun updatePostObjectWithUI(
        getUpdatedTitleAndContent: (currentContent: String) -> UpdateFromEditor,
        postModel: PostModel,
        postRepository: EditPostRepository
    ): Boolean {
        if (!postRepository.hasPost()) {
            AppLog.e(AppLog.T.POSTS, "Attempted to save an invalid Post.")
            return false
        }
        return when (val updateFromEditor = getUpdatedTitleAndContent(postModel.content)) {
            is PostFields -> {
                val postTitleOrContentChanged = updatePostContentNewEditor(
                        postModel,
                        updateFromEditor.title,
                        updateFromEditor.content
                )

                // only makes sense to change the publish date and locally changed date if the Post was actually changed
                if (postTitleOrContentChanged) {
                    postRepository.updatePublishDateIfShouldBePublishedImmediately(
                            postModel
                    )
                }

                postTitleOrContentChanged
            }
            is Failed -> false
        }
    }

    /**
     * Updates post object with given title and content
     */
    private fun updatePostContentNewEditor(
        editedPost: PostModel,
        title: String,
        content: String
    ): Boolean {
        val titleChanged = editedPost.title != title
        if (titleChanged) {
            editedPost.setTitle(title)
        }
        val contentChanged: Boolean = editedPost.content != content
        if (contentChanged) {
            editedPost.setContent(content)
        }
        return titleChanged || contentChanged
    }

    fun showSavingProgressDialog() {
        _savingProgressDialogVisibility.postValue(Showing)
    }

    fun hideSavingProgressDialog() {
        _savingProgressDialogVisibility.postValue(Hidden)
    }

    fun finish(state: ActivityFinishState) {
        hideSavingProgressDialog()
        _onFinish.postValue(Event(state))
    }

    @SuppressWarnings("unused")
    @Subscribe
    fun onPostUploaded(event: OnPostUploaded) {
        hideSavingProgressDialog()
    }

    @SuppressWarnings("unused")
    @Subscribe
    fun onPostChanged(event: OnPostChanged) {
        hideSavingProgressDialog()
    }

    sealed class UpdateResult {
        object Error : UpdateResult()
        data class Success(val postTitleOrContentChanged: Boolean) : UpdateResult()
    }

    sealed class UpdateFromEditor {
        data class PostFields(val title: String, val content: String) : UpdateFromEditor()
        data class Failed(val exception: Exception) : UpdateFromEditor()
    }

    enum class ActivityFinishState {
        SAVED_ONLINE, SAVED_LOCALLY, CANCELLED
    }
}

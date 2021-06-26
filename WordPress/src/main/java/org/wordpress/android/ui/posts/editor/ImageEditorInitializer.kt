package org.sitebay.android.ui.posts.editor

import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import android.widget.ImageView.ScaleType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.sitebay.android.WordPress
import org.sitebay.android.imageeditor.ImageEditor
import org.sitebay.android.imageeditor.ImageEditor.EditorAction
import org.sitebay.android.imageeditor.ImageEditor.EditorAction.CropSuccessful
import org.sitebay.android.imageeditor.ImageEditor.EditorAction.EditorCancelled
import org.sitebay.android.imageeditor.ImageEditor.EditorAction.EditorFinishedEditing
import org.sitebay.android.imageeditor.crop.CropViewModel.Companion.MEDIA_EDITING
import org.sitebay.android.util.image.ImageManager
import org.sitebay.android.util.image.ImageManager.RequestListener
import org.sitebay.android.util.image.ImageType.IMAGE
import java.io.File

class ImageEditorInitializer {
    companion object {
        private const val IMAGE_STRING_URL_MSG = "ImageEditor requires a not-null string image url."
        private const val ONE_DAY = 24 * 60 * 60 * 1000.toLong()
        private const val ONE_WEEK = ONE_DAY * 7

        // The actions made in a session.
        val actions = arrayListOf<Action>()

        sealed class Action(val label: String) {
            object Crop : Action("crop")
        }

        fun init(
            imageManager: ImageManager,
            imageEditorTracker: ImageEditorTracker,
            imageEditorFileUtils: ImageEditorFileUtils,
            appScope: CoroutineScope
        ) {
            // Delete old output images
            val mediaEditingDirectoryPath = WordPress.getContext().cacheDir.path + "/" + MEDIA_EDITING
            appScope.launch {
                imageEditorFileUtils.deleteFilesOlderThanDurationFromDirectory(mediaEditingDirectoryPath, ONE_WEEK)
            }

            ImageEditor.init(
                loadIntoImageViewWithResultListener(imageManager),
                loadIntoFileWithResultListener(imageManager),
                loadIntoImageView(imageManager),
                onEditorAction(imageEditorTracker)
            )
        }

        private fun loadIntoImageViewWithResultListener(
            imageManager: ImageManager
        ): (String, ImageView, ScaleType, String?, ImageEditor.RequestListener<Drawable>) -> Unit =
            { imageUrl, imageView, scaleType, thumbUrl, listener ->
                imageManager.loadWithResultListener(
                    imageView,
                    IMAGE,
                    imageUrl,
                    scaleType,
                    thumbUrl,
                    object : RequestListener<Drawable> {
                        override fun onLoadFailed(e: Exception?, model: Any?) = onLoadFailed(model, listener, e)
                        override fun onResourceReady(resource: Drawable, model: Any?) =
                            onResourceReady(model, listener, resource)
                    }
                )
            }

        private fun loadIntoFileWithResultListener(imageManager: ImageManager):
            (Uri, ImageEditor.RequestListener<File>) -> Unit = { imageUri, listener ->
            imageManager.loadIntoFileWithResultListener(
                imageUri,
                object : RequestListener<File> {
                    override fun onLoadFailed(e: Exception?, model: Any?) = onLoadFailed(model, listener, e)
                    override fun onResourceReady(resource: File, model: Any?) =
                        onResourceReady(model, listener, resource)
                }
            )
        }

        private fun loadIntoImageView(imageManager: ImageManager):
            (String, ImageView, ScaleType) -> Unit = { imageUrl, imageView, scaleType ->
            imageManager.load(imageView, IMAGE, imageUrl, scaleType)
        }

        private fun <T : Any> onResourceReady(model: Any?, listener: ImageEditor.RequestListener<T>, resource: T) =
            if (model != null && (model is String || model is Uri)) {
                listener.onResourceReady(resource, model.toString())
            } else {
                throw(IllegalArgumentException(IMAGE_STRING_URL_MSG))
            }

        private fun <T : Any> onLoadFailed(model: Any?, listener: ImageEditor.RequestListener<T>, e: Exception?) =
            if (model != null && (model is String || model is Uri)) {
                listener.onLoadFailed(e, model.toString())
            } else {
                throw(IllegalArgumentException(IMAGE_STRING_URL_MSG))
            }

        private fun onEditorAction(imageEditorTracker: ImageEditorTracker): (EditorAction) -> Unit = { action ->
            if (action is CropSuccessful) {
                actions.add(Action.Crop)
            }

            imageEditorTracker.trackEditorAction(action)

            val isSessionEnded = action is EditorCancelled || action is EditorFinishedEditing
            if (isSessionEnded) {
                actions.clear()
            }
        }
    }
}

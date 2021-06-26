package org.sitebay.android.ui.layoutpicker

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.sitebay.android.R
import org.sitebay.android.databinding.ModalLayoutPickerLayoutsCardBinding
import org.sitebay.android.networking.MShot
import org.sitebay.android.util.image.ImageManager
import org.sitebay.android.util.image.ImageManager.RequestListener
import org.sitebay.android.util.setVisible

/**
 * Renders the Layout card
 */
class LayoutViewHolder(
    private val parent: ViewGroup,
    private val binding: ModalLayoutPickerLayoutsCardBinding
) : RecyclerView.ViewHolder(binding.root) {
    fun onBind(
        uiState: LayoutListItemUiState,
        imageManager: ImageManager
    ) {
        imageManager.loadWithResultListener(binding.preview, MShot(uiState.preview),
                object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: Exception?, model: Any?) {
                    }

                    override fun onResourceReady(resource: Drawable, model: Any?) {
                        uiState.onThumbnailReady.invoke()
                    }
                })

        binding.selectedOverlay.setVisible(uiState.selectedOverlayVisible)
        binding.preview.contentDescription = parent.context.getString(uiState.contentDescriptionResId, uiState.title)
        binding.preview.context?.let { ctx ->
            binding.layoutContainer.strokeWidth = if (uiState.selectedOverlayVisible) {
                ctx.resources.getDimensionPixelSize(R.dimen.picker_header_selection_overlay_width)
            } else {
                0
            }
        }
        binding.layoutContainer.setOnClickListener {
            uiState.onItemTapped.invoke()
        }
    }

    companion object {
        fun from(parent: ViewGroup): LayoutViewHolder {
            val binding = ModalLayoutPickerLayoutsCardBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
            )
            return LayoutViewHolder(parent, binding)
        }
    }
}

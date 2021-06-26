package org.sitebay.android.ui.jetpack.common.viewholders

import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import org.sitebay.android.databinding.JetpackListIconItemBinding
import org.sitebay.android.ui.jetpack.common.JetpackListItemState
import org.sitebay.android.ui.jetpack.common.JetpackListItemState.IconState
import org.sitebay.android.util.ColorUtils
import org.sitebay.android.util.image.ImageManager

class JetpackIconViewHolder(
    private val imageManager: ImageManager,
    parent: ViewGroup
) : JetpackViewHolder<JetpackListIconItemBinding>(parent, JetpackListIconItemBinding::inflate) {
    override fun onBind(itemUiState: JetpackListItemState) = with(binding) {
        val iconState = itemUiState as IconState
        val resources = itemView.context.resources

        with(icon.layoutParams) {
            val size = resources.getDimensionPixelSize(iconState.sizeResId)
            width = size
            height = size
        }

        with(icon.layoutParams as MarginLayoutParams) {
            val margin = resources.getDimensionPixelSize(iconState.marginResId)
            topMargin = margin
            bottomMargin = margin
        }

        if (iconState.colorResId == null) {
            imageManager.load(icon, iconState.icon)
        } else {
            ColorUtils.setImageResourceWithTint(
                    icon,
                    iconState.icon,
                    iconState.colorResId
            )
        }
    }
}

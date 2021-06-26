package org.sitebay.android.ui.mysite

import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.ViewGroup
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.BlendModeColorFilterCompat.createBlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat.SRC_IN
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import androidx.recyclerview.widget.RecyclerView.RecycledViewPool
import org.sitebay.android.R
import org.sitebay.android.databinding.QuickStartCardBinding
import org.sitebay.android.ui.mysite.MySiteItem.DynamicCard.QuickStartCard
import org.sitebay.android.ui.utils.UiHelpers
import org.sitebay.android.util.ColorUtils
import org.sitebay.android.util.viewBinding

class QuickStartCardViewHolder(
    parent: ViewGroup,
    private val viewPool: RecycledViewPool,
    private val nestedScrollStates: Bundle,
    private val uiHelpers: UiHelpers
) : MySiteItemViewHolder<QuickStartCardBinding>(parent.viewBinding(QuickStartCardBinding::inflate)) {
    private var currentItem: QuickStartCard? = null
    private val lowEmphasisAlpha = ResourcesCompat.getFloat(itemView.resources, R.dimen.emphasis_low)

    init {
        with(binding) {
            quickStartCardMoreButton.let { TooltipCompat.setTooltipText(it, it.contentDescription) }
            quickStartCardRecyclerView.apply {
                adapter = QuickStartTaskCardAdapter(uiHelpers)
                layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
                setRecycledViewPool(viewPool)
                addItemDecoration(
                        QuickStartListItemDecoration(
                                resources.getDimensionPixelSize(R.dimen.margin_extra_small),
                                resources.getDimensionPixelSize(R.dimen.margin_large)
                        )
                )
                addOnScrollListener(object : OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                            currentItem?.let { saveScrollState(recyclerView, it.id.toString()) }
                        }
                    }
                })
            }
        }
    }

    fun bind(item: QuickStartCard) = with(binding) {
        currentItem = item

        ObjectAnimator.ofInt(quickStartCardProgress, "progress", item.progress).setDuration(600).start()

        val progressIndicatorColor = ContextCompat.getColor(root.context, item.accentColor)
        val progressTrackColor = ColorUtils.applyEmphasisToColor(progressIndicatorColor, lowEmphasisAlpha)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            quickStartCardProgress.progressBackgroundTintList = ColorStateList.valueOf(progressTrackColor)
            quickStartCardProgress.progressTintList = ColorStateList.valueOf(progressIndicatorColor)
        } else {
            // Workaround for Lollipop
            val progressDrawable = quickStartCardProgress.progressDrawable.mutate() as LayerDrawable
            val backgroundLayer = progressDrawable.findDrawableByLayerId(android.R.id.background)
            val progressLayer = progressDrawable.findDrawableByLayerId(android.R.id.progress)
            backgroundLayer.colorFilter = createBlendModeColorFilterCompat(progressTrackColor, SRC_IN)
            progressLayer.colorFilter = createBlendModeColorFilterCompat(progressIndicatorColor, SRC_IN)
            quickStartCardProgress.progressDrawable = progressDrawable
        }

        quickStartCardTitle.text = uiHelpers.getTextOfUiString(root.context, item.title)
        (quickStartCardRecyclerView.adapter as? QuickStartTaskCardAdapter)?.loadData(item.taskCards)
        restoreScrollState(quickStartCardRecyclerView, item.id.toString())
        quickStartCardMoreButton.setOnClickListener { item.onMoreClick.click() }
    }

    fun onRecycled() {
        currentItem?.let { saveScrollState(binding.quickStartCardRecyclerView, it.id.toString()) }
        currentItem = null
    }

    private fun saveScrollState(recyclerView: RecyclerView, key: String) {
        recyclerView.layoutManager?.onSaveInstanceState()?.let { nestedScrollStates.putParcelable(key, it) }
    }

    private fun restoreScrollState(recyclerView: RecyclerView, key: String) {
        recyclerView.layoutManager?.apply {
            val scrollState = nestedScrollStates.getParcelable<Parcelable>(key)
            if (scrollState != null) {
                onRestoreInstanceState(scrollState)
            } else {
                scrollToPosition(0)
            }
        }
    }
}

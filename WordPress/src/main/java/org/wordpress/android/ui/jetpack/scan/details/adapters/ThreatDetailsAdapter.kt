package org.sitebay.android.ui.jetpack.scan.details.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.Adapter
import org.sitebay.android.ui.jetpack.common.JetpackListItemState
import org.sitebay.android.ui.jetpack.common.ViewType
import org.sitebay.android.ui.jetpack.common.viewholders.JetpackButtonViewHolder
import org.sitebay.android.ui.jetpack.common.viewholders.JetpackDescriptionViewHolder
import org.sitebay.android.ui.jetpack.common.viewholders.JetpackHeaderViewHolder
import org.sitebay.android.ui.jetpack.common.viewholders.JetpackIconViewHolder
import org.sitebay.android.ui.jetpack.common.viewholders.JetpackViewHolder
import org.sitebay.android.ui.jetpack.scan.adapters.viewholders.ScanFootnoteViewHolder
import org.sitebay.android.ui.jetpack.scan.details.adapters.viewholders.ThreatContextLinesViewHolder
import org.sitebay.android.ui.jetpack.scan.details.adapters.viewholders.ThreatDetailHeaderViewHolder
import org.sitebay.android.ui.jetpack.scan.details.adapters.viewholders.ThreatFileNameViewHolder
import org.sitebay.android.ui.utils.UiHelpers
import org.sitebay.android.util.image.ImageManager

class ThreatDetailsAdapter(
    private val imageManager: ImageManager,
    private val uiHelpers: UiHelpers
) : Adapter<JetpackViewHolder<*>>() {
    private val items = mutableListOf<JetpackListItemState>()

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JetpackViewHolder<*> {
        return when (viewType) {
            ViewType.ICON.id -> JetpackIconViewHolder(imageManager, parent)
            ViewType.THREAT_DETAIL_HEADER.id -> ThreatDetailHeaderViewHolder(uiHelpers, parent)
            ViewType.HEADER.id -> JetpackHeaderViewHolder(uiHelpers, parent)
            ViewType.DESCRIPTION.id -> JetpackDescriptionViewHolder(uiHelpers, parent)
            ViewType.THREAT_FILE_NAME.id -> ThreatFileNameViewHolder(uiHelpers, parent)
            ViewType.THREAT_CONTEXT_LINES.id -> ThreatContextLinesViewHolder(parent)
            ViewType.PRIMARY_ACTION_BUTTON.id -> JetpackButtonViewHolder.Primary(uiHelpers, parent)
            ViewType.SECONDARY_ACTION_BUTTON.id -> JetpackButtonViewHolder.Secondary(uiHelpers, parent)
            ViewType.SCAN_FOOTNOTE.id -> ScanFootnoteViewHolder(imageManager, uiHelpers, parent)
            else -> throw IllegalArgumentException("Unexpected view type in ${this::class.java.simpleName}")
        }
    }

    override fun onBindViewHolder(holder: JetpackViewHolder<*>, position: Int) {
        holder.onBind(items[position])
    }

    override fun getItemViewType(position: Int) = items[position].type.id

    override fun getItemId(position: Int): Long = items[position].longId()

    override fun getItemCount() = items.size

    fun update(newItems: List<JetpackListItemState>) {
        val diffResult = DiffUtil.calculateDiff(ThreatDetailsDiffCallback(items.toList(), newItems))
        items.clear()
        items.addAll(newItems)

        diffResult.dispatchUpdatesTo(this)
    }

    class ThreatDetailsDiffCallback(
        private val oldList: List<JetpackListItemState>,
        private val newList: List<JetpackListItemState>
    ) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            if (oldItem::class != newItem::class) {
                return false
            }
            return oldItem.longId() == newItem.longId()
        }

        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}

package org.sitebay.android.ui.history

import android.view.ViewGroup
import android.widget.TextView
import org.sitebay.android.R

class FooterItemViewHolder(parent: ViewGroup) : HistoryViewHolder(parent, R.layout.history_list_footer) {
    private val footer: TextView = itemView.findViewById(R.id.footer_text)

    fun bind(revision: HistoryListItem.Footer) {
        footer.text = revision.text
    }
}

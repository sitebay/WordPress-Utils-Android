package org.sitebay.android.ui.reader.discover.viewholders

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import org.sitebay.android.ui.reader.discover.ReaderCardUiState

abstract class ReaderViewHolder<T : ViewBinding>(protected val binding: T) : RecyclerView.ViewHolder(binding.root) {
    abstract fun onBind(uiState: ReaderCardUiState)
}

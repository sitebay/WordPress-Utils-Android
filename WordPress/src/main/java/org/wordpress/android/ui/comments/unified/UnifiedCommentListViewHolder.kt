package org.sitebay.android.ui.comments.unified

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class UnifiedCommentListViewHolder<T : ViewBinding>(
    protected val binding: T
) : RecyclerView.ViewHolder(binding.root)

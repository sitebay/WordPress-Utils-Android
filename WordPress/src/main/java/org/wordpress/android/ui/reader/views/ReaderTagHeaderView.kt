package org.sitebay.android.ui.reader.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import org.sitebay.android.WordPress
import org.sitebay.android.databinding.ReaderTagHeaderViewBinding
import org.sitebay.android.ui.reader.views.ReaderTagHeaderViewUiState.ReaderTagHeaderUiState
import org.sitebay.android.ui.utils.UiHelpers
import javax.inject.Inject

/**
 * topmost view in post adapter when showing tag preview - displays tag name and follow button
 */
class ReaderTagHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {
    private val binding = ReaderTagHeaderViewBinding.inflate(LayoutInflater.from(context), this, true)

    @Inject lateinit var uiHelpers: UiHelpers

    private var onFollowBtnClicked: (() -> Unit)? = null

    init {
        (context.applicationContext as WordPress).component().inject(this)
        binding.followButton.setOnClickListener { onFollowBtnClicked?.invoke() }
    }

    fun updateUi(uiState: ReaderTagHeaderUiState) = with(binding) {
        textTag.text = uiState.title
        with(uiState.followButtonUiState) {
            followButton.setIsFollowed(isFollowed)
            followButton.isEnabled = isEnabled
            onFollowBtnClicked = onFollowButtonClicked
        }
    }
}

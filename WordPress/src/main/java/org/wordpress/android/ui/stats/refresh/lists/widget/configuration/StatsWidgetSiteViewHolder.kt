package org.sitebay.android.ui.stats.refresh.lists.widget.configuration

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import org.sitebay.android.R
import org.sitebay.android.databinding.StatsWidgetSiteSelectorItemBinding
import org.sitebay.android.ui.stats.refresh.lists.widget.configuration.StatsSiteSelectionViewModel.SiteUiModel
import org.sitebay.android.util.image.ImageManager
import org.sitebay.android.util.image.ImageType.BLAVATAR
import org.sitebay.android.util.viewBinding

class StatsWidgetSiteViewHolder(
    parent: ViewGroup,
    val imageManager: ImageManager,
    val binding: StatsWidgetSiteSelectorItemBinding = parent.viewBinding(StatsWidgetSiteSelectorItemBinding::inflate)
) : ViewHolder(
        binding.root
) {
    fun bind(site: SiteUiModel) = with(binding) {
        if (site.iconUrl != null) {
            imageManager.load(siteIcon, BLAVATAR, site.iconUrl)
        } else {
            imageManager.load(
                    siteIcon,
                    R.drawable.ic_placeholder_blavatar_grey_lighten_20_40dp
            )
        }
        if (site.title != null) {
            siteTitle.text = site.title
        } else {
            siteTitle.setText(R.string.unknown)
        }
        if (site.url != null) {
            siteUrl.text = site.url
        } else {
            siteUrl.setText(R.string.unknown)
        }
        siteContainer.setOnClickListener {
            site.click()
        }
    }
}

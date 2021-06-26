package org.sitebay.android.ui.stats.refresh.lists.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import org.sitebay.android.WordPress
import org.sitebay.android.analytics.AnalyticsTracker
import org.sitebay.android.databinding.StatsDetailActivityBinding
import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.ui.LocaleAwareActivity
import org.sitebay.android.ui.stats.refresh.lists.StatsListFragment
import org.sitebay.android.ui.stats.refresh.lists.StatsListViewModel.StatsSection
import org.sitebay.android.util.analytics.AnalyticsUtils

const val POST_ID = "POST_ID"
const val POST_TYPE = "POST_TYPE"
const val POST_TITLE = "POST_TITLE"
const val POST_URL = "POST_URL"

class StatsDetailActivity : LocaleAwareActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = StatsDetailActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        fun start(
            context: Context,
            site: SiteModel,
            postId: Long,
            postType: String,
            postTitle: String,
            postUrl: String?
        ) {
            val statsPostViewIntent = Intent(context, StatsDetailActivity::class.java)
            statsPostViewIntent.putExtra(WordPress.LOCAL_SITE_ID, site.id)
            statsPostViewIntent.putExtra(POST_ID, postId)
            statsPostViewIntent.putExtra(POST_TYPE, postType)
            statsPostViewIntent.putExtra(POST_TITLE, postTitle)
            statsPostViewIntent.putExtra(StatsListFragment.LIST_TYPE, StatsSection.DETAIL)
            if (postUrl != null) {
                statsPostViewIntent.putExtra(POST_URL, postUrl)
            }
            AnalyticsUtils.trackWithSiteId(
                    AnalyticsTracker.Stat.STATS_SINGLE_POST_ACCESSED,
                    site.siteId
            )
            context.startActivity(statsPostViewIntent)
        }
    }
}

package org.sitebay.android.ui.stats.refresh

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.ViewModelProvider
import org.sitebay.android.WordPress
import org.sitebay.android.databinding.StatsListActivityBinding
import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.ui.LocaleAwareActivity
import org.sitebay.android.ui.stats.StatsTimeframe
import org.sitebay.android.ui.stats.refresh.utils.StatsSiteProvider
import javax.inject.Inject

class StatsActivity : LocaleAwareActivity() {
    @Inject lateinit var statsSiteProvider: StatsSiteProvider
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: StatsViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as WordPress).component().inject(this)
        setContentView(StatsListActivityBinding.inflate(layoutInflater).root)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNewIntent(intent: Intent?) {
        intent?.let {
            val siteId = intent.getIntExtra(WordPress.LOCAL_SITE_ID, -1)
            if (siteId > -1) {
                viewModel = ViewModelProvider(this, viewModelFactory).get(StatsViewModel::class.java)
                viewModel.start(intent, restart = true)
            }
        }
        super.onNewIntent(intent)
    }

    companion object {
        const val INITIAL_SELECTED_PERIOD_KEY = "INITIAL_SELECTED_PERIOD_KEY"
        const val ARG_LAUNCHED_FROM = "ARG_LAUNCHED_FROM"
        const val ARG_DESIRED_TIMEFRAME = "ARG_DESIRED_TIMEFRAME"
        @JvmStatic
        fun start(context: Context, site: SiteModel) {
            context.startActivity(buildIntent(context, site))
        }

        @JvmStatic
        fun start(context: Context, site: SiteModel, statsTimeframe: StatsTimeframe) {
            context.startActivity(buildIntent(context, site, statsTimeframe))
        }

        fun start(context: Context, localSiteId: Int, statsTimeframe: StatsTimeframe, period: String?) {
            val intent = buildIntent(context, localSiteId, statsTimeframe, period)
            context.startActivity(intent)
        }

        @JvmStatic
        fun buildIntent(context: Context, site: SiteModel): Intent {
            return buildIntent(context, site.id)
        }

        @JvmStatic
        fun buildIntent(context: Context, site: SiteModel, statsTimeframe: StatsTimeframe): Intent {
            return buildIntent(context, site.id, statsTimeframe)
        }

        private fun buildIntent(
            context: Context,
            localSiteId: Int,
            statsTimeframe: StatsTimeframe? = null,
            period: String? = null
        ): Intent {
            val intent = Intent(context, StatsActivity::class.java)
            intent.putExtra(WordPress.LOCAL_SITE_ID, localSiteId)
            statsTimeframe?.let { intent.putExtra(ARG_DESIRED_TIMEFRAME, statsTimeframe) }
            period?.let { intent.putExtra(INITIAL_SELECTED_PERIOD_KEY, period) }
            return intent
        }
    }

    enum class StatsLaunchedFrom {
        STATS_WIDGET,
        NOTIFICATIONS
    }
}

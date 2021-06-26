package org.sitebay.android.ui.history

import android.os.Bundle
import org.sitebay.android.R
import org.sitebay.android.analytics.AnalyticsTracker
import org.sitebay.android.analytics.AnalyticsTracker.Stat
import org.sitebay.android.databinding.HistoryDetailActivityBinding
import org.sitebay.android.ui.LocaleAwareActivity
import org.sitebay.android.ui.history.HistoryListItem.Revision

class HistoryDetailActivity : LocaleAwareActivity() {
    companion object {
        const val KEY_HISTORY_DETAIL_FRAGMENT = "history_detail_fragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(HistoryDetailActivityBinding.inflate(layoutInflater)) {
            setContentView(root)
            setSupportActionBar(toolbarMain)
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val extras = requireNotNull(intent.extras)
        val revision = extras.getParcelable<Revision>(HistoryDetailContainerFragment.EXTRA_REVISION)
        val revisions = extras.getParcelableArrayList<Revision>(HistoryDetailContainerFragment.EXTRA_REVISIONS)

        var historyDetailContainerFragment = supportFragmentManager.findFragmentByTag(KEY_HISTORY_DETAIL_FRAGMENT)

        if (historyDetailContainerFragment == null) {
            historyDetailContainerFragment = HistoryDetailContainerFragment.newInstance(
                    revision,
                    revisions as ArrayList<Revision>
            )
            supportFragmentManager
                    .beginTransaction()
                    .add(R.id.fragment_container, historyDetailContainerFragment, KEY_HISTORY_DETAIL_FRAGMENT)
                    .commit()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        AnalyticsTracker.track(Stat.REVISIONS_DETAIL_CANCELLED)
        finish()
        return true
    }

    override fun onBackPressed() {
        AnalyticsTracker.track(Stat.REVISIONS_DETAIL_CANCELLED)
        super.onBackPressed()
    }
}

package org.sitebay.android.ui.stats.refresh.lists.sections.insights.management

import android.os.Bundle
import android.view.MenuItem
import org.sitebay.android.R
import org.sitebay.android.databinding.InsightsManagementActivityBinding
import org.sitebay.android.ui.LocaleAwareActivity

class InsightsManagementActivity : LocaleAwareActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(InsightsManagementActivityBinding.inflate(layoutInflater)) {
            setContentView(root)

            setSupportActionBar(toolbarMain)
        }

        supportActionBar?.let {
            it.setHomeButtonEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}

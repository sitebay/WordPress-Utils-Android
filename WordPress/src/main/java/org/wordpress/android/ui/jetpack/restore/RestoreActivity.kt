package org.sitebay.android.ui.jetpack.restore

import android.os.Bundle
import android.view.MenuItem
import org.sitebay.android.databinding.RestoreActivityBinding
import org.sitebay.android.ui.LocaleAwareActivity

class RestoreActivity : LocaleAwareActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(RestoreActivityBinding.inflate(layoutInflater)) {
            setContentView(root)

            setSupportActionBar(toolbarMain)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return false
    }
}

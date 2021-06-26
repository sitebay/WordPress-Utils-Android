package org.sitebay.android.ui.jetpack.backup.download

import android.R.id
import android.os.Bundle
import android.view.MenuItem
import org.sitebay.android.databinding.BackupDownloadActivityBinding
import org.sitebay.android.ui.LocaleAwareActivity

class BackupDownloadActivity : LocaleAwareActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(BackupDownloadActivityBinding.inflate(layoutInflater)) {
            setContentView(root)

            setSupportActionBar(toolbarMain)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}

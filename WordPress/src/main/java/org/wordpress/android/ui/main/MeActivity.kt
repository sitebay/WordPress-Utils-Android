package org.sitebay.android.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import org.sitebay.android.R
import org.sitebay.android.ui.LocaleAwareActivity
import org.sitebay.android.ui.RequestCodes
import org.sitebay.android.ui.prefs.AppSettingsFragment.LANGUAGE_CHANGED

class MeActivity : LocaleAwareActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.me_activity)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RequestCodes.APP_SETTINGS -> {
                if (resultCode == LANGUAGE_CHANGED) {
                    // Refresh the app
                    val refresh = Intent(this, this.javaClass)
                    startActivity(refresh)
                    setResult(LANGUAGE_CHANGED)
                    finish()
                }
            }
        }
    }
}

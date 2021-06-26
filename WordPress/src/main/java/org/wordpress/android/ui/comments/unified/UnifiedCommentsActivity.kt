package org.sitebay.android.ui.comments.unified

import android.os.Bundle
import android.view.MenuItem
import org.sitebay.android.databinding.UnifiedCommentActivityBinding
import org.sitebay.android.ui.LocaleAwareActivity

class UnifiedCommentsActivity : LocaleAwareActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        with(UnifiedCommentActivityBinding.inflate(layoutInflater)) {
            setContentView(root)
            setSupportActionBar(toolbarMain)
        }

        supportActionBar?.let {
            it.setHomeButtonEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
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

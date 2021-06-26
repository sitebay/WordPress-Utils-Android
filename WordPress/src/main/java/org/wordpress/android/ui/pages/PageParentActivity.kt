package org.sitebay.android.ui.pages

import android.os.Bundle
import org.sitebay.android.databinding.PagesParentActivityBinding
import org.sitebay.android.ui.LocaleAwareActivity

class PageParentActivity : LocaleAwareActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = PagesParentActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar.toolbarMain)
        supportActionBar?.let {
            it.setHomeButtonEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
        }
    }
}

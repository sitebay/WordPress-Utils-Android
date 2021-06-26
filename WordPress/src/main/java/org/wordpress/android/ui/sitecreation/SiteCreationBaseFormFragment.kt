package org.sitebay.android.ui.sitecreation

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.sitebay.android.R
import org.sitebay.android.databinding.SiteCreationFormScreenBinding

abstract class SiteCreationBaseFormFragment : Fragment(R.layout.site_creation_form_screen) {
    @LayoutRes protected abstract fun getContentLayout(): Int
    protected abstract val screenTitle: String
    protected abstract fun setupContent()
    protected abstract fun onHelp()
    protected abstract fun setBindingViewStubListener(parentBinding: SiteCreationFormScreenBinding)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(SiteCreationFormScreenBinding.bind(view)) {
            siteCreationFormContentStub.layoutResource = getContentLayout()
            setBindingViewStubListener(this)
            siteCreationFormContentStub.inflate()

            setupContent()

            (requireActivity() as AppCompatActivity).setSupportActionBar(toolbarMain)
            val actionBar = (activity as AppCompatActivity?)!!.supportActionBar
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true)
                actionBar.title = screenTitle
                // important for accessibility
                requireActivity().title = screenTitle
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_site_creation, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.help) {
            onHelp()
            return true
        }
        return false
    }

    companion object {
        const val EXTRA_SCREEN_TITLE = "extra_screen_title"
    }
}

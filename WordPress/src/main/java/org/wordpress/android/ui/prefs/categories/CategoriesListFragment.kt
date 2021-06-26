package org.sitebay.android.ui.prefs.categories

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import org.sitebay.android.R
import org.sitebay.android.WordPress
import org.sitebay.android.databinding.SiteSettingsCategoriesListFragmentBinding
import javax.inject.Inject

class CategoriesListFragment : Fragment(R.layout.site_settings_categories_list_fragment) {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: CategoriesListViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(SiteSettingsCategoriesListFragmentBinding.bind(view)) {
            initDagger()
            initViewModel()
        }
    }

    private fun initDagger() {
        (requireActivity().application as WordPress).component()?.inject(this)
    }

    private fun SiteSettingsCategoriesListFragmentBinding.initViewModel() {
        viewModel = ViewModelProvider(this@CategoriesListFragment, viewModelFactory)
                .get(CategoriesListViewModel::class.java)
        viewModel.start()
    }
}

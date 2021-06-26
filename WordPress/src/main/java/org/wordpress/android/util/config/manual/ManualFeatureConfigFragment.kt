package org.sitebay.android.util.config.manual

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.android.support.DaggerFragment
import org.sitebay.android.R
import org.sitebay.android.databinding.ManualFeatureConfigFragmentBinding
import org.sitebay.android.util.DisplayUtils
import org.sitebay.android.viewmodel.observeEvent
import org.sitebay.android.widgets.RecyclerItemDecoration
import javax.inject.Inject
import kotlin.system.exitProcess

class ManualFeatureConfigFragment : DaggerFragment(R.layout.manual_feature_config_fragment) {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: ManualFeatureConfigViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(ManualFeatureConfigFragmentBinding.bind(view)) {
            with(requireActivity() as AppCompatActivity) {
                setSupportActionBar(toolbar)
                supportActionBar?.let {
                    it.setHomeButtonEnabled(true)
                    it.setDisplayHomeAsUpEnabled(true)
                }
            }
            recyclerView.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
            recyclerView.addItemDecoration(RecyclerItemDecoration(0, DisplayUtils.dpToPx(activity, 1)))

            viewModel = ViewModelProvider(this@ManualFeatureConfigFragment, viewModelFactory)
                    .get(ManualFeatureConfigViewModel::class.java)
            viewModel.uiState.observe(viewLifecycleOwner, {
                it?.let { uiState ->
                    val adapter: FeatureAdapter
                    if (recyclerView.adapter == null) {
                        adapter = FeatureAdapter()
                        recyclerView.adapter = adapter
                    } else {
                        adapter = recyclerView.adapter as FeatureAdapter
                    }

                    val layoutManager = recyclerView.layoutManager
                    val recyclerViewState = layoutManager?.onSaveInstanceState()
                    adapter.update(uiState.uiItems)
                    layoutManager?.onRestoreInstanceState(recyclerViewState)
                }
            })
            viewModel.restartAction.observeEvent(viewLifecycleOwner, {
                exitProcess(0)
            })
            viewModel.start()
        }
    }
}

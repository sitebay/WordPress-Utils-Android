package org.sitebay.android.ui.stats.refresh.lists.widget.today

import android.content.Context
import android.content.Intent
import org.sitebay.android.WordPress
import org.sitebay.android.ui.stats.refresh.lists.widget.WidgetBlockListProvider
import javax.inject.Inject

class TodayWidgetBlockListProviderFactory(val context: Context, val intent: Intent) {
    @Inject lateinit var viewModel: TodayWidgetBlockListViewModel

    init {
        (context.applicationContext as WordPress).component().inject(this)
    }

    fun build(): WidgetBlockListProvider {
        return WidgetBlockListProvider(context, viewModel, intent)
    }
}

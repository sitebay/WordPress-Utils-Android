package org.sitebay.android.ui.stats.refresh.lists.widget.views

import org.sitebay.android.modules.AppComponent
import org.sitebay.android.ui.stats.refresh.lists.widget.StatsWidget
import org.sitebay.android.ui.stats.refresh.lists.widget.WidgetUpdater
import javax.inject.Inject

class StatsViewsWidget : StatsWidget() {
    @Inject lateinit var viewsWidgetUpdater: ViewsWidgetUpdater
    override val widgetUpdater: WidgetUpdater
        get() = viewsWidgetUpdater

    override fun inject(appComponent: AppComponent) {
        appComponent.inject(this)
    }
}

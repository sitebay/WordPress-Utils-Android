package org.sitebay.android.ui.stats.refresh.lists.widget.alltime

import org.sitebay.android.modules.AppComponent
import org.sitebay.android.ui.stats.refresh.lists.widget.StatsWidget
import org.sitebay.android.ui.stats.refresh.lists.widget.WidgetUpdater
import javax.inject.Inject

class StatsAllTimeWidget : StatsWidget() {
    @Inject lateinit var allTimeWidgetUpdater: AllTimeWidgetUpdater
    override val widgetUpdater: WidgetUpdater
        get() = allTimeWidgetUpdater

    override fun inject(appComponent: AppComponent) {
        appComponent.inject(this)
    }
}

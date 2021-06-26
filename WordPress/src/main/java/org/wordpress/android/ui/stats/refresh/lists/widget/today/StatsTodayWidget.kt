package org.sitebay.android.ui.stats.refresh.lists.widget.today

import org.sitebay.android.modules.AppComponent
import org.sitebay.android.ui.stats.refresh.lists.widget.StatsWidget
import org.sitebay.android.ui.stats.refresh.lists.widget.WidgetUpdater
import javax.inject.Inject

class StatsTodayWidget : StatsWidget() {
    @Inject lateinit var todayWidgetUpdater: TodayWidgetUpdater
    override val widgetUpdater: WidgetUpdater
        get() = todayWidgetUpdater

    override fun inject(appComponent: AppComponent) {
        appComponent.inject(this)
    }
}

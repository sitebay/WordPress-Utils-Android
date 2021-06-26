package org.sitebay.android.ui.stats.refresh.lists.widget.alltime

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import org.sitebay.android.R
import org.sitebay.android.analytics.AnalyticsTracker
import org.sitebay.android.fluxc.store.AccountStore
import org.sitebay.android.fluxc.store.SiteStore
import org.sitebay.android.ui.prefs.AppPrefsWrapper
import org.sitebay.android.ui.stats.StatsTimeframe.INSIGHTS
import org.sitebay.android.ui.stats.refresh.lists.widget.WidgetUpdater
import org.sitebay.android.ui.stats.refresh.lists.widget.configuration.StatsColorSelectionViewModel.Color.LIGHT
import org.sitebay.android.ui.stats.refresh.lists.widget.configuration.StatsWidgetConfigureFragment.WidgetType.ALL_TIME_VIEWS
import org.sitebay.android.ui.stats.refresh.lists.widget.utils.WidgetUtils
import org.sitebay.android.ui.stats.refresh.utils.trackWithWidgetType
import org.sitebay.android.util.NetworkUtilsWrapper
import org.sitebay.android.util.analytics.AnalyticsTrackerWrapper
import org.sitebay.android.viewmodel.ResourceProvider
import javax.inject.Inject

class AllTimeWidgetUpdater
@Inject constructor(
    private val appPrefsWrapper: AppPrefsWrapper,
    private val siteStore: SiteStore,
    private val accountStore: AccountStore,
    private val networkUtilsWrapper: NetworkUtilsWrapper,
    private val resourceProvider: ResourceProvider,
    private val widgetUtils: WidgetUtils,
    private val analyticsTrackerWrapper: AnalyticsTrackerWrapper
) : WidgetUpdater {
    override fun updateAppWidget(
        context: Context,
        appWidgetId: Int,
        appWidgetManager: AppWidgetManager?
    ) {
        val widgetManager = appWidgetManager ?: AppWidgetManager.getInstance(context)
        val isWideView = widgetUtils.isWidgetWiderThanLimit(
                widgetManager,
                appWidgetId
        )
        val colorMode = appPrefsWrapper.getAppWidgetColor(appWidgetId) ?: LIGHT
        val siteId = appPrefsWrapper.getAppWidgetSiteId(appWidgetId)
        val siteModel = siteStore.getSiteBySiteId(siteId)
        val networkAvailable = networkUtilsWrapper.isNetworkAvailable()
        val hasToken = accountStore.hasAccessToken()
        val views = RemoteViews(context.packageName, widgetUtils.getLayout(colorMode))
        views.setTextViewText(R.id.widget_title, resourceProvider.getString(R.string.stats_insights_all_time_stats))
        val widgetHasData = appPrefsWrapper.hasAppWidgetData(appWidgetId)
        if (networkAvailable && siteModel != null && hasToken) {
            widgetUtils.setSiteIcon(siteModel, context, views, appWidgetId)
            siteModel.let {
                views.setOnClickPendingIntent(
                        R.id.widget_title_container,
                        widgetUtils.getPendingSelfIntent(context, siteModel.id, INSIGHTS)
                )
            }
            widgetUtils.showList(
                    widgetManager,
                    views,
                    context,
                    appWidgetId,
                    colorMode,
                    siteModel.id,
                    ALL_TIME_VIEWS,
                    isWideView
            )
        } else if (!widgetHasData || !hasToken || siteModel == null) {
            widgetUtils.showError(
                    widgetManager,
                    views,
                    appWidgetId,
                    networkAvailable,
                    hasToken,
                    resourceProvider,
                    context,
                    StatsAllTimeWidget::class.java
            )
        }
    }

    override fun componentName(context: Context) = ComponentName(context, StatsAllTimeWidget::class.java)

    override fun delete(appWidgetId: Int) {
        analyticsTrackerWrapper.trackWithWidgetType(AnalyticsTracker.Stat.STATS_WIDGET_REMOVED, ALL_TIME_VIEWS)
        appPrefsWrapper.removeAppWidgetColorModeId(appWidgetId)
        appPrefsWrapper.removeAppWidgetSiteId(appWidgetId)
    }
}

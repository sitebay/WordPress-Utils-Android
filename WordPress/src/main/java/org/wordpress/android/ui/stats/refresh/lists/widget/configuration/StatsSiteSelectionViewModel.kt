package org.sitebay.android.ui.stats.refresh.lists.widget.configuration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineDispatcher
import org.sitebay.android.R
import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.fluxc.store.AccountStore
import org.sitebay.android.fluxc.store.SiteStore
import org.sitebay.android.modules.UI_THREAD
import org.sitebay.android.ui.prefs.AppPrefsWrapper
import org.sitebay.android.util.SiteUtils
import org.sitebay.android.viewmodel.Event
import org.sitebay.android.viewmodel.ScopedViewModel
import javax.inject.Inject
import javax.inject.Named

class StatsSiteSelectionViewModel
@Inject constructor(
    @Named(UI_THREAD) private val mainDispatcher: CoroutineDispatcher,
    private val siteStore: SiteStore,
    private val accountStore: AccountStore,
    private val appPrefsWrapper: AppPrefsWrapper
) : ScopedViewModel(mainDispatcher) {
    private val mutableSelectedSite = MutableLiveData<SiteUiModel>()
    val selectedSite: LiveData<SiteUiModel> = mutableSelectedSite

    private val mutableSites = MutableLiveData<List<SiteUiModel>>()
    val sites: LiveData<List<SiteUiModel>> = mutableSites
    private val mutableHideSiteDialog = MutableLiveData<Event<Unit>>()
    val hideSiteDialog: LiveData<Event<Unit>> = mutableHideSiteDialog

    private val mutableNotification = MutableLiveData<Event<Int>>()
    val notification: LiveData<Event<Int>> = mutableNotification

    private val mutableDialogOpened = MutableLiveData<Event<Unit>>()
    val dialogOpened: LiveData<Event<Unit>> = mutableDialogOpened

    fun start(appWidgetId: Int) {
        val siteId = appPrefsWrapper.getAppWidgetSiteId(appWidgetId)
        if (siteId > -1) {
            mutableSelectedSite.postValue(siteStore.getSiteBySiteId(siteId)?.let { toUiModel(it) })
        }
    }

    fun loadSites() {
        val sites = siteStore.sites.filter { it.isWPCom || it.isJetpackConnected }.map { toUiModel(it) }
        mutableSites.postValue(sites)
    }

    private fun toUiModel(site: SiteModel): SiteUiModel {
        val blogName = SiteUtils.getSiteNameOrHomeURL(site)
        val homeUrl = SiteUtils.getHomeURLOrHostName(site)
        val title = when {
            !blogName.isNullOrEmpty() -> blogName
            !homeUrl.isNullOrEmpty() -> homeUrl
            else -> null
        }
        val description = when {
            !homeUrl.isNullOrEmpty() -> homeUrl
            else -> null
        }
        return SiteUiModel(site.siteId, site.iconUrl, title, description, this::selectSite)
    }

    private fun selectSite(site: SiteUiModel) {
        mutableHideSiteDialog.postValue(Event(Unit))
        mutableSelectedSite.postValue(site)
    }

    fun openSiteDialog() {
        if (accountStore.hasAccessToken()) {
            mutableDialogOpened.postValue(Event(Unit))
        } else {
            mutableNotification.postValue(Event(R.string.stats_widget_log_in_message))
        }
    }

    data class SiteUiModel(
        val siteId: Long,
        val iconUrl: String?,
        val title: String?,
        val url: String?,
        private val onClick: (site: SiteUiModel) -> Unit
    ) {
        fun click() {
            onClick(this)
        }
    }
}

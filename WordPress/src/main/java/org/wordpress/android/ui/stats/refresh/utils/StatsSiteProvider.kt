package org.sitebay.android.ui.stats.refresh.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.sitebay.android.fluxc.Dispatcher
import org.sitebay.android.fluxc.generated.SiteActionBuilder
import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.fluxc.store.SiteStore
import org.sitebay.android.fluxc.store.SiteStore.OnSiteChanged
import org.sitebay.android.ui.prefs.AppPrefs
import org.sitebay.android.ui.stats.refresh.utils.StatsSiteProvider.SiteUpdateResult.NotConnectedJetpackSite
import org.sitebay.android.ui.stats.refresh.utils.StatsSiteProvider.SiteUpdateResult.SiteConnected
import org.sitebay.android.viewmodel.Event
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatsSiteProvider
@Inject constructor(
    private val siteStore: SiteStore,
    private val selectedSite: SelectedSiteStorage,
    private val dispatcher: Dispatcher
) {
    var siteModel = SiteModel()
        private set

    private val mutableSiteChanged = MutableLiveData<Event<SiteUpdateResult>>()
    val siteChanged: LiveData<Event<SiteUpdateResult>> = mutableSiteChanged
    private val maxAttempts = 3
    private var counter = 0

    init {
        reset()
        dispatcher.register(this)
    }

    fun start(localSiteId: Int): Boolean {
        if (localSiteId != 0) {
            val siteChanged = localSiteId != siteModel.id
            siteStore.getSiteByLocalId(localSiteId)?.let { site ->
                siteModel = site
            }
            return siteChanged
        }
        return false
    }

    fun reset() {
        start(selectedSite.currentLocalSiteId)
    }

    fun clear() {
        if (mutableSiteChanged.value != null) {
            mutableSiteChanged.value = null
        }
    }

    fun hasLoadedSite(): Boolean = siteModel.siteId != 0L

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSiteChanged(event: OnSiteChanged) {
        if (event.isError) {
            return
        }
        siteStore.getSiteByLocalId(siteModel.id)?.let { site ->
            if (site.siteId != 0L) {
                counter = 0
                siteModel = site
                mutableSiteChanged.value = Event(SiteConnected(site.siteId))
            } else {
                if (counter < maxAttempts) {
                    counter++
                    dispatcher.dispatch(SiteActionBuilder.newFetchSiteAction(site))
                } else {
                    counter = 0
                    mutableSiteChanged.value = Event(NotConnectedJetpackSite)
                }
            }
        }
    }

    class SelectedSiteStorage {
        val currentLocalSiteId
            get() = AppPrefs.getSelectedSite()
    }

    sealed class SiteUpdateResult {
        object NotConnectedJetpackSite : SiteUpdateResult()
        data class SiteConnected(val siteId: Long) : SiteUpdateResult()
    }
}

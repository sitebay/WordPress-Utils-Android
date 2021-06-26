package org.sitebay.android.ui.reader.reblog

import dagger.Reusable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.fluxc.store.SiteStore
import org.sitebay.android.models.ReaderPost
import org.sitebay.android.modules.BG_THREAD
import org.sitebay.android.ui.PagePostCreationSourcesDetail.POST_FROM_REBLOG
import org.sitebay.android.ui.main.SitePickerAdapter.SitePickerMode.REBLOG_SELECT_MODE
import org.sitebay.android.ui.reader.discover.ReaderNavigationEvents
import org.sitebay.android.ui.reader.discover.ReaderNavigationEvents.OpenEditorForReblog
import org.sitebay.android.ui.reader.discover.ReaderNavigationEvents.ShowNoSitesToReblog
import org.sitebay.android.ui.reader.discover.ReaderNavigationEvents.ShowSitePickerForResult
import org.sitebay.android.ui.reader.reblog.ReblogState.MultipleSites
import org.sitebay.android.ui.reader.reblog.ReblogState.NoSite
import org.sitebay.android.ui.reader.reblog.ReblogState.SingleSite
import org.sitebay.android.ui.reader.reblog.ReblogState.Unknown
import org.sitebay.android.util.AppLog
import org.sitebay.android.util.AppLog.T
import org.sitebay.android.util.BuildConfig
import javax.inject.Inject
import javax.inject.Named

@Reusable
class ReblogUseCase @Inject constructor(
    private val siteStore: SiteStore,
    @Named(BG_THREAD) private val bgDispatcher: CoroutineDispatcher
) {
    suspend fun onReblogButtonClicked(post: ReaderPost): ReblogState {
        return withContext(bgDispatcher) {
            val sites = siteStore.visibleSitesAccessedViaWPCom

            when (sites.count()) {
                0 -> NoSite
                1 -> {
                    sites.firstOrNull()?.let {
                        SingleSite(it, post)
                    } ?: Unknown
                }
                else -> {
                    sites.firstOrNull()?.let {
                        MultipleSites(it, post)
                    } ?: Unknown
                }
            }
        }
    }

    suspend fun onReblogSiteSelected(siteLocalId: Int, post: ReaderPost?): ReblogState {
        return withContext(bgDispatcher) {
            when {
                post != null -> {
                    val site: SiteModel? = siteStore.getSiteByLocalId(siteLocalId)
                    if (site != null) SingleSite(site, post) else Unknown
                }
                BuildConfig.DEBUG -> {
                    throw IllegalStateException("Site Selected without passing the SitePicker state")
                }
                else -> {
                    AppLog.e(T.READER, "Site Selected without passing the SitePicker state")
                    Unknown
                }
            }
        }
    }

    fun convertReblogStateToNavigationEvent(state: ReblogState): ReaderNavigationEvents? {
        return when (state) {
            is NoSite -> ShowNoSitesToReblog
            is MultipleSites -> ShowSitePickerForResult(state.defaultSite, state.post, REBLOG_SELECT_MODE)
            is SingleSite -> OpenEditorForReblog(state.site, state.post, POST_FROM_REBLOG)
            Unknown -> null
        }
    }
}

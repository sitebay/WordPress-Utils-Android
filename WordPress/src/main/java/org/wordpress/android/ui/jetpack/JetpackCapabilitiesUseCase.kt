package org.sitebay.android.ui.jetpack

import kotlinx.coroutines.flow.flow
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.sitebay.android.fluxc.Dispatcher
import org.sitebay.android.fluxc.generated.SiteActionBuilder
import org.sitebay.android.fluxc.model.JetpackCapability
import org.sitebay.android.fluxc.model.JetpackCapability.BACKUP
import org.sitebay.android.fluxc.model.JetpackCapability.BACKUP_DAILY
import org.sitebay.android.fluxc.model.JetpackCapability.BACKUP_REALTIME
import org.sitebay.android.fluxc.model.JetpackCapability.SCAN
import org.sitebay.android.fluxc.store.SiteStore
import org.sitebay.android.fluxc.store.SiteStore.FetchJetpackCapabilitiesPayload
import org.sitebay.android.fluxc.store.SiteStore.JetpackCapabilitiesError
import org.sitebay.android.fluxc.store.SiteStore.JetpackCapabilitiesErrorType.GENERIC_ERROR
import org.sitebay.android.fluxc.store.SiteStore.OnJetpackCapabilitiesFetched
import org.sitebay.android.ui.prefs.AppPrefsWrapper
import javax.inject.Inject
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private val SCAN_CAPABILITIES = listOf(SCAN)
val BACKUP_CAPABILITIES = listOf(BACKUP, BACKUP_DAILY, BACKUP_REALTIME)

class JetpackCapabilitiesUseCase @Inject constructor(
    @Suppress("unused") private val siteStore: SiteStore,
    private val dispatcher: Dispatcher,
    private val appPrefsWrapper: AppPrefsWrapper
) {
    private var continuation: HashMap<Long, Continuation<OnJetpackCapabilitiesFetched>?> = hashMapOf()

    init {
        dispatcher.register(this@JetpackCapabilitiesUseCase)
    }

    fun clear() {
        dispatcher.unregister(this)
    }

    suspend fun getJetpackPurchasedProducts(remoteSiteId: Long) = flow {
        emit(getCachedJetpackPurchasedProducts(remoteSiteId))
        emit(fetchJetpackPurchasedProducts(remoteSiteId))
    }

    fun getCachedJetpackPurchasedProducts(remoteSiteId: Long): JetpackPurchasedProducts =
            mapToJetpackPurchasedProducts(getCachedJetpackCapabilities(remoteSiteId))

    suspend fun fetchJetpackPurchasedProducts(remoteSiteId: Long): JetpackPurchasedProducts =
            mapToJetpackPurchasedProducts(fetchJetpackCapabilities(remoteSiteId))

    private fun mapToJetpackPurchasedProducts(capabilities: List<JetpackCapability>) =
            JetpackPurchasedProducts(
                    scan = capabilities.find { SCAN_CAPABILITIES.contains(it) } != null,
                    backup = capabilities.find { BACKUP_CAPABILITIES.contains(it) } != null
            )

    private fun getCachedJetpackCapabilities(remoteSiteId: Long): List<JetpackCapability> {
        return appPrefsWrapper.getSiteJetpackCapabilities(remoteSiteId)
    }

    private suspend fun fetchJetpackCapabilities(remoteSiteId: Long): List<JetpackCapability> {
        forceResumeDuplicateRequests(remoteSiteId)

        val response = suspendCoroutine<OnJetpackCapabilitiesFetched> { cont ->
            val payload = FetchJetpackCapabilitiesPayload(remoteSiteId)
            continuation[remoteSiteId] = cont
            dispatcher.dispatch(SiteActionBuilder.newFetchJetpackCapabilitiesAction(payload))
        }

        val capabilities: List<JetpackCapability> = response.capabilities ?: listOf()
        if (!response.isError) {
            updateCache(remoteSiteId, capabilities)
        }
        return capabilities
    }

    private fun forceResumeDuplicateRequests(remoteSiteId: Long) {
        continuation[remoteSiteId]?.let {
            continuation.remove(remoteSiteId)
            val event = OnJetpackCapabilitiesFetched(
                    remoteSiteId, listOf(), JetpackCapabilitiesError(GENERIC_ERROR, "Already running")
            )
            it.resume(event)
        }
    }

    private fun updateCache(
        remoteSiteId: Long,
        capabilities: List<JetpackCapability>
    ) {
        appPrefsWrapper.setSiteJetpackCapabilities(
                remoteSiteId,
                capabilities
        )
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    fun onJetpackCapabilitiesFetched(event: OnJetpackCapabilitiesFetched) {
        continuation[event.remoteSiteId]?.let {
            continuation.remove(event.remoteSiteId)
            it.resume(event)
        }
    }

    data class JetpackPurchasedProducts(val scan: Boolean, val backup: Boolean)
}

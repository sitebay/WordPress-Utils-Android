package org.sitebay.android.viewmodel.accounts

import androidx.lifecycle.ViewModel
import org.sitebay.android.analytics.AnalyticsTracker.Stat.WELCOME_NO_SITES_INTERSTITIAL_ADD_SELF_HOSTED_SITE_TAPPED
import org.sitebay.android.analytics.AnalyticsTracker.Stat.WELCOME_NO_SITES_INTERSTITIAL_CREATE_NEW_SITE_TAPPED
import org.sitebay.android.analytics.AnalyticsTracker.Stat.WELCOME_NO_SITES_INTERSTITIAL_DISMISSED
import org.sitebay.android.analytics.AnalyticsTracker.Stat.WELCOME_NO_SITES_INTERSTITIAL_SHOWN
import org.sitebay.android.ui.accounts.UnifiedLoginTracker
import org.sitebay.android.ui.accounts.UnifiedLoginTracker.Click
import org.sitebay.android.ui.accounts.UnifiedLoginTracker.Step.SUCCESS
import org.sitebay.android.ui.prefs.AppPrefsWrapper
import org.sitebay.android.util.analytics.AnalyticsTrackerWrapper
import org.sitebay.android.viewmodel.SingleLiveEvent
import org.sitebay.android.viewmodel.accounts.PostSignupInterstitialViewModel.NavigationAction.DISMISS
import org.sitebay.android.viewmodel.accounts.PostSignupInterstitialViewModel.NavigationAction.START_SITE_CONNECTION_FLOW
import org.sitebay.android.viewmodel.accounts.PostSignupInterstitialViewModel.NavigationAction.START_SITE_CREATION_FLOW
import javax.inject.Inject

class PostSignupInterstitialViewModel
@Inject constructor(
    private val appPrefs: AppPrefsWrapper,
    private val unifiedLoginTracker: UnifiedLoginTracker,
    private val analyticsTracker: AnalyticsTrackerWrapper
) : ViewModel() {
    val navigationAction: SingleLiveEvent<NavigationAction> = SingleLiveEvent()

    fun onInterstitialShown() {
        analyticsTracker.track(WELCOME_NO_SITES_INTERSTITIAL_SHOWN)
        unifiedLoginTracker.track(step = SUCCESS)
        appPrefs.shouldShowPostSignupInterstitial = false
    }

    fun onCreateNewSiteButtonPressed() {
        analyticsTracker.track(WELCOME_NO_SITES_INTERSTITIAL_CREATE_NEW_SITE_TAPPED)
        unifiedLoginTracker.trackClick(Click.CREATE_NEW_SITE)
        navigationAction.value = START_SITE_CREATION_FLOW
    }

    fun onAddSelfHostedSiteButtonPressed() {
        analyticsTracker.track(WELCOME_NO_SITES_INTERSTITIAL_ADD_SELF_HOSTED_SITE_TAPPED)
        unifiedLoginTracker.trackClick(Click.ADD_SELF_HOSTED_SITE)
        navigationAction.value = START_SITE_CONNECTION_FLOW
    }

    fun onDismissButtonPressed() = onDismiss()

    fun onBackButtonPressed() = onDismiss()

    private fun onDismiss() {
        unifiedLoginTracker.trackClick(Click.DISMISS)
        analyticsTracker.track(WELCOME_NO_SITES_INTERSTITIAL_DISMISSED)
        navigationAction.value = DISMISS
    }

    enum class NavigationAction { START_SITE_CREATION_FLOW, START_SITE_CONNECTION_FLOW, DISMISS }
}

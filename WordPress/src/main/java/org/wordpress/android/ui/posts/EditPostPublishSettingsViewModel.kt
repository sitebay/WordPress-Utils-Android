package org.sitebay.android.ui.posts

import org.sitebay.android.fluxc.store.PostSchedulingNotificationStore
import org.sitebay.android.fluxc.store.SiteStore
import org.sitebay.android.util.LocaleManagerWrapper
import org.sitebay.android.viewmodel.ResourceProvider
import javax.inject.Inject

class EditPostPublishSettingsViewModel @Inject constructor(
    resourceProvider: ResourceProvider,
    postSettingsUtils: PostSettingsUtils,
    localeManagerWrapper: LocaleManagerWrapper,
    postSchedulingNotificationStore: PostSchedulingNotificationStore,
    siteStore: SiteStore
) : PublishSettingsViewModel(
        resourceProvider,
        postSettingsUtils,
        localeManagerWrapper,
        postSchedulingNotificationStore,
        siteStore
)

package org.sitebay.android.workers

import androidx.work.DelegatingWorkerFactory
import org.sitebay.android.fluxc.store.SiteStore
import org.sitebay.android.ui.uploads.UploadStarter
import org.sitebay.android.util.UploadWorker
import javax.inject.Inject

class WordPressWorkersFactory @Inject constructor(
    uploadStarter: UploadStarter,
    siteStore: SiteStore,
    localNotificationHandlerFactory: LocalNotificationHandlerFactory
) : DelegatingWorkerFactory() {
    init {
        addFactory(UploadWorker.Factory(uploadStarter, siteStore))
        addFactory(LocalNotificationWorker.Factory(localNotificationHandlerFactory))
    }
}

package org.sitebay.android.ui.reader.repository.usecases

import org.sitebay.android.ui.reader.repository.ReaderDiscoverCommunication
import org.sitebay.android.ui.reader.repository.ReaderDiscoverCommunication.Error.NetworkUnavailable
import org.sitebay.android.ui.reader.repository.ReaderDiscoverCommunication.Error.ServiceNotStarted
import org.sitebay.android.ui.reader.repository.ReaderDiscoverCommunication.Started
import org.sitebay.android.ui.reader.services.discover.ReaderDiscoverLogic.DiscoverTasks
import org.sitebay.android.ui.reader.services.discover.ReaderDiscoverServiceStarter
import org.sitebay.android.util.NetworkUtilsWrapper
import org.sitebay.android.viewmodel.ContextProvider
import javax.inject.Inject

class FetchDiscoverCardsUseCase @Inject constructor(
    private val networkUtilsWrapper: NetworkUtilsWrapper,
    private val contextProvider: ContextProvider
) {
    fun fetch(discoverTask: DiscoverTasks): ReaderDiscoverCommunication {
        if (!networkUtilsWrapper.isNetworkAvailable()) {
            return NetworkUnavailable(discoverTask)
        }

        val isStarted =
                ReaderDiscoverServiceStarter.startService(contextProvider.getContext(), discoverTask)

        return if (isStarted)
            Started(discoverTask)
        else
            ServiceNotStarted(discoverTask)
    }
}

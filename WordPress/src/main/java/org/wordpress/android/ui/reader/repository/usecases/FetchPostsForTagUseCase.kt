package org.sitebay.android.ui.reader.repository.usecases

import org.sitebay.android.models.ReaderTag
import org.sitebay.android.ui.reader.repository.ReaderRepositoryCommunication
import org.sitebay.android.ui.reader.repository.ReaderRepositoryCommunication.Error.NetworkUnavailable
import org.sitebay.android.ui.reader.repository.ReaderRepositoryCommunication.Started
import org.sitebay.android.ui.reader.services.post.ReaderPostServiceStarter.UpdateAction
import org.sitebay.android.ui.reader.services.post.ReaderPostServiceStarter.UpdateAction.REQUEST_NEWER
import org.sitebay.android.ui.reader.services.post.wrapper.ReaderPostServiceStarterWrapper
import org.sitebay.android.util.NetworkUtilsWrapper
import org.sitebay.android.viewmodel.ContextProvider
import javax.inject.Inject

class FetchPostsForTagUseCase @Inject constructor(
    private val networkUtilsWrapper: NetworkUtilsWrapper,
    private val contextProvider: ContextProvider,
    private val readerPostServiceStarterWrapper: ReaderPostServiceStarterWrapper
) {
    fun fetch(readerTag: ReaderTag, updateAction: UpdateAction = REQUEST_NEWER): ReaderRepositoryCommunication {
        if (!networkUtilsWrapper.isNetworkAvailable()) {
            return NetworkUnavailable
        }

        readerPostServiceStarterWrapper.startServiceForTag(
            contextProvider.getContext(),
            readerTag,
            updateAction
        )

        return Started
    }
}

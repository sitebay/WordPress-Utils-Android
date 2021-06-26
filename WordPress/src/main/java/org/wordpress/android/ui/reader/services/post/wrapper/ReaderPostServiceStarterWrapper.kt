package org.sitebay.android.ui.reader.services.post.wrapper

import android.content.Context
import dagger.Reusable
import org.sitebay.android.models.ReaderTag
import org.sitebay.android.ui.reader.services.post.ReaderPostServiceStarter
import org.sitebay.android.ui.reader.services.post.ReaderPostServiceStarter.UpdateAction
import javax.inject.Inject

@Reusable
class ReaderPostServiceStarterWrapper @Inject constructor() {
    fun startServiceForTag(context: Context, readerTag: ReaderTag, action: UpdateAction) =
            ReaderPostServiceStarter.startServiceForTag(context, readerTag, action)
}

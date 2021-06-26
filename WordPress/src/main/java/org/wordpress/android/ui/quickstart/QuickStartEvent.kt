package org.sitebay.android.ui.quickstart

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.sitebay.android.fluxc.store.QuickStartStore.QuickStartTask

/**
 * Container for passing around QuickStartTask to destinations and retaining it there
 **/
@Parcelize
@SuppressLint("ParcelCreator")
data class QuickStartEvent(val task: QuickStartTask) : Parcelable {
    companion object {
        const val KEY = "quick_start_event"
    }
}

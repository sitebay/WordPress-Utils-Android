package org.sitebay.android.ui.stats

import org.sitebay.android.viewmodel.ResourceProvider
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class StatsUtilsWrapper @Inject constructor(
    val resourceProvider: ResourceProvider
) {
    fun getSinceLabelLowerCase(date: Date): String {
        return StatsUtils.getSinceLabel(resourceProvider, date).toLowerCase(Locale.getDefault())
    }
}

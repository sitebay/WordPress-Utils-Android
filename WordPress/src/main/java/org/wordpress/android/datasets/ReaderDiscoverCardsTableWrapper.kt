package org.sitebay.android.datasets

import dagger.Reusable
import javax.inject.Inject

@Reusable
class ReaderDiscoverCardsTableWrapper @Inject constructor() {
    fun loadDiscoverCardsJsons() = ReaderDiscoverCardsTable.loadDiscoverCardsJsons()
}

package org.sitebay.android.ui.reader.utils

import dagger.Reusable
import org.sitebay.android.models.ReaderTag
import javax.inject.Inject

/**
 * Injectable wrapper around ReaderTag.
 *
 * ReaderTag interface is consisted of static methods, which make the client code difficult to test/mock.
 * Main purpose of this wrapper is to make testing easier.
 *
 */
@Reusable
class ReaderTagWrapper @Inject constructor() {
    fun createDiscoverPostCardsTag(): ReaderTag =
            ReaderTag.createDiscoverPostCardsTag()
}

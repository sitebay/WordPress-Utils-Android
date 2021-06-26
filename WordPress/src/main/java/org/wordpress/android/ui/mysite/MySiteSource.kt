package org.sitebay.android.ui.mysite

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import org.sitebay.android.ui.mysite.MySiteUiState.PartialState

interface MySiteSource<T : PartialState> {
    fun buildSource(coroutineScope: CoroutineScope, siteId: Int): LiveData<T>
    interface SiteIndependentSource<T : PartialState> : MySiteSource<T> {
        fun buildSource(coroutineScope: CoroutineScope): LiveData<T>
        override fun buildSource(coroutineScope: CoroutineScope, siteId: Int): LiveData<T> = buildSource(coroutineScope)
    }
}

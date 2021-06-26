package org.sitebay.android.util

import dagger.Reusable
import org.sitebay.android.WordPress
import javax.inject.Inject

@Reusable
class NetworkUtilsWrapper @Inject constructor() {
    /**
     * Returns true if a network connection is available.
     */
    fun isNetworkAvailable() = NetworkUtils.isNetworkAvailable(WordPress.getContext())
}

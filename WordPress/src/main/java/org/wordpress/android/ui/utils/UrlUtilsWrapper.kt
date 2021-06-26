package org.sitebay.android.ui.utils

import org.sitebay.android.util.UrlUtils
import javax.inject.Inject

class UrlUtilsWrapper
@Inject constructor() {
    fun getHost(url: String): String = UrlUtils.getHost(url)
}

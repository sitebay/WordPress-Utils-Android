package org.sitebay.android.ui.deeplinks

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.sitebay.android.util.UriWrapper

fun buildUri(host: String?, vararg path: String): UriWrapper {
    val uri = mock<UriWrapper>()
    if (host != null) {
        whenever(uri.host).thenReturn(host)
    }
    if (path.isNotEmpty()) {
        whenever(uri.pathSegments).thenReturn(path.toList())
    }
    return uri
}

fun buildUri(
    host: String? = null,
    queryParam1: Pair<String, String>? = null,
    queryParam2: Pair<String, String>? = null
): UriWrapper {
    val uri = mock<UriWrapper>()
    if (host != null) {
        whenever(uri.host).thenReturn(host)
    }
    if (queryParam1 != null) {
        whenever(uri.getQueryParameter(queryParam1.first)).thenReturn(queryParam1.second)
    }
    if (queryParam2 != null) {
        whenever(uri.getQueryParameter(queryParam2.first)).thenReturn(queryParam2.second)
    }
    return uri
}

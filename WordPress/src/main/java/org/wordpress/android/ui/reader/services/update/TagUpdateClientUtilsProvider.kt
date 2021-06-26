package org.sitebay.android.ui.reader.services.update

import org.sitebay.android.WordPress
import org.sitebay.android.networking.RestClientUtils
import javax.inject.Inject

class TagUpdateClientUtilsProvider @Inject constructor() {
    fun getRestClientForTagUpdate(): RestClientUtils {
        return WordPress.getRestClientUtilsV1_3()
    }

    fun getTagUpdateEndpointURL(): String {
        return WordPress.getRestClientUtilsV1_3().restClient.endpointURL
    }

    fun getRestClientForInterestTags(): RestClientUtils {
        return WordPress.getRestClientUtilsV2()
    }
}

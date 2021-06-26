package org.sitebay.android.ui.suggestion

import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.ui.suggestion.SuggestionType.Users
import org.sitebay.android.ui.suggestion.SuggestionType.XPosts
import javax.inject.Inject

class SuggestionSourceProvider @Inject constructor(
    private val suggestionSourceSubcomponentFactory: SuggestionSourceSubcomponent.Factory
) {
    fun get(type: SuggestionType, site: SiteModel): SuggestionSource {
        val factory = suggestionSourceSubcomponentFactory.create(site)
        return when (type) {
            XPosts -> factory.xPostSuggestionSource()
            Users -> factory.userSuggestionSource()
        }
    }
}

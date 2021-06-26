package org.sitebay.android.ui.suggestion.util

import android.content.Context
import org.sitebay.android.datasets.UserSuggestionTable
import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.models.UserSuggestion
import org.sitebay.android.ui.suggestion.Suggestion
import org.sitebay.android.ui.suggestion.adapters.SuggestionAdapter
import org.sitebay.android.util.SiteUtils

object SuggestionUtils {
    @JvmStatic
    fun setupUserSuggestions(
        site: SiteModel,
        context: Context,
        connectionManager: SuggestionServiceConnectionManager
    ): SuggestionAdapter = setupUserSuggestions(
            site.siteId,
            context,
            connectionManager,
            SiteUtils.isAccessedViaWPComRest(site)
    )

    @JvmStatic
    fun setupUserSuggestions(
        siteId: Long,
        context: Context,
        connectionManager: SuggestionServiceConnectionManager,
        isWPCom: Boolean
    ): SuggestionAdapter {
        val initialSuggestions = setupUserSuggestions(siteId, connectionManager, isWPCom)
        return SuggestionAdapter(context, '@').apply {
            suggestionList = Suggestion.fromUserSuggestions(initialSuggestions)
        }
    }

    private fun setupUserSuggestions(
        siteId: Long,
        serviceConnectionManager: SuggestionServiceConnectionManager,
        isWPCom: Boolean
    ): List<UserSuggestion> {
        if (!isWPCom) {
            return emptyList()
        }
        serviceConnectionManager.bindToService()

        // Immediately return any already saved suggestions
        return UserSuggestionTable.getSuggestionsForSite(siteId) ?: emptyList()
    }
}

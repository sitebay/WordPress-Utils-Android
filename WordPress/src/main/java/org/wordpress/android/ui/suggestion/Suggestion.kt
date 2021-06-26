package org.sitebay.android.ui.suggestion

import org.sitebay.android.fluxc.model.XPostSiteModel
import org.sitebay.android.models.UserSuggestion

data class Suggestion(val avatarUrl: String, val value: String, val displayValue: String) {
    companion object {
        fun fromUserSuggestions(userSuggestions: List<UserSuggestion>): List<Suggestion> =
                userSuggestions.map {
                    Suggestion(it.imageUrl, it.userLogin, it.displayName)
                }

        fun fromXpost(xpost: XPostSiteModel): Suggestion = Suggestion(xpost.blavatar, xpost.subdomain, xpost.title)
    }
}

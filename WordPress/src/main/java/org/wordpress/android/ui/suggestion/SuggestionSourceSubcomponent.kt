package org.sitebay.android.ui.suggestion

import dagger.BindsInstance
import dagger.Module
import dagger.Subcomponent
import org.sitebay.android.fluxc.model.SiteModel

@Subcomponent
interface SuggestionSourceSubcomponent {
    fun userSuggestionSource(): UserSuggestionSource
    fun xPostSuggestionSource(): XPostsSuggestionSource

    @Subcomponent.Factory
    interface Factory {
        fun create(
            @BindsInstance site: SiteModel
        ): SuggestionSourceSubcomponent
    }

    @Module(subcomponents = [ SuggestionSourceSubcomponent::class ])
    interface SuggestionSourceModule
}

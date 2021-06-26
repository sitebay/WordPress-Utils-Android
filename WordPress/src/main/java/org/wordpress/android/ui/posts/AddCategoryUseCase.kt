package org.sitebay.android.ui.posts

import dagger.Reusable
import org.sitebay.android.fluxc.Dispatcher
import org.sitebay.android.fluxc.generated.TaxonomyActionBuilder
import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.fluxc.model.TermModel
import org.sitebay.android.fluxc.store.TaxonomyStore
import org.sitebay.android.fluxc.store.TaxonomyStore.RemoteTermPayload
import javax.inject.Inject

@Reusable
class AddCategoryUseCase @Inject constructor(
    private val dispatcher: Dispatcher
) {
    fun addCategory(categoryName: String, parentCategoryId: Long, siteModel: SiteModel) {
        val newCategory = TermModel()
        newCategory.taxonomy = TaxonomyStore.DEFAULT_TAXONOMY_CATEGORY
        newCategory.name = categoryName
        newCategory.parentRemoteId = parentCategoryId
        val payload = RemoteTermPayload(newCategory, siteModel)
        dispatcher.dispatch(TaxonomyActionBuilder.newPushTermAction(payload))
    }
}

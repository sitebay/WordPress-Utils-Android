package org.sitebay.android.ui.posts

import dagger.Reusable
import org.apache.commons.text.StringEscapeUtils
import org.sitebay.android.fluxc.Dispatcher
import org.sitebay.android.fluxc.generated.TaxonomyActionBuilder
import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.fluxc.model.TermModel
import org.sitebay.android.fluxc.store.TaxonomyStore
import org.sitebay.android.fluxc.utils.AppLogWrapper
import org.sitebay.android.models.CategoryNode
import org.sitebay.android.models.wrappers.CategoryNodeWrapper
import org.sitebay.android.util.AppLog.T.PREPUBLISHING_NUDGES
import java.util.ArrayList
import javax.inject.Inject

@Reusable
class GetCategoriesUseCase @Inject constructor(
    private val taxonomyStore: TaxonomyStore,
    private val dispatcher: Dispatcher,
    private val appLogWrapper: AppLogWrapper,
    private val categoryNodeWrapper: CategoryNodeWrapper
) {
    fun getPostCategoriesString(
        editPostRepository: EditPostRepository,
        siteModel: SiteModel
    ): String {
        val post = editPostRepository.getPost()
                if (post == null) {
                    appLogWrapper.d(PREPUBLISHING_NUDGES, "Post is null in EditPostRepository")
                    return ""
                }
        val categories: List<TermModel> = taxonomyStore.getCategoriesForPost(
                post,
                siteModel
        )
        return formatCategories(categories)
    }

    fun getPostCategories(editPostRepository: EditPostRepository) =
            editPostRepository.getPost()?.categoryIdList ?: listOf()

    fun getSiteCategories(siteModel: SiteModel): ArrayList<CategoryNode> {
        val rootCategory = categoryNodeWrapper.createCategoryTreeFromList(
                getCategoriesForSite(siteModel)
        )
        return categoryNodeWrapper.getSortedListOfCategoriesFromRoot(rootCategory)
    }

    fun fetchSiteCategories(siteModel: SiteModel) {
        dispatcher.dispatch(TaxonomyActionBuilder.newFetchCategoriesAction(siteModel))
    }

    private fun formatCategories(categoryList: List<TermModel>): String {
        if (categoryList.isEmpty()) return ""

        val formattedCategories = categoryList.joinToString { it -> it.name }
        return StringEscapeUtils.unescapeHtml4(formattedCategories)
    }

    private fun getCategoriesForSite(siteModel: SiteModel): List<TermModel> {
        return taxonomyStore.getCategoriesForSite(siteModel)
    }
}

package org.sitebay.android.models.wrappers

import dagger.Reusable
import org.sitebay.android.fluxc.model.TermModel
import org.sitebay.android.models.CategoryNode
import java.util.ArrayList
import javax.inject.Inject

@Reusable
class CategoryNodeWrapper @Inject constructor() {
    fun createCategoryTreeFromList(categories: List<TermModel>): CategoryNode =
            CategoryNode.createCategoryTreeFromList(categories)

    fun getSortedListOfCategoriesFromRoot(node: CategoryNode): ArrayList<CategoryNode> =
            CategoryNode.getSortedListOfCategoriesFromRoot(node)
}

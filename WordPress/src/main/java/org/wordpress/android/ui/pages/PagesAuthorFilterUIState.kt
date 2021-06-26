package org.sitebay.android.ui.pages

import org.sitebay.android.ui.posts.AuthorFilterListItemUIState
import org.sitebay.android.ui.posts.AuthorFilterSelection

data class PagesAuthorFilterUIState(
    val isAuthorFilterVisible: Boolean,
    val authorFilterSelection: AuthorFilterSelection,
    val authorFilterItems: List<AuthorFilterListItemUIState>
)

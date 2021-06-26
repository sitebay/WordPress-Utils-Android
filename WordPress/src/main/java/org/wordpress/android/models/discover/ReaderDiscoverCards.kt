package org.sitebay.android.models.discover

import org.sitebay.android.models.ReaderBlog
import org.sitebay.android.models.ReaderPost
import org.sitebay.android.models.ReaderTagList

data class ReaderDiscoverCards(val cards: List<ReaderDiscoverCard>)

sealed class ReaderDiscoverCard {
    object WelcomeBannerCard : ReaderDiscoverCard()
    data class InterestsYouMayLikeCard(val interests: ReaderTagList) : ReaderDiscoverCard()
    data class ReaderPostCard(val post: ReaderPost) : ReaderDiscoverCard()
    data class ReaderRecommendedBlogsCard(val blogs: List<ReaderBlog>) : ReaderDiscoverCard()
}

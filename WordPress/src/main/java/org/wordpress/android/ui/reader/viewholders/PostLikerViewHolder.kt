package org.sitebay.android.ui.reader.viewholders

import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import org.sitebay.android.R
import org.sitebay.android.ui.engagement.EngageItem.Liker
import org.sitebay.android.ui.engagement.EngagedPeopleViewHolder
import org.sitebay.android.util.GravatarUtils
import org.sitebay.android.util.image.ImageManager
import org.sitebay.android.util.image.ImageType

class PostLikerViewHolder(
    parent: ViewGroup,
    private val imageManager: ImageManager,
    private val context: Context
) : EngagedPeopleViewHolder(parent, R.layout.liker_face_item) {
    private val likerAvatar = itemView.findViewById<ImageView>(R.id.liker_avatar)

    fun bind(liker: Liker) {
        val likerAvatarUrl = GravatarUtils.fixGravatarUrl(
                liker.userAvatarUrl,
                context.resources.getDimensionPixelSize(R.dimen.avatar_sz_small)
        )

        this.likerAvatar.setOnClickListener(null)
        imageManager.loadIntoCircle(this.likerAvatar, ImageType.AVATAR_WITH_BACKGROUND, likerAvatarUrl)
    }
}

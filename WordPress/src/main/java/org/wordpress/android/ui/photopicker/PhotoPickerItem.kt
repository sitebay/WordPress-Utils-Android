package org.sitebay.android.ui.photopicker

import org.sitebay.android.util.UriWrapper

@Deprecated("This class is being refactored, if you implement any change, please also update " +
        "{@link org.sitebay.android.ui.mediapicker.MediaItem}")
data class PhotoPickerItem(
    val id: Long = 0,
    val uri: UriWrapper? = null,
    val isVideo: Boolean = false
)

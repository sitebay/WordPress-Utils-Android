package org.sitebay.android.util.image

enum class ImageType {
    @Deprecated(
            message = "Use AVATAR_WITH_BACKGROUND or AVATAR_WITHOUT_BACKGROUND instead.",
            replaceWith = ReplaceWith(
                    expression = "AVATAR_WITH_BACKGROUND",
                    imports = ["org.sitebay.android.util.image.ImageType"]))
    AVATAR,
    AVATAR_WITH_BACKGROUND,
    AVATAR_WITHOUT_BACKGROUND,
    BLAVATAR,
    P2_BLAVATAR,
    BLAVATAR_ROUNDED_CORNERS,
    P2_BLAVATAR_ROUNDED_CORNERS,
    BLAVATAR_CIRCULAR,
    P2_BLAVATAR_CIRCULAR,
    IMAGE,
    PHOTO,
    PHOTO_ROUNDED_CORNERS,
    PLAN,
    PLUGIN,
    THEME,
    UNKNOWN,
    USER,
    VIDEO,
    ICON,
    NO_PLACEHOLDER
}

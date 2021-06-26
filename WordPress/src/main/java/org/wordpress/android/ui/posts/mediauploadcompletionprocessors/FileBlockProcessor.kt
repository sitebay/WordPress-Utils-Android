package org.sitebay.android.ui.posts.mediauploadcompletionprocessors

import com.google.gson.JsonObject
import org.jsoup.nodes.Document
import org.sitebay.android.util.helpers.MediaFile

/**
 * When a File Block's upload is complete, this processor replaces the href pointing to a local file url with a
 * remote url for all a tags present within the wp:file block.
 */
class FileBlockProcessor(localId: String?, mediaFile: MediaFile?) : BlockProcessor(localId, mediaFile) {
    override fun processBlockContentDocument(document: Document?): Boolean {
        val hyperLinkTargets = document?.select(HYPERLINK_TAG)

        hyperLinkTargets?.let {
            for (target in hyperLinkTargets) {
                // replaces the href attribute's local url with the remote counterpart.
                target.attr(HREF_ATTRIBUTE, mRemoteUrl)
            }
            return true
        }
        return false
    }

    override fun processBlockJsonAttributes(jsonAttributes: JsonObject?): Boolean {
        val id = jsonAttributes?.get(ID_ATTRIBUTE)

        return if (id != null && !id.isJsonNull && id.asString == mLocalId) {
            jsonAttributes.apply {
                addProperty(ID_ATTRIBUTE, Integer.parseInt(mRemoteId))
                addProperty(HREF_ATTRIBUTE, mRemoteUrl)
            }
            true
        } else {
            false
        }
    }

    companion object {
        const val HYPERLINK_TAG = "a"
        const val HREF_ATTRIBUTE = "href"
        const val ID_ATTRIBUTE = "id"
    }
}

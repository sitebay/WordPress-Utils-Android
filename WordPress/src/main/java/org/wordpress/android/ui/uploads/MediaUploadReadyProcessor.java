package org.sitebay.android.ui.uploads;

import androidx.annotation.Nullable;

import org.sitebay.android.WordPress;
import org.sitebay.android.editor.AztecEditorFragment;
import org.sitebay.android.fluxc.model.PostModel;
import org.sitebay.android.fluxc.model.SiteModel;
import org.sitebay.android.ui.media.services.MediaUploadReadyListener;
import org.sitebay.android.ui.posts.PostUtils;
import org.sitebay.android.ui.prefs.AppPrefs;
import org.sitebay.android.ui.stories.SaveStoryGutenbergBlockUseCase;
import org.sitebay.android.util.helpers.MediaFile;

import javax.inject.Inject;


public class MediaUploadReadyProcessor implements MediaUploadReadyListener {
    @Inject SaveStoryGutenbergBlockUseCase mSaveStoryGutenbergBlockUseCase;

    @Inject public MediaUploadReadyProcessor() {
        ((WordPress) WordPress.getContext().getApplicationContext()).component().inject(this);
    }

    @Override
    public PostModel replaceMediaFileWithUrlInPost(@Nullable PostModel post, String localMediaId, MediaFile mediaFile,
                                                   @Nullable SiteModel site) {
        if (post != null) {
            boolean showAztecEditor = AppPrefs.isAztecEditorEnabled();
            boolean showGutenbergEditor = AppPrefs.isGutenbergEditorEnabled();

            if (PostUtils.contentContainsWPStoryGutenbergBlocks(post.getContent())) {
                mSaveStoryGutenbergBlockUseCase
                    .replaceLocalMediaIdsWithRemoteMediaIdsInPost(post, site, mediaFile);
            } else if (showGutenbergEditor && PostUtils.contentContainsGutenbergBlocks(post.getContent())) {
                String siteUrl = site != null ? site.getUrl() : "";
                post.setContent(
                        PostUtils.replaceMediaFileWithUrlInGutenbergPost(post.getContent(), localMediaId, mediaFile,
                                siteUrl));
            } else if (showAztecEditor) {
                post.setContent(AztecEditorFragment.replaceMediaFileWithUrl(WordPress.getContext(), post.getContent(),
                                                                            localMediaId, mediaFile));
            }
        }

        return post;
    }

    @Override
    public PostModel markMediaUploadFailedInPost(@Nullable PostModel post, String localMediaId,
                                                 final MediaFile mediaFile) {
        if (post != null) {
            boolean showAztecEditor = AppPrefs.isAztecEditorEnabled();
            boolean showGutenbergEditor = AppPrefs.isGutenbergEditorEnabled();

            if (showGutenbergEditor) {
                // TODO check if anything needs be done in Gutenberg
            } else if (showAztecEditor) {
                post.setContent(AztecEditorFragment.markMediaFailed(WordPress.getContext(), post.getContent(),
                                                                    localMediaId, mediaFile));
            }
        }

        return post;
    }
}

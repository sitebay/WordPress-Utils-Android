package org.sitebay.android.editor;

import org.sitebay.android.util.helpers.MediaFile;

public interface EditorMediaUploadListener {
    void onMediaUploadRetry(String localId, EditorFragmentAbstract.MediaType mediaType);
    void onMediaUploadReattached(String localId, float currentProgress);
    void onMediaUploadSucceeded(String localId, MediaFile mediaFile);
    void onMediaUploadProgress(String localId, float progress);
    void onMediaUploadFailed(String localId);
    void onGalleryMediaUploadSucceeded(long galleryId, long remoteId, int remaining);
}

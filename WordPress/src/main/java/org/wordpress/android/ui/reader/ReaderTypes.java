package org.sitebay.android.ui.reader;

import org.sitebay.android.ui.reader.tracker.ReaderTracker;

public class ReaderTypes {
    public static final ReaderPostListType DEFAULT_POST_LIST_TYPE = ReaderPostListType.TAG_FOLLOWED;

    public enum ReaderPostListType {
        TAG_FOLLOWED(ReaderTracker.SOURCE_FOLLOWING), // list posts in a followed tag
        TAG_PREVIEW(ReaderTracker.SOURCE_TAG_PREVIEW), // list posts in a specific tag
        BLOG_PREVIEW(ReaderTracker.SOURCE_SITE_PREVIEW), // list posts in a specific blog/feed
        SEARCH_RESULTS(ReaderTracker.SOURCE_SEARCH); // list posts matching a specific search keyword or phrase

        private final String mSource;

        ReaderPostListType(String source) {
            mSource = source;
        }

        public boolean isTagType() {
            return this.equals(TAG_FOLLOWED) || this.equals(TAG_PREVIEW);
        }

        public String getSource() {
            return mSource;
        }
    }
}

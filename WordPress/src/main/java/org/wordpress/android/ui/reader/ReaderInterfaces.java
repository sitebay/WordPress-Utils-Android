package org.sitebay.android.ui.reader;

import android.view.View;

import org.sitebay.android.models.ReaderPost;
import org.sitebay.android.ui.reader.discover.ReaderPostCardActionType;

public class ReaderInterfaces {
    private ReaderInterfaces() {
        throw new AssertionError();
    }

    public interface OnPostSelectedListener {
        void onPostSelected(ReaderPost post);
    }

    /*
     * called from post detail fragment so toolbar can animate in/out when scrolling
     */
    public interface AutoHideToolbarListener {
        void onShowHideToolbar(boolean show);
    }

    /*
     * used by adapters to notify when data has been loaded
     */
    public interface DataLoadedListener {
        void onDataLoaded(boolean isEmpty);
    }

    /*
     * used by adapters to notify when follow button has been tapped
     */
    public interface OnFollowListener {
        void onFollowTapped(View view, String blogName, long blogId, long feedId);

        void onFollowingTapped();
    }

    /*
     * Used by adapters to notify when button on post list item is clicked. This interface was created during
     * refactoring for the new Discover tab. It isn't ideal but we wanted to re-use some of the legacy code but
     * refactoring everything was out of scope of the project.
     */
    public interface OnPostListItemButtonListener {
        void onButtonClicked(ReaderPost post, ReaderPostCardActionType actionType);
    }
}

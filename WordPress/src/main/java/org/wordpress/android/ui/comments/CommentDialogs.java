package org.sitebay.android.ui.comments;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;

import org.sitebay.android.R;

/**
 * Dialogs related to comment moderation displayed from CommentsActivity and NotificationsActivity
 *
 * @deprecated
 * Comments are being refactored as part of Comments Unification project. If you are adding any
 * features or modifying this class, please ping develric or klymyam
 */
@Deprecated
class CommentDialogs {
    static final int ID_COMMENT_DLG_APPROVING = 100;
    static final int ID_COMMENT_DLG_DISAPPROVING = 101;
    static final int ID_COMMENT_DLG_SPAMMING = 102;
    static final int ID_COMMENT_DLG_TRASHING = 103;
    static final int ID_COMMENT_DLG_DELETING = 104;

    private CommentDialogs() {
        throw new AssertionError();
    }

    static Dialog createCommentDialog(Activity activity, int dialogId) {
        final int resId;
        switch (dialogId) {
            case ID_COMMENT_DLG_APPROVING:
                resId = R.string.dlg_approving_comments;
                break;
            case ID_COMMENT_DLG_DISAPPROVING:
                resId = R.string.dlg_unapproving_comments;
                break;
            case ID_COMMENT_DLG_TRASHING:
                resId = R.string.dlg_trashing_comments;
                break;
            case ID_COMMENT_DLG_SPAMMING:
                resId = R.string.dlg_spamming_comments;
                break;
            case ID_COMMENT_DLG_DELETING:
                resId = R.string.dlg_deleting_comments;
                break;
            default:
                return null;
        }

        ProgressDialog dialog = new ProgressDialog(activity);
        dialog.setMessage(activity.getString(resId));
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);

        return dialog;
    }
}

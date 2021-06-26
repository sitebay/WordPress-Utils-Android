package org.sitebay.android.ui.activitylog.detail

sealed class ActivityLogDetailNavigationEvents {
    data class ShowBackupDownload(val model: ActivityLogDetailModel) : ActivityLogDetailNavigationEvents()
    data class ShowRestore(val model: ActivityLogDetailModel) : ActivityLogDetailNavigationEvents()
}

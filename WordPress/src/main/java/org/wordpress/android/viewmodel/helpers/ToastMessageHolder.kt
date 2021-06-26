package org.sitebay.android.viewmodel.helpers

import android.content.Context
import androidx.annotation.StringRes
import org.sitebay.android.util.ToastUtils
import org.sitebay.android.util.ToastUtils.Duration

class ToastMessageHolder(
    @StringRes val messageRes: Int,
    val duration: Duration
) {
    fun show(context: Context) {
        ToastUtils.showToast(context, messageRes, duration)
    }
}

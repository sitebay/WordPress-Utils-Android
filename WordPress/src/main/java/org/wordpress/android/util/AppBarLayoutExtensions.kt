package org.sitebay.android.util

import androidx.annotation.IdRes
import com.google.android.material.appbar.AppBarLayout

fun AppBarLayout.setLiftOnScrollTargetViewIdAndRequestLayout(@IdRes liftOnScrollTargetViewId: Int) {
    this.post {
        setLiftOnScrollTargetViewId(liftOnScrollTargetViewId)
        requestLayout()
    }
}

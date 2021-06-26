package org.sitebay.android.ui

import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import org.sitebay.android.R

abstract class ViewPagerFragment : Fragment {
    constructor() : super()

    constructor(@LayoutRes contentLayoutId: Int) : super(contentLayoutId)

    companion object {
        @JvmStatic
        fun restoreOriginalViewId(view: View) {
            val originalId = view.getTag(R.id.original_view_pager_fragment_id_tag_key)
            if (originalId != null) {
                view.id = originalId as Int
            }
        }

        @JvmStatic
        fun setUniqueIdToView(view: View): Int {
            val newId = View.generateViewId()
            if (view.getTag(R.id.original_view_pager_fragment_id_tag_key) == null) {
                view.setTag(R.id.original_view_pager_fragment_id_tag_key, view.id)
            }
            view.id = newId
            return newId
        }
    }

    /**
     * Provide a scrollable view that will be used with "lift on scroll" functionality of AppBar in parent
     * fragment/activity. ID will of the scrollable view be set to unique one using View.generateViewId()
     */
    abstract fun getScrollableViewForUniqueIdProvision(): View?

    /**
     * It is expected that onResume will be called when the fragment in ViewPager becomes active.
     * This is default behavior of FragmentStateAdapter and FragmentStatePagerAdapter with
     * BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT flag.
     *
     * onResume() is called after fragment view is created, so it's safe to change it's ID as long as you got a
     * reference to it beforehand, or in all cases when View Binding using Kotlin Extensions, since the first time you
     * access a view by original ID it's stored in cache for future access.
     */
    override fun onResume() {
        super.onResume()

        // set unique ID to scrollable view and notify parent fragment/activity
        val scrollableContainer = getScrollableViewForUniqueIdProvision()
        scrollableContainer?.let {
            val host = when {
                parentFragment is ScrollableViewInitializedListener -> {
                    parentFragment
                }
                activity is ScrollableViewInitializedListener -> {
                    activity
                }
                else -> {
                    null
                }
            }
            host?.let {
                (host as ScrollableViewInitializedListener).onScrollableViewInitialized(
                        setUniqueIdToView(scrollableContainer)
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        val scrollableContainer = getScrollableViewForUniqueIdProvision()
        scrollableContainer?.let {
            // restore original view ID, so it's state could be restored correctly
            restoreOriginalViewId(scrollableContainer)
        }
    }
}

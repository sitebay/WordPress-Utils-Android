package org.sitebay.android.ui.pages

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import org.sitebay.android.R
import org.sitebay.android.WordPress
import org.sitebay.android.databinding.PagesActivityBinding
import org.sitebay.android.push.NotificationType
import org.sitebay.android.push.NotificationsProcessingService.ARG_NOTIFICATION_TYPE
import org.sitebay.android.ui.LocaleAwareActivity
import org.sitebay.android.ui.notifications.SystemNotificationsTracker
import org.sitebay.android.ui.posts.BasicFragmentDialog.BasicDialogNegativeClickInterface
import org.sitebay.android.ui.posts.BasicFragmentDialog.BasicDialogPositiveClickInterface
import javax.inject.Inject

const val EXTRA_PAGE_REMOTE_ID_KEY = "extra_page_remote_id_key"
const val EXTRA_PAGE_PARENT_ID_KEY = "extra_page_parent_id_key"

class PagesActivity : LocaleAwareActivity(),
        BasicDialogPositiveClickInterface,
        BasicDialogNegativeClickInterface {
    @Inject internal lateinit var systemNotificationTracker: SystemNotificationsTracker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as WordPress).component().inject(this)
        setContentView(PagesActivityBinding.inflate(layoutInflater).root)

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (intent.hasExtra(ARG_NOTIFICATION_TYPE)) {
            val notificationType: NotificationType =
                    intent.getSerializableExtra(ARG_NOTIFICATION_TYPE) as NotificationType
            systemNotificationTracker.trackTappedNotification(notificationType)
        }

        if (intent.hasExtra(EXTRA_PAGE_REMOTE_ID_KEY)) {
            val pageId = intent.getLongExtra(EXTRA_PAGE_REMOTE_ID_KEY, -1)
            supportFragmentManager.findFragmentById(R.id.fragment_container)?.let {
                (it as PagesFragment).onSpecificPageRequested(pageId)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPositiveClicked(instanceTag: String) {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (fragment is PagesFragment) {
            fragment.onPositiveClickedForBasicDialog(instanceTag)
        } else {
            throw IllegalStateException("PagesFragment is required to consume this event.")
        }
    }

    override fun onNegativeClicked(instanceTag: String) {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (fragment is PagesFragment) {
            fragment.onNegativeClickedForBasicDialog(instanceTag)
        } else {
            throw IllegalStateException("PagesFragment is required to consume this event.")
        }
    }
}

package org.sitebay.android.ui.quickstart;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.sitebay.android.R;
import org.sitebay.android.WordPress;
import org.sitebay.android.analytics.AnalyticsTracker;
import org.sitebay.android.analytics.AnalyticsTracker.Stat;
import org.sitebay.android.fluxc.store.QuickStartStore;
import org.sitebay.android.fluxc.store.QuickStartStore.QuickStartTask;
import org.sitebay.android.push.NotificationPushIds;
import org.sitebay.android.push.NotificationType;
import org.sitebay.android.push.NotificationsProcessingService;
import org.sitebay.android.ui.main.MySiteFragment;
import org.sitebay.android.ui.main.WPMainActivity;
import org.sitebay.android.ui.notifications.SystemNotificationsTracker;
import org.sitebay.android.ui.prefs.AppPrefs;

import javax.inject.Inject;

import static org.sitebay.android.push.NotificationsProcessingService.ARG_NOTIFICATION_TYPE;

public class QuickStartReminderReceiver extends BroadcastReceiver {
    public static final String ARG_QUICK_START_TASK_BATCH = "ARG_QUICK_START_TASK_BATCH";

    @Inject QuickStartStore mQuickStartStore;
    @Inject SystemNotificationsTracker mSystemNotificationsTracker;

    @Override
    public void onReceive(Context context, Intent intent) {
        ((WordPress) context.getApplicationContext()).component().inject(this);

        Bundle bundleWithQuickStartTaskDetails = intent.getBundleExtra(ARG_QUICK_START_TASK_BATCH);

        if (bundleWithQuickStartTaskDetails == null) {
            return;
        }

        int siteLocalId = AppPrefs.getSelectedSite();

        QuickStartTaskDetails quickStartTaskDetails = (QuickStartTaskDetails) bundleWithQuickStartTaskDetails
                .getSerializable(QuickStartTaskDetails.KEY);

        // Failsafes
        if (quickStartTaskDetails == null || siteLocalId == -1 || AppPrefs.isQuickStartDisabled()
            || !mQuickStartStore.hasDoneTask(siteLocalId, QuickStartTask.CREATE_SITE)
            || mQuickStartStore.getQuickStartCompleted(siteLocalId)
            || mQuickStartStore.hasDoneTask(siteLocalId, quickStartTaskDetails.getTask())) {
            return;
        }

        Intent resultIntent = new Intent(context, WPMainActivity.class);
        resultIntent.putExtra(MySiteFragment.ARG_QUICK_START_TASK, true);
        NotificationType notificationType = NotificationType.QUICK_START_REMINDER;
        resultIntent.putExtra(ARG_NOTIFICATION_TYPE, notificationType);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK
                              | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        resultIntent.setAction(Intent.ACTION_MAIN);
        resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent notificationContentIntent =
                PendingIntent.getActivity(context, NotificationPushIds.QUICK_START_REMINDER_NOTIFICATION_ID,
                        resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        Notification notification = new NotificationCompat.Builder(context,
                context.getString(R.string.notification_channel_reminder_id))
                .setSmallIcon(R.drawable.ic_app_white_24dp)
                .setContentTitle(context.getString(quickStartTaskDetails.getTitleResId()))
                .setContentText(context.getString(quickStartTaskDetails.getSubtitleResId()))
                .setOnlyAlertOnce(true)
                .setAutoCancel(true)
                .setContentIntent(notificationContentIntent)
                .setDeleteIntent(NotificationsProcessingService
                        .getPendingIntentForNotificationDismiss(context,
                                NotificationPushIds.QUICK_START_REMINDER_NOTIFICATION_ID,
                                notificationType))
                .build();

        notificationManager.notify(NotificationPushIds.QUICK_START_REMINDER_NOTIFICATION_ID, notification);
        AnalyticsTracker.track(Stat.QUICK_START_NOTIFICATION_SENT);
        mSystemNotificationsTracker.trackShownNotification(notificationType);
    }
}

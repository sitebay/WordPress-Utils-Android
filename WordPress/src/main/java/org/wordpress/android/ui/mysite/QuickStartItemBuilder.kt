package org.sitebay.android.ui.mysite

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import org.sitebay.android.R
import org.sitebay.android.fluxc.model.DynamicCardType
import org.sitebay.android.fluxc.store.QuickStartStore.QuickStartTask
import org.sitebay.android.fluxc.store.QuickStartStore.QuickStartTask.CHECK_STATS
import org.sitebay.android.fluxc.store.QuickStartStore.QuickStartTask.CREATE_SITE
import org.sitebay.android.fluxc.store.QuickStartStore.QuickStartTask.EDIT_HOMEPAGE
import org.sitebay.android.fluxc.store.QuickStartStore.QuickStartTask.ENABLE_POST_SHARING
import org.sitebay.android.fluxc.store.QuickStartStore.QuickStartTask.EXPLORE_PLANS
import org.sitebay.android.fluxc.store.QuickStartStore.QuickStartTask.FOLLOW_SITE
import org.sitebay.android.fluxc.store.QuickStartStore.QuickStartTask.PUBLISH_POST
import org.sitebay.android.fluxc.store.QuickStartStore.QuickStartTask.REVIEW_PAGES
import org.sitebay.android.fluxc.store.QuickStartStore.QuickStartTask.UPDATE_SITE_TITLE
import org.sitebay.android.fluxc.store.QuickStartStore.QuickStartTask.UPLOAD_SITE_ICON
import org.sitebay.android.fluxc.store.QuickStartStore.QuickStartTask.VIEW_SITE
import org.sitebay.android.fluxc.store.QuickStartStore.QuickStartTaskType
import org.sitebay.android.fluxc.store.QuickStartStore.QuickStartTaskType.CUSTOMIZE
import org.sitebay.android.fluxc.store.QuickStartStore.QuickStartTaskType.GROW
import org.sitebay.android.fluxc.store.QuickStartStore.QuickStartTaskType.UNKNOWN
import org.sitebay.android.ui.mysite.MySiteItem.DynamicCard.QuickStartCard
import org.sitebay.android.ui.mysite.MySiteItem.DynamicCard.QuickStartCard.QuickStartTaskCard
import org.sitebay.android.ui.mysite.QuickStartRepository.QuickStartCategory
import org.sitebay.android.ui.mysite.dynamiccards.DynamicCardMenuFragment.DynamicCardMenuModel
import org.sitebay.android.ui.quickstart.QuickStartTaskDetails
import org.sitebay.android.ui.utils.ListItemInteraction
import org.sitebay.android.ui.utils.UiString.UiStringRes
import javax.inject.Inject
import kotlin.math.roundToInt

class QuickStartItemBuilder
@Inject constructor() {
    fun build(
        quickStartCategory: QuickStartCategory,
        pinnedDynamicCardType: DynamicCardType?,
        onQuickStartCardMoreClick: (DynamicCardMenuModel) -> Unit,
        onQuickStartTaskClick: (QuickStartTask) -> Unit
    ): QuickStartCard {
        val accentColor = getAccentColor(quickStartCategory.taskType)
        val tasks = mutableListOf<QuickStartTaskCard>()
        tasks.addAll(quickStartCategory.uncompletedTasks.map { it.toUiItem(false, accentColor, onQuickStartTaskClick) })
        val completedTasks = quickStartCategory.completedTasks.map {
            it.toUiItem(
                    true,
                    accentColor,
                    onQuickStartTaskClick
            )
        }
        tasks.addAll(completedTasks)
        val dynamicCardType = quickStartCategory.taskType.toDynamicCardType()
        val isPinned = pinnedDynamicCardType == dynamicCardType
        return QuickStartCard(
                dynamicCardType,
                UiStringRes(getTitle(quickStartCategory.taskType)),
                tasks,
                accentColor,
                getProgress(tasks, completedTasks),
                ListItemInteraction.create(DynamicCardMenuModel(dynamicCardType, isPinned), onQuickStartCardMoreClick)
        )
    }

    private fun QuickStartTaskType.toDynamicCardType(): DynamicCardType {
        return when (this) {
            CUSTOMIZE -> DynamicCardType.CUSTOMIZE_QUICK_START
            GROW -> DynamicCardType.GROW_QUICK_START
            UNKNOWN -> throw IllegalArgumentException("Unexpected quick start type")
        }
    }

    private fun getProgress(tasks: List<QuickStartTaskCard>, completedTasks: List<QuickStartTaskCard>): Int {
        return if (tasks.isNotEmpty()) ((completedTasks.size / tasks.size.toFloat()) * 100).roundToInt() else 0
    }

    @ColorRes
    private fun getAccentColor(taskType: QuickStartTaskType): Int {
        return when (taskType) {
            CUSTOMIZE -> R.color.green_20
            GROW -> R.color.orange_40
            UNKNOWN -> throw IllegalArgumentException("Unexpected quick start type")
        }
    }

    private fun getTitle(taskType: QuickStartTaskType): Int {
        return when (taskType) {
            CUSTOMIZE -> R.string.quick_start_sites_type_customize
            GROW -> R.string.quick_start_sites_type_grow
            UNKNOWN -> throw IllegalArgumentException("Unexpected quick start type")
        }
    }

    private fun QuickStartTaskDetails.toUiItem(
        done: Boolean,
        accentColor: Int,
        onQuickStartTaskClick: (QuickStartTask) -> Unit
    ): QuickStartTaskCard {
        return QuickStartTaskCard(
                this.task,
                UiStringRes(this.titleResId),
                UiStringRes(this.subtitleResId),
                getIllustration(this.task),
                accentColor,
                done,
                ListItemInteraction.create(this.task, onQuickStartTaskClick)
        )
    }

    @DrawableRes
    private fun getIllustration(task: QuickStartTask): Int {
        return when (task) {
            UPDATE_SITE_TITLE -> R.drawable.img_illustration_quick_start_task_set_site_title
            UPLOAD_SITE_ICON -> R.drawable.img_illustration_quick_start_task_edit_site_icon
            VIEW_SITE -> R.drawable.img_illustration_quick_start_task_visit_your_site
            ENABLE_POST_SHARING -> R.drawable.img_illustration_quick_start_task_enable_post_sharing
            PUBLISH_POST -> R.drawable.img_illustration_quick_start_task_publish_post
            FOLLOW_SITE -> R.drawable.img_illustration_quick_start_task_follow_other_sites
            CHECK_STATS -> R.drawable.img_illustration_quick_start_task_check_site_stats
            EDIT_HOMEPAGE -> R.drawable.img_illustration_quick_start_task_edit_your_homepage
            REVIEW_PAGES -> R.drawable.img_illustration_quick_start_task_review_site_pages
            EXPLORE_PLANS -> R.drawable.img_illustration_quick_start_task_explore_plans
            CREATE_SITE -> R.drawable.img_illustration_quick_start_task_create_site
            QuickStartTask.UNKNOWN -> R.drawable.img_illustration_quick_start_task_placeholder
        }
    }
}

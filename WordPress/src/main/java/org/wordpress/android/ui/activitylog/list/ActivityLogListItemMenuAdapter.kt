package org.sitebay.android.ui.activitylog.list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import org.sitebay.android.R
import org.sitebay.android.ui.activitylog.list.ActivityLogListItem.SecondaryAction
import org.sitebay.android.ui.activitylog.list.ActivityLogListItem.SecondaryAction.DOWNLOAD_BACKUP
import org.sitebay.android.ui.activitylog.list.ActivityLogListItem.SecondaryAction.RESTORE
import org.sitebay.android.util.ColorUtils.setImageResourceWithTint
import org.sitebay.android.util.getColorResIdFromAttribute

class ActivityLogListItemMenuAdapter(
    context: Context
) : BaseAdapter() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val items: List<SecondaryAction> = SecondaryAction.values().toList()
    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(position: Int): Any {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        return items[position].itemId
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: ActivityListItemMenuHolder
        val view: View
        if (convertView == null) {
            view = inflater.inflate(R.layout.activity_log_list_item_menu_item, parent, false)
            holder = ActivityListItemMenuHolder(view)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ActivityListItemMenuHolder
        }

        val textRes: Int
        val iconRes: Int
        val colorRes = view.context.getColorResIdFromAttribute(R.attr.wpColorOnSurfaceMedium)
        when (items[position]) {
            RESTORE -> {
                textRes = R.string.activity_log_item_menu_restore_label
                iconRes = R.drawable.ic_history_white_24dp
            }
            DOWNLOAD_BACKUP -> {
                textRes = R.string.activity_log_item_menu_download_backup_label
                iconRes = R.drawable.ic_get_app_white_24dp
            }
        }
        holder.text.setText(textRes)
        holder.text.setTextColor(
                AppCompatResources.getColorStateList(
                        view.context,
                        colorRes
                )
        )
        setImageResourceWithTint(
                holder.icon,
                iconRes,
                colorRes
        )
        return view
    }

    internal inner class ActivityListItemMenuHolder(view: View) {
        val text: TextView = view.findViewById(R.id.text)
        val icon: ImageView = view.findViewById(R.id.image)
    }
}

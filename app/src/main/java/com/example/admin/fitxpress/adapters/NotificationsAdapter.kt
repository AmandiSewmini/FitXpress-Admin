/*
package com.example.admin.fitxpress.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.admin.fitxpress.R
import com.example.admin.fitxpress.models.AdminNotification

class NotificationsAdapter(
    private val notifications: List<AdminNotification>,
    private val onNotificationClick: (AdminNotification) -> Unit,
    private val onViewClick: (AdminNotification) -> Unit,
    private val onDismissClick: (AdminNotification) -> Unit
) : RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(notifications[position])
    }

    override fun getItemCount(): Int = notifications.size

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val notificationIconImageView: ImageView = itemView.findViewById(R.id.notificationIconImageView)
        private val unreadIndicator: View = itemView.findViewById(R.id.unreadIndicator)
        private val notificationTitleTextView: TextView = itemView.findViewById(R.id.notificationTitleTextView)
        private val notificationTimeTextView: TextView = itemView.findViewById(R.id.notificationTimeTextView)
        private val notificationMessageTextView: TextView = itemView.findViewById(R.id.notificationMessageTextView)
        private val notificationCategoryTextView: TextView = itemView.findViewById(R.id.notificationCategoryTextView)
        private val actionButtonsLayout: LinearLayout = itemView.findViewById(R.id.actionButtonsLayout)
        private val viewButton: Button = itemView.findViewById(R.id.viewButton)
        private val dismissButton: Button = itemView.findViewById(R.id.dismissButton)

        fun bind(notification: AdminNotification) {
            // Set notification content
            notificationTitleTextView.text = notification.title
            notificationMessageTextView.text = notification.message
            notificationTimeTextView.text = notification.getFormattedTime()
            notificationCategoryTextView.text = notification.category

            // Set category color
            val context = itemView.context
            notificationCategoryTextView.setTextColor(context.getColor(notification.getCategoryColor()))

            // Set icon based on category
            val iconResource = when (notification.category) {
                "ORDER" -> R.drawable.ic_orders
                "USER" -> R.drawable.ic_users
                "PRODUCT" -> R.drawable.ic_products
                "SYSTEM" -> R.drawable.ic_notifications
                else -> R.drawable.ic_notifications
            }
            notificationIconImageView.setImageResource(iconResource)

            // Show/hide unread indicator
            unreadIndicator.visibility = if (notification.isRead) View.GONE else View.VISIBLE

            // Set background opacity based on read status
            itemView.alpha = if (notification.isRead) 0.7f else 1.0f

            // Show action buttons for unread notifications
            if (!notification.isRead) {
                actionButtonsLayout.visibility = View.VISIBLE
            } else {
                actionButtonsLayout.visibility = View.GONE
            }

            // Set click listeners
            itemView.setOnClickListener {
                onNotificationClick(notification)
            }

            viewButton.setOnClickListener {
                onViewClick(notification)
            }

            dismissButton.setOnClickListener {
                onDismissClick(notification)
            }

            // Long click for additional options
            itemView.setOnLongClickListener {
                showNotificationOptions(notification)
                true
            }
        }

        private fun showNotificationOptions(notification: AdminNotification) {
            val context = itemView.context
            val options = arrayOf("Mark as Read", "Delete", "Cancel")

            android.app.AlertDialog.Builder(context)
                .setTitle("Notification Options")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> onNotificationClick(notification) // Mark as read
                        1 -> onDismissClick(notification) // Delete
                    }
                }
                .show()
        }
    }
}
*/

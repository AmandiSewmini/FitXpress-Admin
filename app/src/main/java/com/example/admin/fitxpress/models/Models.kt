package com.example.admin.fitxpress.models

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

// allows Product objects to be easily passed between Android components
@Parcelize
data class Product(
    var id: String = "",
    var name: String = "",
    var description: String = "",
    val price: Double = 0.0,
    val categoryId: String = "",
    val categoryName: String = "",
    val imageDrawableId: Int? = null,
    val imageUrl: String? = null,
    val stock: Int = 0,
    var isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable {
    @Exclude                   //tell Firebase to ignore these when writing the object to the database, as they are derived/utility functions
    fun getFormattedPrice(): String {
        return "Rs. ${String.format("%.2f", price)}"
    }

    @Exclude
    fun getStockStatus(): String {
        return when {
            stock <= 0 -> "Out of Stock"
            stock <= 10 -> "Low Stock"
            else -> "In Stock"
        }
    }
}

@Parcelize
data class Category(
    @DocumentId var id: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "", // Add imageUrl

    @get:PropertyName("isActive") // Add this annotation
    @set:PropertyName("isActive")
    var isActive: Boolean = true,

    val productCount: Int = 0, // Add productCount
    val stability: Int = 0,
    @ServerTimestamp val createdAt: Date? = null, // Firestore will set this on creation
    @ServerTimestamp var updatedAt: Date? = null
) : Parcelable

@Parcelize
data class Trainee(
    var id: String = "",
    var name: String = "",
    var email: String = "",
    var specialty: String = "",
    var experience: String = "",
    var fee: String = "",
    var imageUrl: String = "",
    var nextAvailableTime: Long = 0,
    var workingHours: WorkingHours = WorkingHours(),

    @get:PropertyName("isActive") //annotations are used by Firebase to correctly map the isActive
    @set:PropertyName("isActive")


    var isActive: Boolean = true
): Parcelable {
    // Check if trainee is available at specific time
    constructor() : this("", "", "", "", "", "", "", 0L, WorkingHours(), true)

    fun isAvailableAt(dateTime: Long): Boolean {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = dateTime

        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        // Check if it's within working hours
        if (hour < workingHours.startHour || hour >= workingHours.endHour) {
            return false
        }

        // Check if it's a working day (assuming Monday-Friday)
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            return false
        }

        // Check if the requested time is after next available time
        return dateTime >= nextAvailableTime
        }
    fun getFormattedNextAvailableTime(): String {
        if (nextAvailableTime <= 0) return "Not set"
        val sdf = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(nextAvailableTime))
    }
}


@Parcelize
data class WorkingHours(
    var startHour: Int = 9,
    var endHour: Int = 17
): Parcelable {
    constructor() : this(9,17)
}

@Parcelize
data class Order(
    var id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val items: List<OrderItem> = emptyList(),
    val subtotal: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val total: Double = 0.0,
    val status: String = "pending", // pending, processing, shipped, delivered, cancelled
    val orderDate: Long = System.currentTimeMillis(),
    val deliveryAddress: String = "",
    val paymentMethod: String = "",
    val notes: String = ""
) : Parcelable {

    fun getFormattedTotal(): String {
        return "Rs. ${String.format("%.2f", total)}"
    }


    fun getItemCount(): Int = items.sumOf { it.quantity }
}

@Parcelize
data class OrderItem(
    val productId: String = "",
    val productName: String = "",
    val quantity: Int = 0,
    val price: Double = 0.0,
    val imageUrl: String = ""
) : Parcelable {

    fun getSubtotal(): Double = quantity * price

}

@Parcelize
data class TrainerBooking(
    var id: String = "",
    val trainerId: String = "",
    val trainerName: String = "",
    val userId: String = "",
    val userName: String = "",
    val date: String = "",
    val time: String = "",
    val duration: Int = 60, // minutes
    val status: String = "pending", // pending, confirmed, completed, cancelled
    val notes: String = "",
    val totalAmount: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable {

}

data class AdminNotification(
    var id: String = "",
    val title: String = "",
    val message: String = "",
    val category: String = "", // ORDER, USER, PRODUCT, SYSTEM, ADMIN
    val timestamp: Long = 0,
    var isRead: Boolean = false,
    val relatedId: String = "", // ID of related order, user, product, etc.
    val iconType: String = "default", // notification, order, user, product
    val priority: String = "normal", // low, normal, high
    val sentBy: String = "System"
) : Serializable {

    fun getFormattedTime(): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60000 -> "Just now"
            diff < 3600000 -> "${diff / 60000} min ago"
            diff < 86400000 -> "${diff / 3600000} hours ago"
            diff < 604800000 -> "${diff / 86400000} days ago"
            else -> {
                val sdf = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
                sdf.format(java.util.Date(timestamp))
            }
        }
    }

    fun getCategoryColor(): Int {
        return when (category) {
            "ORDER" -> android.R.color.holo_green_light
            "USER" -> android.R.color.holo_blue_light
            "PRODUCT" -> android.R.color.holo_orange_light
            "SYSTEM" -> android.R.color.holo_red_light
            else -> android.R.color.darker_gray
        }
    }
}

data class Notification(
    var id: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "general", // general, order, promotion, system
    val targetUsers: List<String> = emptyList(), // empty means all users
    val isScheduled: Boolean = false,
    val scheduledTime: Long = 0L,
    val sentAt: Long = 0L,
    val status: String = "draft", // draft, sent, scheduled
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis()
) : Serializable

data class DashboardStats(
    val totalProducts: Int = 0,
    val totalUsers: Int = 0,
    val totalOrders: Int = 0,
    val totalTrainers: Int = 0,
    val totalRevenue: Double = 0.0,
    val pendingOrders: Int = 0,
    val activeUsers: Int = 0,
    val lowStockProducts: Int = 0
) {

    fun getFormattedRevenue(): String {
        return "Rs. ${String.format("%.2f", totalRevenue)}"
    }
}

data class DashboardItem(
    val title: String,
    val iconResId: Int,
    val activityClass: Class<*>
)

@Parcelize
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val status: String = "Active", // e.g., "Active", "Inactive"
    val totalOrders: Int = 0,
    val totalSpent: Double = 0.0,
    val joinDate: String = "",
    val lastLogin: String = "",
    val isVerified: Boolean = false // For the verified badge
) : Parcelable



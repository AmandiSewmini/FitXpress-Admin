package com.example.admin.fitxpress.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.admin.fitxpress.R
import com.example.admin.fitxpress.models.User

class UsersAdapter(
    private val users: MutableList<User>,
    private val onUserClick: (User) -> Unit,
    private val onViewOrdersClick: (User) -> Unit,
    private val onDeleteClick: (User) -> Unit,
    private val onStatusChange: (User, Boolean) -> Unit
) : RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userAvatarImageView: ImageView = itemView.findViewById(R.id.userAvatarImageView)
        val userNameTextView: TextView = itemView.findViewById(R.id.userNameTextView)
        val userEmailTextView: TextView = itemView.findViewById(R.id.userEmailTextView)
        val userPhoneTextView: TextView = itemView.findViewById(R.id.userPhoneTextView)
        val statusBadge: TextView = itemView.findViewById(R.id.statusBadge)
        val statusSwitch: Switch = itemView.findViewById(R.id.statusSwitch)
        val totalOrdersTextView: TextView = itemView.findViewById(R.id.totalOrdersTextView)
        val totalSpentTextView: TextView = itemView.findViewById(R.id.totalSpentTextView)
        val joinDateTextView: TextView = itemView.findViewById(R.id.joinDateTextView)
        val lastLoginTextView: TextView = itemView.findViewById(R.id.lastLoginTextView)
        val viewOrdersButton: Button = itemView.findViewById(R.id.viewOrdersButton)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]

        // Dummy image for now, replace with actual image loading library like Glide/Picasso
        holder.userAvatarImageView.setImageResource(R.drawable.placeholder_user)
        holder.userNameTextView.text = user.name
        holder.userEmailTextView.text = user.email
        holder.userPhoneTextView.text = user.phone
        holder.totalOrdersTextView.text = user.totalOrders.toString()
        holder.totalSpentTextView.text = "Rs. ${String.format("%,.2f", user.totalSpent)}"
        holder.joinDateTextView.text = "${holder.itemView.context.getString(R.string.joined)}: ${user.joinDate}"
        holder.lastLoginTextView.text = "${holder.itemView.context.getString(R.string.last_login)}: ${user.lastLogin}"

        // Set status badge and switch
        holder.statusBadge.text = user.status
        when (user.status) {
            "Active" -> {
                holder.statusBadge.setBackgroundResource(R.drawable.status_background_green)
                holder.statusSwitch.isChecked = true
            }
            "Inactive" -> {
                holder.statusBadge.setBackgroundResource(R.drawable.status_background_red)
                holder.statusSwitch.isChecked = false
            }
            else -> {
                holder.statusBadge.setBackgroundResource(R.drawable.status_background_gray)
                holder.statusSwitch.isChecked = false
            }
        }

        holder.statusSwitch.setOnCheckedChangeListener { _, isChecked ->
            val newStatus = if (isChecked) "Active" else "Inactive"
            val updatedUser = user.copy(status = newStatus)
            users[position] = updatedUser // Update the user in the list
            onStatusChange(updatedUser, isChecked)
            notifyItemChanged(position) // Notify adapter to rebind the item
        }

        // Handle verified status (if applicable, based on your User model)
        // For now, assuming a simple text for verified status
        // If you have a separate badge for verification, you can set it here.
        // Example:
        // if (user.isVerified) {
        //     holder.userNameTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_verified, 0)
        // } else {
        //     holder.userNameTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        // }


        holder.itemView.setOnClickListener { onUserClick(user) }
        holder.viewOrdersButton.setOnClickListener { onViewOrdersClick(user) }
        holder.deleteButton.setOnClickListener { onDeleteClick(user) }
    }

    override fun getItemCount(): Int = users.size

    fun updateUsers(newUsers: List<User>) {
        users.clear()
        users.addAll(newUsers)
        notifyDataSetChanged()
    }
}

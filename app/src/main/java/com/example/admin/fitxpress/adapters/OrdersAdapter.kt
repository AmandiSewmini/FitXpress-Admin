package com.example.admin.fitxpress.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.admin.fitxpress.R
import com.example.admin.fitxpress.models.Order
import java.text.SimpleDateFormat
import java.util.*

class OrdersAdapter(
    private val orders: List<Order>,
    private val onOrderAction: (Order, String) -> Unit
) : RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    override fun getItemCount(): Int = orders.size

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val orderIdText: TextView = itemView.findViewById(R.id.orderIdText)
        private val customerNameText: TextView = itemView.findViewById(R.id.customerNameText)
        private val orderStatusText: TextView = itemView.findViewById(R.id.orderStatusText)
        private val orderDateText: TextView = itemView.findViewById(R.id.orderDateText)
        private val itemCountText: TextView = itemView.findViewById(R.id.itemCountText)
        private val orderTotalText: TextView = itemView.findViewById(R.id.orderTotalText)
        private val updateStatusButton: Button = itemView.findViewById(R.id.updateStatusButton)
        private val viewDetailsButton: Button = itemView.findViewById(R.id.viewDetailsButton)

        fun bind(order: Order) {
            // Set order information
            orderIdText.text = "Order #${order.id.take(8).uppercase()}"
            customerNameText.text = order.userName
            orderTotalText.text = order.getFormattedTotal()
            itemCountText.text = "${order.getItemCount()} items"

            // Format and set order date
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            orderDateText.text = sdf.format(Date(order.orderDate))

            // Set status
            orderStatusText.text = order.status.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }
            setStatusBackground(order.status)

            // Set click listeners
            updateStatusButton.setOnClickListener {
                onOrderAction(order, "update_status")
            }

            viewDetailsButton.setOnClickListener {
                onOrderAction(order, "view_details")
            }

            // Set item click listener for full order details
            itemView.setOnClickListener {
                onOrderAction(order, "view_details")
            }
        }

        private fun setStatusBackground(status: String) {
            when (status.lowercase()) {
                "pending" -> {
                    orderStatusText.setBackgroundResource(R.drawable.status_background_orange)
                }
                "processing" -> {
                    orderStatusText.setBackgroundResource(R.drawable.status_background_blue)
                }
                "shipped" -> {
                    orderStatusText.setBackgroundResource(R.drawable.status_background_purple)
                }
                "delivered" -> {
                    orderStatusText.setBackgroundResource(R.drawable.status_background_green)
                }
                "cancelled" -> {
                    orderStatusText.setBackgroundResource(R.drawable.status_background_red)
                }
                else -> {
                    orderStatusText.setBackgroundResource(R.drawable.status_background_gray)
                }
            }
        }
    }
}

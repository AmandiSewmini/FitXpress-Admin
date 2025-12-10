package com.example.admin.fitxpress.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.admin.fitxpress.R
import com.example.admin.fitxpress.models.DashboardItem

class DashboardAdapter(
    private val items: List<DashboardItem>,
    private val onItemClick: (DashboardItem) -> Unit
) : RecyclerView.Adapter<DashboardAdapter.DashboardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DashboardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dashboard, parent, false)
        return DashboardViewHolder(view)
    }

    override fun onBindViewHolder(holder: DashboardViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class DashboardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconImageView: ImageView? = itemView.findViewById(R.id.iconImageView)
        private val titleTextView: TextView? = itemView.findViewById(R.id.titleTextView)

        fun bind(item: DashboardItem) {
            iconImageView?.setImageResource(item.iconResId)
            titleTextView?.text = item.title

            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}

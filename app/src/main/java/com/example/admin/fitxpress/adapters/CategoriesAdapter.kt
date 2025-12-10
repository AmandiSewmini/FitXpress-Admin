package com.example.admin.fitxpress.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.admin.fitxpress.R
import com.example.admin.fitxpress.models.Category

class CategoriesAdapter(
    private val categories: MutableList<Category>,
    private val onEditClick: (Category) -> Unit,
    private val onDeleteClick: (Category) -> Unit,
    private val onStatusToggle: (Category) -> Unit
) : RecyclerView.Adapter<CategoriesAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int = categories.size

    fun updateCategories(newCategories: List<Category>) {
        categories.clear()
        categories.addAll(newCategories)
        notifyDataSetChanged()
    }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryImageView: ImageView = itemView.findViewById(R.id.categoryImageView) // item_category.xml හි ImageView එකට අදාළව
        private val categoryNameTextView: TextView = itemView.findViewById(R.id.categoryNameTextView)
        private val categoryDescriptionTextView: TextView = itemView.findViewById(R.id.categoryDescriptionTextView)
        private val productCountTextView: TextView = itemView.findViewById(R.id.productCountTextView)
        private val statusSwitch: Switch = itemView.findViewById(R.id.statusSwitch)
        private val editButton: Button = itemView.findViewById(R.id.editButton)
        private val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
        private val context: Context = itemView.context

        fun bind(category: Category) {
            categoryNameTextView.text = category.name
            categoryDescriptionTextView.text = category.description
            productCountTextView.text = "${category.productCount} Products"

            if (category.imageUrl.isNotEmpty()) {
                val imageResId = context.resources.getIdentifier(
                    category.imageUrl,
                    "drawable",
                    context.packageName
                )
                if (imageResId != 0) {
                    categoryImageView.setImageResource(imageResId)
                } else {
                    // Log an error if the resource name is invalid but not empty
                    Log.e("CategoriesAdapter", "Drawable resource not found for name: ${category.imageUrl}")
                    categoryImageView.setImageResource(R.drawable.placeholder_category) // Fallback
                }
            } else {
                categoryImageView.setImageResource(R.drawable.placeholder_category) // Default placeholder
            }

            statusSwitch.setOnCheckedChangeListener(null)
            statusSwitch.isChecked = category.isActive
            statusSwitch.setOnCheckedChangeListener { _, isChecked ->
                // Only trigger if the state actually changed by user interaction
                if (isChecked != category.isActive) {
                    onStatusToggle(category.copy(isActive = isChecked))
                }
            }

            editButton.setOnClickListener { onEditClick(category) }
            deleteButton.setOnClickListener { onDeleteClick(category) }
        }
    }
}

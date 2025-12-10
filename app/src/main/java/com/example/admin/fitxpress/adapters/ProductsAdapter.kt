package com.example.admin.fitxpress.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.admin.fitxpress.R
import com.example.admin.fitxpress.models.Product

// ProductsActivity will provide the actual code to run when the edit button, delete button,
// or status switch is interacted with for a specific product

class ProductsAdapter(
    private val products: MutableList<Product>, //The list of products to display
    private val onEditClick: (Product) -> Unit, //These are higher-order functions (callbacks)
    private val onDeleteClick: (Product) -> Unit,
    private val onStatusToggle: (Product) -> Unit
) : RecyclerView.Adapter<ProductsAdapter.ProductViewHolder>() {

    //Creates and returns a ProductViewHolder instance
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    //Called by the RecyclerView to display the data at the specified position.
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    //Returns the total number of products in the list
    override fun getItemCount(): Int = products.size

    //clears the old list, adds the new ones
    fun submitList(newProducts: List<Product>) {
        products.clear()
        products.addAll(newProducts)
        notifyDataSetChanged() // Consider DiffUtil for better performance later
    }

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productImageView: ImageView = itemView.findViewById(R.id.productImageView)
        private val productNameTextView: TextView = itemView.findViewById(R.id.productNameTextView)
        private val productCategoryTextView: TextView = itemView.findViewById(R.id.productCategoryTextView)
        private val productPriceTextView: TextView = itemView.findViewById(R.id.productPriceTextView)
        private val statusSwitch: Switch = itemView.findViewById(R.id.statusSwitch)
        private val productStockTextView: TextView = itemView.findViewById(R.id.productStockTextView)
        private val stockStatusTextView: TextView = itemView.findViewById(R.id.stockStatusTextView)
        private val lowStockIndicator: View = itemView.findViewById(R.id.lowStockIndicator)
        private val editButton: Button = itemView.findViewById(R.id.editButton)
        private val deleteButton: Button = itemView.findViewById(R.id.deleteButton)

        //It takes a Product object and sets the data to the corresponding views
        fun bind(product: Product) {
            productNameTextView.text = product.name
            productCategoryTextView.text = product.categoryName
            productPriceTextView.text = itemView.context.getString(R.string.product_price_format, product.price)
            productStockTextView.text = product.stock.toString()

            // Status Switch
            statusSwitch.setOnCheckedChangeListener(null)
            statusSwitch.isChecked = product.isActive
            statusSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked != product.isActive) {
                    onStatusToggle(product.copy(isActive = isChecked))
                }
            }

            // Stock Status and Indicator
            val context = itemView.context
            if (product.stock <= 10 && product.isActive) {
                lowStockIndicator.visibility = View.VISIBLE
                stockStatusTextView.text = context.getString(R.string.stock_status_low)
                stockStatusTextView.setBackgroundResource(R.drawable.stock_status_low)
                stockStatusTextView.setTextColor(itemView.context.resources.getColor(R.color.yellow_warning))
            } else if (product.stock > 0 && product.isActive) {
                lowStockIndicator.visibility = View.GONE
                stockStatusTextView.text = context.getString(R.string.stock_status_in)
                stockStatusTextView.setBackgroundResource(R.drawable.stock_status_in)
                stockStatusTextView.setTextColor(itemView.context.resources.getColor(R.color.green_success))
            } else {
                lowStockIndicator.visibility = View.GONE
                stockStatusTextView.text = context.getString(R.string.stock_status_out)
                stockStatusTextView.setBackgroundResource(R.drawable.stock_status_out)
                stockStatusTextView.setTextColor(itemView.context.resources.getColor(R.color.error_red))
            }
            // --- IMAGE HANDLING ---
            // Load image using Glide or any other image loading library
            if (product.imageDrawableId != null && product.imageDrawableId != 0) { // Check for valid drawable ID
                Glide.with(itemView.context)
                    .load(product.imageDrawableId)
                    .placeholder(R.drawable.placeholder_product) // Your placeholder
                    .error(R.drawable.placeholder_product) // Fallback to placeholder on error
                    .into(productImageView)
            } else if (!product.imageUrl.isNullOrEmpty()) {
                // Fallback to URL if drawableId is not set but imageUrl is
                Glide.with(itemView.context)
                    .load(product.imageUrl)
                    .placeholder(R.drawable.placeholder_product)
                    .error(R.drawable.placeholder_product)
                    .into(productImageView)
            } else {
                // If neither is available, show placeholder
                productImageView.setImageResource(R.drawable.placeholder_product)
            }

            // Action Buttons
            editButton.setOnClickListener { onEditClick(product) }
            deleteButton.setOnClickListener { onDeleteClick(product) }
        }
    }
}

package com.example.admin.fitxpress

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.admin.fitxpress.adapters.ProductsAdapter
import com.example.admin.fitxpress.models.Product
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlin.text.contains
import kotlin.text.toLowerCase


class ProductsActivity : AppCompatActivity() {
 private lateinit var productsRecyclerView: RecyclerView //To display the list of products.
    private lateinit var addProductFab: FloatingActionButton
    private lateinit var searchEditText: EditText
    private lateinit var backButton: ImageView

    private lateinit var progressBar: ProgressBar
    private lateinit var emptyStateTextView: TextView

    private val originalProductsList = mutableListOf<Product>()// Holds the complete list of products fetched from Firestore.
    private val filteredProductsList = mutableListOf<Product>()//Holds the products currently displayed in the RecyclerView (after filtering by search).
    private lateinit var productsAdapter: ProductsAdapter

    private lateinit var db: FirebaseFirestore
    private var productsListener: ListenerRegistration? = null // For real-time updates

    companion object {
        private const val TAG = "ProductsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_products)

        db = FirebaseFirestore.getInstance()

         setupViews()
        setupRecyclerView()
    }

    //Finds all the UI elements from the XML.
    private fun setupViews() {
        productsRecyclerView = findViewById(R.id.productsRecyclerView)
        addProductFab = findViewById(R.id.addProductFab)
        searchEditText = findViewById(R.id.searchEditText)
        backButton = findViewById(R.id.backButton)
        progressBar = findViewById(R.id.productsProgressBar)
        emptyStateTextView = findViewById(R.id.emptyStateTextView)

        //Opens Add/Edit screen.
        addProductFab.setOnClickListener {
            val intent = Intent(this, AddEditProductActivity::class.java)
            startActivity(intent)
        }

        backButton.setOnClickListener {
            finish()
        }

        searchEditText.addTextChangedListener { text ->
            filterProducts(text.toString())
        }
    }

    //Sets up the RecyclerView to display products.
    private fun setupRecyclerView() {
        productsRecyclerView.layoutManager = LinearLayoutManager(this)
        productsAdapter = ProductsAdapter(
            products = filteredProductsList,
            onEditClick = { product ->
                editProduct(product)
            },
            onDeleteClick = { product ->
                showDeleteConfirmationDialog(product)
            },
            onStatusToggle = { product ->
                toggleProductStatusInFirestore(product)
            }
        )
        productsRecyclerView.adapter = productsAdapter
    }

    //Connects to Firestore products collection.
    private fun loadProductsFromFirestore() {
        progressBar.visibility = View.VISIBLE
        emptyStateTextView.visibility = View.GONE
        productsRecyclerView.visibility = View.GONE

        // Detach any existing listener to avoid multiple listeners
        productsListener?.remove()

        // Listen for real-time updates
        productsListener = db.collection("products")
            .orderBy("name", Query.Direction.ASCENDING) //Sort products by name (A–Z).
            //It listens in real time
            .addSnapshotListener { snapshots, e ->
                progressBar.visibility = View.GONE
                if (e != null) { //If there's an error (e != null)
                    Log.w(TAG, "Listen failed.", e)
                    emptyStateTextView.text = "Error loading products: ${e.message}"
                    emptyStateTextView.visibility = View.VISIBLE
                    productsRecyclerView.visibility = View.GONE
                    return@addSnapshotListener
                }
                if (snapshots != null && !snapshots.isEmpty) {
                    originalProductsList.clear()
                    for (document in snapshots.documents) {
                        val product = document.toObject(Product::class.java)
                        if (product != null) {
                            product.id = document.id // Assign Firestore document ID to our model
                            originalProductsList.add(product)
                        }
                    }
                    Log.d(TAG, "Products loaded: ${originalProductsList.size}")
                    filterProducts(searchEditText.text.toString()) // Apply current search query
                } else {
                    Log.d(TAG, "No products found in Firestore.")
                    originalProductsList.clear() // Clear list if no products
                    filterProducts("") // Clear filtered list too
                }
                updateUiBasedOnData()
            }
    }

    //Filters products by name, description, or category.
    private fun filterProducts(query: String) {
        filteredProductsList.clear()
         val lowerCaseQuery = query.toLowerCase().trim()

        if (lowerCaseQuery.isEmpty()) {
            filteredProductsList.addAll(originalProductsList)
        } else {
            val results = originalProductsList.filter { product ->
                 product.name.toLowerCase().contains(lowerCaseQuery) ||
                        product.description.toLowerCase().contains(lowerCaseQuery) ||
                        product.categoryName.toLowerCase().contains(lowerCaseQuery)
            }
            filteredProductsList.addAll(results)
        }
        productsAdapter.notifyDataSetChanged()
        updateUiBasedOnData()
    }

    private fun updateUiBasedOnData() {

        if (progressBar.visibility == View.VISIBLE) return // Still loading

        if (originalProductsList.isEmpty() && searchEditText.text.toString().isEmpty()) {
            emptyStateTextView.text = "No products yet. Add one!"
            emptyStateTextView.visibility = View.VISIBLE
            productsRecyclerView.visibility = View.GONE
        } else if (filteredProductsList.isEmpty() && searchEditText.text.toString().isNotEmpty()) {
            emptyStateTextView.text = "No products match your search."
            emptyStateTextView.visibility = View.VISIBLE
            productsRecyclerView.visibility = View.GONE
        } else if (filteredProductsList.isEmpty() && originalProductsList.isNotEmpty()){
            emptyStateTextView.text = "No products found (unexpected state)." // Should not happen often
            emptyStateTextView.visibility = View.VISIBLE
            productsRecyclerView.visibility = View.GONE
        }
        else {
            emptyStateTextView.visibility = View.GONE
            productsRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun editProduct(product: Product) {
        val intent = Intent(this, AddEditProductActivity::class.java)
        // Assuming AddEditProductActivity.EXTRA_PRODUCT is accessible here
        // If not, just ensure the string literal matches EXACTLY: "PRODUCT_EXTRA"
        intent.putExtra(AddEditProductActivity.EXTRA_PRODUCT, product)
        startActivity(intent)
    }

    private fun showDeleteConfirmationDialog(product: Product) {
        AlertDialog.Builder(this)
            .setTitle("Delete Product")
            .setMessage("Are you sure you want to delete ${product.name}?")
            .setPositiveButton("Delete") { _, _ ->
                deleteProductFromFirestore(product) // This will now show a toast
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteProductFromFirestore(product: Product) {
        if (product.id.isEmpty()) {
            Toast.makeText(this, "Product ID is missing, cannot delete.", Toast.LENGTH_SHORT).show()
            return
        }
        progressBar.visibility = View.VISIBLE
        db.collection("products").document(product.id)
            .delete()
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                Log.d(TAG, "Product '${product.name}' successfully deleted!")
                Toast.makeText(this, "Product '${product.name}' deleted.", Toast.LENGTH_SHORT).show()
                // The listener will automatically update the list.
                // If not using a listener, you'd manually remove from originalProductsList and re-filter.
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Log.w(TAG, "Error deleting product '${product.name}'", e)
                Toast.makeText(this, "Failed to delete product: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    //Updates the product's isActive status (on/off).
    private fun toggleProductStatusInFirestore(productWithToggledStatus: Product) {
        if (productWithToggledStatus.id.isEmpty()) {
            Toast.makeText(this, "Product ID is missing, cannot update status.", Toast.LENGTH_SHORT).show()
            // Optionally revert the switch in the UI if you have direct access to it or refresh list
            return
        }
        progressBar.visibility = View.VISIBLE
        // The product object passed from adapter already has the new 'isActive' status
        db.collection("products").document(productWithToggledStatus.id)
            .update("isActive", productWithToggledStatus.isActive)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                val statusText = if (productWithToggledStatus.isActive) "activated" else "deactivated"
                Log.d(TAG, "Product '${productWithToggledStatus.name}' status successfully $statusText!")
                Toast.makeText(this, "Product '${productWithToggledStatus.name}' $statusText.", Toast.LENGTH_SHORT).show()
                // Listener will update the UI.
                // If not using listener, find and update in originalProductsList and re-filter.
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Log.w(TAG, "Error updating status for product '${productWithToggledStatus.name}'", e)
                Toast.makeText(this, "Failed to update status: ${e.message}", Toast.LENGTH_SHORT).show()
                // Important: If update fails, you might want to refresh the list or
                // revert the switch in the adapter to reflect the actual DB state.
                // For simplicity here, we rely on the next Firestore snapshot if using listener,
                // or you could force a refresh.
            }
    }

    override fun onStart() {
        super.onStart()
        loadProductsFromFirestore() // Load/listen for products when activity starts/resumes
    }

    override fun onStop() {
        super.onStop()
        productsListener?.remove() // Stop listening when activity is not visible
    }
}

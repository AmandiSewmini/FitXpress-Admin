
package com.example.admin.fitxpress

import android.app.Activity
// import android.content.ContentValues.TAG // Use class level TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
// import androidx.core.util.remove // Not used here
import androidx.core.widget.addTextChangedListener
// import androidx.glance.visibility // Not used here, use android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.admin.fitxpress.adapters.CategoriesAdapter
import com.example.admin.fitxpress.models.Category
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
// import com.google.firebase.firestore.firestore // Redundant with Firebase.firestore ktx
// kotlin.text.* are generally implicitly available

import com.google.firebase.firestore.ktx.firestore // Correct ktx import
import com.google.firebase.firestore.ktx.toObject // Correct ktx import for toObject
import com.google.firebase.ktx.Firebase // Correct ktx import
import com.google.firebase.firestore.DocumentChange // CORRECT IMPORT
import com.google.firebase.firestore.ListenerRegistration // CORRECT IMPORT
// import kotlin.collections.sortBy // Implicitly available
// import kotlin.text.isEmpty // Implicitly available
// import kotlin.text.trim // Implicitly available

class CategoriesActivity : AppCompatActivity() {

    private val TAG = "CategoriesActivity" // Class level TAG

    private lateinit var db: FirebaseFirestore
    private lateinit var categoriesRecyclerView: RecyclerView
    private lateinit var categoriesAdapter: CategoriesAdapter
    private lateinit var addCategoryFab: FloatingActionButton
    private lateinit var searchEditText: EditText
    private lateinit var backButton: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyStateLayout: LinearLayout

    private val originalCategoriesList = mutableListOf<Category>()
    private var categoryListener: ListenerRegistration? = null // Declare categoryListener

    // Activity Result Launcher for Add/Edit
    private val addEditCategoryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "Category list updated (or change submitted).", Toast.LENGTH_SHORT).show()
            // Data will refresh via snapshot listener
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)

        db = Firebase.firestore

        setupViews()
        setupRecyclerView()
        // loadCategoriesFromFirestore() // Called in onStart
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart called, attaching Firestore listener.")
        loadCategoriesFromFirestore()
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop called, removing Firestore listener.")
        categoryListener?.remove()
    }

    private fun setupViews() {
        categoriesRecyclerView = findViewById(R.id.categoriesRecyclerView)
        addCategoryFab = findViewById(R.id.addCategoryFab)
        searchEditText = findViewById(R.id.searchEditText)
        backButton = findViewById(R.id.backButton)
        progressBar = findViewById(R.id.progressBar)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)

        addCategoryFab.setOnClickListener {
            val intent = Intent(this, AddEditCategoryActivity::class.java)
            addEditCategoryLauncher.launch(intent)
        }

        backButton.setOnClickListener {
            finish()
        }

        searchEditText.addTextChangedListener { text ->
            filterCategories(text.toString().trim())
        }
    }

    private fun setupRecyclerView() {
        categoriesAdapter = CategoriesAdapter(
            categories = mutableListOf(), // Start with empty, will be updated
            onEditClick = { category ->
                editCategory(category)
            },
            onDeleteClick = { category ->
                showDeleteConfirmationDialog(category)
            },
            onStatusToggle = { category ->
                toggleCategoryStatusInFirestore(category)
            }
        )
        categoriesRecyclerView.layoutManager = LinearLayoutManager(this)
        categoriesRecyclerView.adapter = categoriesAdapter
    } // REMOVED EXTRA BRACE that was here

    private fun loadCategoriesFromFirestore() {
        Log.d(TAG, "Attempting to load categories from Firestore.")
        progressBar.visibility = View.VISIBLE
        emptyStateLayout.visibility = View.GONE
        categoriesRecyclerView.visibility = View.GONE

        categoryListener?.remove() // Remove any existing listener

        categoryListener = db.collection("categories")
            .orderBy("name", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                progressBar.visibility = View.GONE

                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    Toast.makeText(this, "Error loading categories: ${e.message}", Toast.LENGTH_LONG).show()
                    updateUiBasedOnOriginalListContent()
                    return@addSnapshotListener
                }

                if (snapshots == null) {
                    Log.w(TAG, "Snapshot is null, no data to process.")
                    updateUiBasedOnOriginalListContent()
                    return@addSnapshotListener
                }

                Log.d(TAG, "Categories snapshot received. Number of document changes: ${snapshots.documentChanges.size}")

                var listModified = false
                for (change in snapshots.documentChanges) {
                    val docId = change.document.id
                    val rawData = change.document.data
                    Log.d(TAG, "Doc ID: $docId, Change Type: ${change.type}, Raw Data: $rawData")

                    if (rawData.containsKey("isActive")) {
                        Log.d(TAG, "  'isActive' field EXISTS in raw data. Value: ${rawData["isActive"]}, Type: ${rawData["isActive"]?.javaClass?.simpleName}")
                    } else if (rawData.containsKey("active")) {
                        Log.d(TAG, "  'active' (lowercase) field EXISTS in raw data. Value: ${rawData["active"]}, Type: ${rawData["active"]?.javaClass?.simpleName}")
                    } else {
                        Log.d(TAG, "  'isActive' (and 'active') field NOT FOUND in raw data for doc ID: $docId")
                    }
                    if (rawData.containsKey("stability")) {
                        Log.d(TAG, "  'stability' field EXISTS in raw data. Value: ${rawData["stability"]}")
                    }

                    try {
                        val category = change.document.toObject<Category>()
                        Log.d(TAG, "  Successfully deserialized to Category: ID=${category.id}, Name=${category.name}, isActive=${category.isActive}")

                        val index = originalCategoriesList.indexOfFirst { it.id == category.id }

                        when (change.type) {
                            DocumentChange.Type.ADDED -> {
                                if (index == -1) {
                                    originalCategoriesList.add(category)
                                    listModified = true
                                    Log.d(TAG, "  ADDED: ${category.name} to originalCategoriesList")
                                } else {
                                    Log.d(TAG, "  Treating ADDED as MODIFIED (already exists): ${category.name}")
                                    originalCategoriesList[index] = category
                                    listModified = true
                                }
                            }
                            DocumentChange.Type.MODIFIED -> {
                                if (index != -1) {
                                    originalCategoriesList[index] = category
                                    listModified = true
                                    Log.d(TAG, "  MODIFIED: ${category.name} in originalCategoriesList")
                                } else {
                                    originalCategoriesList.add(category) // Should ideally not happen if ADDED is handled
                                    listModified = true
                                    Log.d(TAG, "  MODIFIED (but was not in list, adding): ${category.name}")
                                }
                            }
                            DocumentChange.Type.REMOVED -> {
                                if (index != -1) {
                                    originalCategoriesList.removeAt(index)
                                    listModified = true
                                    Log.d(TAG, "  REMOVED: ${category.name} from originalCategoriesList")
                                }
                            }
                        }
                    } catch (ex: Exception) {
                        Log.e(TAG, "Error converting document ${change.document.id} to Category", ex)
                    }
                }

                if (listModified || snapshots.documentChanges.isNotEmpty()) {
                    originalCategoriesList.sortBy { it.name.lowercase() }
                    Log.d(TAG, "Original list size after processing changes: ${originalCategoriesList.size}. List: $originalCategoriesList")
                    filterCategories(searchEditText.text.toString().trim()) // This will also update UI
                } else if (originalCategoriesList.isEmpty() && snapshots.isEmpty) {
                    Log.d(TAG, "No document changes, original list and snapshot are empty.")
                    filterCategories(searchEditText.text.toString().trim()) // Update UI to show empty state
                } else {
                    Log.d(TAG, "No significant list modifications, UI should be up to date or snapshot was empty but list wasn't.")
                    // If the list was not modified but was not empty, filter might still be needed if search text changed
                    // However, filterCategories is called by text changed listener anyway.
                    // Call updateUi to ensure correct state if nothing else triggers filter
                    updateUiBasedOnOriginalListContent()
                }
            }
    }

    private fun filterCategories(query: String) {
        val searchQuery = query.lowercase().trim()
        Log.d(TAG, "Filtering categories with query: '$searchQuery'")
        val currentFilteredList = mutableListOf<Category>()

        if (searchQuery.isEmpty()) {
            currentFilteredList.addAll(originalCategoriesList)
            Log.d(TAG, "Search query is empty, filtered list matches original. Size: ${currentFilteredList.size}")
        } else {
            currentFilteredList.addAll(
                originalCategoriesList.filter { category ->
                    (category.name.lowercase().contains(searchQuery) ||
                            category.description.lowercase().contains(searchQuery))
                }
            )
            Log.d(TAG, "Search query is '$searchQuery', filtered list size: ${currentFilteredList.size}")
        }
        categoriesAdapter.updateCategories(currentFilteredList)

        if (currentFilteredList.isEmpty()) {
            Log.d(TAG, "After filtering, list for adapter is empty. Showing empty state.")
            emptyStateLayout.visibility = View.VISIBLE
            categoriesRecyclerView.visibility = View.GONE
        } else {
            Log.d(TAG, "After filtering, list for adapter is NOT empty. Showing RecyclerView.")
            emptyStateLayout.visibility = View.GONE
            categoriesRecyclerView.visibility = View.VISIBLE
        }
    }

    // Renamed from updateEmptyStateBasedOnOriginalList for clarity on its primary role
    private fun updateUiBasedOnOriginalListContent() {
        // This function is generally for the case where Firestore returns,
        // but no specific changes are processed that would trigger filterCategories,
        // or to set initial state before filterCategories runs after a full load.
        if (originalCategoriesList.isEmpty()) {
            Log.d(TAG, "Original categories list is empty. Ensuring empty state is shown.")
            emptyStateLayout.visibility = View.VISIBLE
            categoriesRecyclerView.visibility = View.GONE
            categoriesAdapter.updateCategories(emptyList()) // Ensure adapter also has empty list
        } else {
            Log.d(TAG, "Original categories list is NOT empty. Ensuring RecyclerView is visible.")
            // Visibility is primarily handled by filterCategories, but this can be a fallback
            // or initial setup if filterCategories hasn't run yet with the new original list.
            emptyStateLayout.visibility = View.GONE
            categoriesRecyclerView.visibility = View.VISIBLE
            // It's better to let filterCategories update the adapter
        }
        // If search text is present, filterCategories should be the one to update the adapter and visibility.
        // This function is more like a safeguard for initial load or error cases.
    }

    private fun editCategory(category: Category) {
        val intent = Intent(this, AddEditCategoryActivity::class.java)
        intent.putExtra("category", category)
        addEditCategoryLauncher.launch(intent)
    }

    private fun showDeleteConfirmationDialog(category: Category) {
        AlertDialog.Builder(this)
            .setTitle("Delete Category")
            .setMessage("Are you sure you want to delete '${category.name}'? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteCategoryFromFirestore(category)
            }
            .setNegativeButton("Cancel", null)
            .setIcon(android.R.drawable.ic_dialog_alert) // Standard Android icon, REPLACE if you have a custom one
            .show()
    }

    private fun deleteCategoryFromFirestore(category: Category) {
        if (category.id.isEmpty()) {
            Log.w(TAG, "Attempted to delete category with empty ID.")
            Toast.makeText(this, "Cannot delete category: Missing ID.", Toast.LENGTH_SHORT).show()
            return
        }
        Log.d(TAG, "Attempting to delete category: ${category.id} - ${category.name}")
        progressBar.visibility = View.VISIBLE
        db.collection("categories").document(category.id)
            .delete()
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                Log.i(TAG, "Category '${category.name}' deleted successfully from Firestore.")
                Toast.makeText(this, "'${category.name}' deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Log.e(TAG, "Error deleting category ${category.id} from Firestore", e)
                Toast.makeText(this, "Error deleting category: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun toggleCategoryStatusInFirestore(categoryToUpdate: Category) {
        if (categoryToUpdate.id.isEmpty()) {
            Log.w(TAG, "Attempted to toggle status for category with empty ID.")
            Toast.makeText(this, "Cannot update category: Missing ID.", Toast.LENGTH_SHORT).show()
            return
        }
        val newStatus = categoryToUpdate.isActive
        val statusText = if (newStatus) "activated" else "deactivated"
        Log.d(TAG, "Attempting to toggle status for category: ${categoryToUpdate.id} to $statusText (isActive: $newStatus)")

        progressBar.visibility = View.VISIBLE
        db.collection("categories").document(categoryToUpdate.id)
            .update(mapOf(
                "isActive" to newStatus,
                "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            ))
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                Log.i(TAG, "Category '${categoryToUpdate.name}' status successfully updated to $statusText in Firestore.")
                Toast.makeText(this, "'${categoryToUpdate.name}' status $statusText.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Log.e(TAG, "Error updating status for ${categoryToUpdate.id} in Firestore", e)
                Toast.makeText(this, "Error updating status: ${e.message}", Toast.LENGTH_SHORT).show()
                // Revert UI or re-fetch (snapshot listener should eventually get correct state)
            }
    }
}

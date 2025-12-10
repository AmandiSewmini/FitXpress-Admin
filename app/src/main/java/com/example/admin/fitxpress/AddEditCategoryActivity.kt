package com.example.admin.fitxpress

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.admin.fitxpress.models.Category
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import java.util.*

class AddEditCategoryActivity : AppCompatActivity() {


    private lateinit var nameEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var imageResourceNameEditText: EditText
    private lateinit var statusSwitch: Switch
    private lateinit var saveButton: Button
    private lateinit var backButton: ImageView
    private lateinit var activityTitle: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var categoryPreviewImageView: ImageView

    private lateinit var db: FirebaseFirestore
    private var currentCategory: Category? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_category)

        db = Firebase.firestore
        currentCategory = intent.getParcelableExtra("category") // No 'as? Category' needed if using modern getParcelableExtra

        setupViews()
        if (currentCategory != null) {
            populateFields()
            activityTitle.text = getString(R.string.edit_category)
        } else {
            activityTitle.text = getString(R.string.add_category)
            // Set a default placeholder for new category preview
            categoryPreviewImageView.setImageResource(R.drawable.placeholder_category)
        }

    }


    private fun setupViews() {
        nameEditText = findViewById(R.id.nameEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        imageResourceNameEditText = findViewById(R.id.imageResourceNameEditText)
        statusSwitch = findViewById(R.id.statusSwitch)
        saveButton = findViewById(R.id.saveButton)
        backButton = findViewById(R.id.backButton)
        activityTitle = findViewById(R.id.activityTitle)
        progressBar = findViewById(R.id.progressBar)
        categoryPreviewImageView = findViewById(R.id.categoryImageView)

        imageResourceNameEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                previewEnteredImage(s.toString().trim())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        saveButton.setOnClickListener {
            saveCategory()
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun previewEnteredImage(resourceName: String) {
        if (resourceName.isNotEmpty()) {
            val imageResId = resources.getIdentifier(
                resourceName,
                "drawable",
                packageName
            )
            if (imageResId != 0) {
                categoryPreviewImageView.setImageResource(imageResId)
            } else {
                categoryPreviewImageView.setImageResource(R.drawable.placeholder_category) // Fallback if name is invalid
            }
        } else {
            categoryPreviewImageView.setImageResource(R.drawable.placeholder_category)
        }
    }

    private fun populateFields() {
        currentCategory?.let { category ->
            nameEditText.setText(category.name)
            descriptionEditText.setText(category.description)
            imageResourceNameEditText.setText(category.imageUrl)
            statusSwitch.isChecked = category.isActive
            previewEnteredImage(category.imageUrl)
        }
    }

    private fun saveCategory() {
        val name = nameEditText.text.toString().trim()
        val description = descriptionEditText.text.toString().trim()
        val imageResourceName = imageResourceNameEditText.text.toString().trim()
        val isActive = statusSwitch.isChecked

        if (!validateInput(name, description, imageResourceName)) {
            return
        }
        progressBar.visibility = View.VISIBLE
        saveButton.isEnabled = false

        if (currentCategory == null) {
            // Add New Category
            val newCategory = Category(
                name = name,
                description = description,
                imageUrl = imageResourceName, // Save resource name
                isActive = isActive,
                productCount = 0 // Default product count
            )
            db.collection("categories")
                .add(newCategory)
                .addOnSuccessListener { documentReference ->
                    progressBar.visibility = View.GONE
                    saveButton.isEnabled = true
                    Toast.makeText(this, "Category '${newCategory.name}' added successfully!", Toast.LENGTH_SHORT).show()
                    // Set result OK to notify CategoriesActivity to refresh
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                .addOnFailureListener { e ->
                    progressBar.visibility = View.GONE
                    saveButton.isEnabled = true
                    Log.e("AddEditCategory", "Error adding category", e)
                    Toast.makeText(this, "Error adding category: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Update Existing Category
            val categoryToUpdate = currentCategory!!
            val updatedData = mapOf(
                "name" to name,
                "description" to description,
                "imageUrl" to imageResourceName, // Update resource name
                "isActive" to isActive,
                "updatedAt" to Date()
            )
            db.collection("categories").document(categoryToUpdate.id)
                .update(updatedData) // Only updates specified fields, @ServerTimestamp for updatedAt works well here
                .addOnSuccessListener {
                    progressBar.visibility = View.GONE
                    saveButton.isEnabled = true
                    Toast.makeText(this, "Category '${name}' updated successfully!", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                .addOnFailureListener { e ->
                    progressBar.visibility = View.GONE
                    saveButton.isEnabled = true
                    Log.e("AddEditCategory", "Error updating category", e)
                    Toast.makeText(this, "Error updating category: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

    }

    private fun validateInput(name: String, description: String,  imageResourceName: String): Boolean {
        if (name.isEmpty()) {
            nameEditText.error = "Category name is required"
            nameEditText.requestFocus()
            return false
        }
        if (description.isEmpty()) {
            descriptionEditText.error = "Description is required"
            descriptionEditText.requestFocus()
            return false
        }
        if (imageResourceName.isEmpty()) {
            // Make image resource name mandatory
            imageResourceNameEditText.error = "Image resource name is required"
            imageResourceNameEditText.requestFocus()
            return false
        } else {
            // Optional: Validate if the resource name actually exists
            val imageResId = resources.getIdentifier(imageResourceName, "drawable", packageName)
            if (imageResId == 0) {
                imageResourceNameEditText.error = "Invalid image resource name (not found in drawables)"
                imageResourceNameEditText.requestFocus()
                return false
            }
        }
        return true
    }
}

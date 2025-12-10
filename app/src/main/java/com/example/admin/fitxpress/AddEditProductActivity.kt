package com.example.admin.fitxpress

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.admin.fitxpress.models.Category
import com.example.admin.fitxpress.models.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*

class AddEditProductActivity : AppCompatActivity() {

    //Will hold references
    private var productImageView: ImageView? = null
    private var nameEditText: EditText? = null
    private var descriptionEditText: EditText? = null
    private var priceEditText: EditText? = null
    private var stockEditText: EditText? = null
    private var categorySpinner: Spinner? = null
    private var spinnerProductImage: Spinner? = null
    private var saveButton: Button? = null
    private var backButton: ImageView? = null
    private var activityTitle: TextView? = null
    private var progressBar: ProgressBar? = null

    //private lateinit var db: FirebaseFirestore: This declares a variable to hold the instance of FirebaseFirestore.
    // lateinit means it will be initialized later (in onCreate), and the compiler trusts you to do so before it's accessed.
    // It's private because it's only needed within this activity.
    private lateinit var db: FirebaseFirestore

    //(?) initially? These are typically initialized in onCreate after the layout has been inflated, using findViewById()

    private var currentProduct: Product? = null

    //A mutable list to store Category objects fetched from Firestore.
    // These are used to populate the categorySpinner.
    private val categoriesList = mutableListOf<Category>()
    //The adapter for the categorySpinner.
    // It will be initialized after the categories are loaded.
    private lateinit var categoryAdapter: ArrayAdapter<String>

    private var selectedProductImageResId: Int = R.drawable.ic_add_photo // Default placeholder

    //"General": A constant string key used to access general images in the categorizedImages map.
    private val GENERAL_IMAGES_CATEGORY_KEY = "General"
    // a pre-defined, read-only map that structures your local drawable images
    private val categorizedImages: Map<String, Map<String, Int>> = mapOf(
        "Protein Powders" to mapOf(
            "Chocolate Protein Powder" to R.drawable.chocolateproteinpowder,
            "Double Chocolate Protein Powder" to R.drawable.doublechocolateproteinpowder,
            "Taurine Powder" to R.drawable.taurinepowder,
            "Unflavoured Protein Powder" to R.drawable.unflavouredproteinpowder,
            "Vanilla Protein Powder" to R.drawable.vanillaproteinpowder
        ),
        "Energy Bars" to mapOf(
            "Banana Energy Bars" to R.drawable.bananaenergybars,
            "Dibosco Natural Energy Bar" to R.drawable.dibosconaturalenergybar,
            "Honey Flavoured Bar" to R.drawable.honeyflavouredbar,
            "Oat Bar" to R.drawable.oatbar,
            "Strawberry Flavoured Bar" to R.drawable.strawberryflavouredbar
        ),
        "Multivitamins" to mapOf(
            "Carbamide Forte Multivitamin" to R.drawable.carbamidefortemultivitamin,
            "CodeAge Multivitamin" to R.drawable.codeagemultivitamin,
            "Junior Sport Multivitamin" to R.drawable.juniorsportmultivitamin,
            "Muscle Blaze Multivitamin" to R.drawable.muscleblazemultivitamin,
            "Vita Health Multivitamin" to R.drawable.vitahealthmultivitamin
        ),
        "Fat Burners" to mapOf(
            "Men Belly Fat Burner" to R.drawable.menbellyfatburner,
            "Women Fat Burner" to R.drawable.womenfatburner
        ),
        "Accessories" to mapOf(
            "Blue Gym Towel" to R.drawable.bluegymtowel,
            "Fitness Bag" to R.drawable.fitnessbag,
            "Metal Water Bottle" to R.drawable.metalwaterbottle,
            "Pink Gym Towel" to R.drawable.pinkgymtowel,
            "Plastic Water Bottle" to R.drawable.plasticwaterbottle,
            "Premium Duffle Bag" to R.drawable.premiumdufflebag,
            "Ripple Towel" to R.drawable.rippletowel
        ),
        GENERAL_IMAGES_CATEGORY_KEY to mapOf( // For general/always available images
            "Default Placeholder" to R.drawable.ic_add_photo
            // Add any other truly generic images here if needed
        )
    )
    private lateinit var productImageAdapter: ArrayAdapter<String>
    private var currentImageNamesInSpinner = mutableListOf<String>()
    private val defaultCategoryPrompt = "-- Select Category --"

    companion object {
        private const val TAG = "AddEditProductActivity"
        const val EXTRA_PRODUCT = "product"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_product)

        //Initializes the db variable
        db = FirebaseFirestore.getInstance()

        try {
            // Get product object if passed (for editing)
            // Get product object if passed (for editing)
            currentProduct = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(EXTRA_PRODUCT, Product::class.java) // Use the constant
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(EXTRA_PRODUCT) as? Product // Use the constant
            }
            Log.d(TAG, "onCreate: currentProduct received: ${currentProduct?.name ?: "None"}")


            setupViews()
            setupProductImageSpinner(null)
            loadCategories()
            if (currentProduct == null) {
                productImageView?.setImageResource(R.drawable.ic_add_photo)
                // Ensure "Default Placeholder" is selected if available in the initial general list
                val defaultImagePos = currentImageNamesInSpinner.indexOf("Default Placeholder")
                if (defaultImagePos != -1 && (spinnerProductImage?.selectedItemPosition
                        ?: -1) != defaultImagePos
                ) {
                    spinnerProductImage?.setSelection(defaultImagePos)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "onCreate: Fatal error during activity creation", e)
            Toast.makeText(this, "Error loading product screen: ${e.message}", Toast.LENGTH_LONG)
                .show()
            finish() // Close activity if a critical error occurs
        }
    }

    private fun setupViews() {
        try {
            productImageView = findViewById(R.id.productImageView)
            spinnerProductImage = findViewById(R.id.spinnerProductImage)
            nameEditText = findViewById(R.id.nameEditText)
            descriptionEditText = findViewById(R.id.descriptionEditText)
            priceEditText = findViewById(R.id.priceEditText)
            stockEditText = findViewById(R.id.stockEditText)
            categorySpinner = findViewById(R.id.categorySpinner)
            saveButton = findViewById(R.id.saveButton)
            backButton = findViewById(R.id.backButton)
            activityTitle = findViewById(R.id.activityTitle)
            progressBar = findViewById(R.id.progressBar)

            // Set title based on mode (Add or Edit)
            activityTitle?.text =
                if (currentProduct != null) getString(R.string.edit_product) else getString(R.string.add_product)

            saveButton?.setOnClickListener {
                saveProduct()
            }
            backButton?.setOnClickListener {
                finish()
            }
            Log.d(TAG, "setupViews: All views initialized and listeners set.")
        } catch (e: Exception) {
            Log.e(TAG, "setupViews: Error initializing views", e)
            Toast.makeText(this, "Error setting up UI components: ${e.message}", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun setupProductImageSpinner(selectedCategoryName: String?) {
        val imagesToShowMap = mutableMapOf<String, Int>()
        // Always add general images first, so they are available.
        categorizedImages[GENERAL_IMAGES_CATEGORY_KEY]?.let { imagesToShowMap.putAll(it) }

        // Add category-specific images if a category is selected
        // These will overwrite general images if they have the same name (which is fine if intended)
        if (selectedCategoryName != null) {
            categorizedImages[selectedCategoryName]?.let { imagesToShowMap.putAll(it) }
        }

        // If imagesToShowMap is still empty (e.g., bad category name, or no general/specific images defined)
        // ensure at least the "Default Placeholder" is there.
        if (imagesToShowMap.isEmpty()) {
            imagesToShowMap["Default Placeholder"] = R.drawable.ic_add_photo
            Log.w(TAG, "setupProductImageSpinner: imagesToShowMap was empty, added default placeholder.")
        }

        val newImageNamesForAdapter = imagesToShowMap.keys.toMutableList().sorted()
        Log.d(TAG, "setupProductImageSpinner: Category '$selectedCategoryName'. Images to show keys: ${newImageNamesForAdapter.joinToString()}")


        // Update currentImageNamesInSpinner BEFORE updating the adapter
        currentImageNamesInSpinner.clear()
        currentImageNamesInSpinner.addAll(newImageNamesForAdapter)

        if (!::productImageAdapter.isInitialized) {
            productImageAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                // Provide a new ArrayList copy for the adapter's initial data
                ArrayList(currentImageNamesInSpinner)
            )
            productImageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerProductImage?.adapter = productImageAdapter
            Log.d(TAG, "setupProductImageSpinner: productImageAdapter initialized. Count: ${productImageAdapter.count}")
        } else {
            productImageAdapter.clear()
            productImageAdapter.addAll(currentImageNamesInSpinner) // Add all new names
            // productImageAdapter.notifyDataSetChanged() // Let's try re-enabling this
            Log.d(TAG, "setupProductImageSpinner: productImageAdapter cleared and new items added. Count: ${productImageAdapter.count}")
        }
        // Crucial: Notify the adapter that its data set has changed.
        // Do this OUTSIDE the if/else for initialization because even on init,
        // the data is set and listeners might depend on a notified state.
        // However, usually setting the adapter itself handles the first refresh.
        // Let's be explicit after data manipulation.
        if (::productImageAdapter.isInitialized) { // Check again in case it was just initialized
            productImageAdapter.notifyDataSetChanged()
        }


        spinnerProductImage?.onItemSelectedListener = null // Clear previous to avoid multiple triggers
        spinnerProductImage?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ){
                // Check against currentImageNamesInSpinner which should be in sync with the adapter
                if (position >= 0 && position < currentImageNamesInSpinner.size) {
                    val selectedName = currentImageNamesInSpinner[position]
                    val newResId = imagesToShowMap[selectedName] ?: R.drawable.ic_add_photo

                    // Only update if the actual resource ID changed, not just the selection name
                    // if (selectedProductImageResId != newResId) { //This check was problematic if names were same but resId should change or vice-versa
                    selectedProductImageResId = newResId
                    productImageView?.setImageResource(selectedProductImageResId)
                    // }
                    Log.d(
                        TAG,
                        "Image selected from spinner: $selectedName, New Res ID: $newResId, Current selectedProductImageResId: $selectedProductImageResId"
                    )
                } else {
                    Log.w(TAG, "Image spinner onItemSelected: Invalid position $position, currentImageNamesInSpinner size: ${currentImageNamesInSpinner.size}")
                    setFallbackImageSelection(imagesToShowMap) // Fallback if position is somehow invalid
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.d(TAG, "Image spinner: Nothing selected.")
                setFallbackImageSelection(imagesToShowMap)
            }
        }
        // Logic to pre-select an image in the spinner
        var imageNameToSelect: String? = null

        if (currentProduct != null && selectedCategoryName == currentProduct?.categoryName) {
            // Editing: Try to select the product's current image if it exists in the new list
            imageNameToSelect = imagesToShowMap.entries.find { it.value == currentProduct?.imageDrawableId }?.key
            if (imageNameToSelect == null) {
                Log.w(TAG, "Product's image (ResId ${currentProduct?.imageDrawableId}) not found in images for category '$selectedCategoryName'. Falling back to placeholder.")
                imageNameToSelect = "Default Placeholder" // Fallback to placeholder for the category
            }
        } else {
            // Adding new product OR category has changed for an existing product: Select "Default Placeholder"
            imageNameToSelect = "Default Placeholder"
        }

        // Fallback if "Default Placeholder" itself isn't in the list for some reason
        if (!currentImageNamesInSpinner.contains(imageNameToSelect) && currentImageNamesInSpinner.isNotEmpty()) {
            if (currentImageNamesInSpinner.contains("Default Placeholder")) {
                imageNameToSelect = "Default Placeholder" // Prefer if available
            } else {
                Log.w(TAG, "'$imageNameToSelect' not in currentImageNamesInSpinner. Selecting first available image.")
                imageNameToSelect = currentImageNamesInSpinner[0] // Fallback to the first image in the list
            }
        } else if (currentImageNamesInSpinner.isEmpty()){
            Log.e(TAG, "setupProductImageSpinner: currentImageNamesInSpinner is EMPTY. Cannot select an image.")
            imageNameToSelect = null // Should not happen if imagesToShowMap logic is correct
        }
        val selectionPosition = if (imageNameToSelect != null) currentImageNamesInSpinner.indexOf(imageNameToSelect) else -1

        if (selectionPosition != -1) {
            // Check if the current selection is already what we want.
            // This avoids unnecessary re-selection and listener firing if already correct.
            if (spinnerProductImage?.selectedItemPosition != selectionPosition) {
                spinnerProductImage?.setSelection(selectionPosition, false) // false to not animate, can help avoid listener issues
                Log.d(TAG, "setupProductImageSpinner: Setting image spinner selection to '$imageNameToSelect' at position $selectionPosition.")
            } else {
                // If already selected, ensure the image view is also reflecting it,
                // as the listener might not have fired if the position didn't change.
                val currentSelectedResId = imagesToShowMap[imageNameToSelect] ?: R.drawable.ic_add_photo
                if (selectedProductImageResId != currentSelectedResId) {
                    selectedProductImageResId = currentSelectedResId
                    productImageView?.setImageResource(selectedProductImageResId)
                }
                Log.d(TAG, "setupProductImageSpinner: Image spinner already at '$imageNameToSelect'. Ensuring ImageView is updated. ResId: $selectedProductImageResId")
            }
        } else {
            Log.w(TAG, "setupProductImageSpinner: Target image '$imageNameToSelect' not found for selection. Using fallback.")
            setFallbackImageSelection(imagesToShowMap)
        }

        Log.d(TAG, "setupProductImageSpinner END: Category '$selectedCategoryName'. Adapter items: ${productImageAdapter.count}. Spinner Selected Item: '${spinnerProductImage?.selectedItem}', Pos: ${spinnerProductImage?.selectedItemPosition}")

    }

    private fun loadCategories() {
        progressBar?.visibility = View.VISIBLE
        categorySpinner?.isEnabled = false
        val spinnerCategoryNames = mutableListOf<String>()

        //db.collection("categories"): Specifies that you want to query the "categories" collection in your Firestore database.
        db.collection("categories")
            .whereEqualTo("isActive", true)
            .orderBy("name", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { documents ->
                if (isFinishing || isDestroyed) return@addOnSuccessListener

                progressBar?.visibility = View.GONE
                categorySpinner?.isEnabled = true
                categoriesList.clear()
                // spinnerCategoryNames.clear() // Not needed as it's a local var initialized empty

                if (currentProduct == null) {
                    spinnerCategoryNames.add(defaultCategoryPrompt)
                }

                if (documents.isEmpty) {
                    Log.d(TAG, "No active categories found in Firestore.")
                    if (spinnerCategoryNames.isEmpty() || (spinnerCategoryNames.size == 1 && spinnerCategoryNames[0] == defaultCategoryPrompt)) {
                        spinnerCategoryNames.add("No categories available")
                    } else if (currentProduct != null && spinnerCategoryNames.isEmpty()) {
                        spinnerCategoryNames.add("No categories available") // Should ideally not happen if product exists
                    }
                    Toast.makeText(this, "No categories available.", Toast.LENGTH_SHORT).show()
                } else {
                    for (document in documents) {
                        try{
                            val category = document.toObject(Category::class.java)
                            category.id = document.id
                            categoriesList.add(category)
                            spinnerCategoryNames.add(category.name)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error converting Firestore document to Category object", e)
                        }
                    }
                }

                if (!::categoryAdapter.isInitialized) {
                    categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerCategoryNames)
                    categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    categorySpinner?.adapter = categoryAdapter
                } else {
                    categoryAdapter.clear()
                    categoryAdapter.addAll(spinnerCategoryNames)
                }
                Log.d(TAG, "Categories loaded for spinner: ${spinnerCategoryNames.joinToString()}")

                var categoryToSelectAdapterPosition = 0
                currentProduct?.let { product ->
                    val productCategoryName = product.categoryName
                    // If editing, spinnerCategoryNames does NOT contain the prompt.
                    // categoriesList contains the actual Category objects.
                    val indexInSpinner = spinnerCategoryNames.indexOf(productCategoryName)
                    if (indexInSpinner != -1) {
                        categoryToSelectAdapterPosition = indexInSpinner
                        Log.d(TAG, "Product category '${product.categoryName}' found in spinner list at index $indexInSpinner")
                    } else {
                        Log.w(TAG, "Category name '${product.categoryName}' for product not found in spinner. Defaulting.")
                        // If category is not found, what should happen? For now, it defaults to 0.
                        // This might be an issue if 0 is "No categories available".
                    }
                }
                // If new product, categoryToSelectAdapterPosition remains 0 (the prompt).
                if (categorySpinner?.selectedItemPosition != categoryToSelectAdapterPosition) {
                    categorySpinner?.setSelection(categoryToSelectAdapterPosition)
                }


                categorySpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        val selectedNameInSpinner = parent?.getItemAtPosition(position).toString()
                        Log.d(TAG, "Category Spinner: '$selectedNameInSpinner' selected at position $position.")

                        // *** FIX 2: Corrected 'if' condition logic ***
                        var actualCategoryNameForImageSpinner: String? = null
                        if (selectedNameInSpinner != defaultCategoryPrompt &&
                            !selectedNameInSpinner.contains("No categories available", ignoreCase = true) &&
                            !selectedNameInSpinner.contains("Error loading categories", ignoreCase = true)
                        ) {
                            // This is a valid category name
                            actualCategoryNameForImageSpinner = selectedNameInSpinner
                        }
                        // If it's the prompt or an error/empty message, actualCategoryNameForImageSpinner remains null
                        // setupProductImageSpinner(null) will correctly show general images.
                        setupProductImageSpinner(actualCategoryNameForImageSpinner)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        setupProductImageSpinner(null) // Show general images
                    }
                }

                if (currentProduct != null) {
                    populateFields()
                } else {
                    // For new products, the category spinner's onItemSelected should have triggered
                    // based on the initial selection (usually the prompt).
                    // If not, ensure image spinner reflects the current category.
                    val initiallySelectedCategoryItem = categorySpinner?.selectedItem?.toString()
                    var categoryForImageInit: String? = null
                    if (initiallySelectedCategoryItem != null &&
                        initiallySelectedCategoryItem != defaultCategoryPrompt &&
                        !initiallySelectedCategoryItem.contains("No categories", ignoreCase = true) &&
                        !initiallySelectedCategoryItem.contains("Error loading", ignoreCase = true)) {
                        categoryForImageInit = initiallySelectedCategoryItem
                    }
                    // This call might be redundant if onItemSelected fired, but ensures correctness.
                    // However, we need to be careful not to override an existing product's image selection
                    // if populateFields() already handled it. This branch is for NEW products.
                    if (categorySpinner?.selectedItemPosition == 0 && spinnerCategoryNames.isNotEmpty() && spinnerCategoryNames[0] == defaultCategoryPrompt) {
                        setupProductImageSpinner(null) // Explicitly ensure general for prompt
                    } else {
                        setupProductImageSpinner(categoryForImageInit)
                    }
                }
            }
            .addOnFailureListener { exception ->
                if (isFinishing || isDestroyed) return@addOnFailureListener
                progressBar?.visibility = View.GONE
                categorySpinner?.isEnabled = true
                Log.e(TAG, "Error loading categories from Firestore", exception)
                Toast.makeText(this, "Error loading categories: ${exception.message}", Toast.LENGTH_LONG).show()

                spinnerCategoryNames.clear() // Already local, but good practice.
                if (currentProduct == null) spinnerCategoryNames.add(defaultCategoryPrompt)
                spinnerCategoryNames.add("Error loading categories")

                if (!::categoryAdapter.isInitialized) {
                    categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerCategoryNames)
                    categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    categorySpinner?.adapter = categoryAdapter
                } else {
                    categoryAdapter.clear()
                    categoryAdapter.addAll(spinnerCategoryNames)
                }
                val errorMsgIndex = spinnerCategoryNames.indexOf("Error loading categories")
                val selectionOnError = if (currentProduct == null && spinnerCategoryNames.contains(defaultCategoryPrompt)) 0
                else if (errorMsgIndex != -1) errorMsgIndex
                else 0
                categorySpinner?.setSelection(selectionOnError)
                setupProductImageSpinner(null)
            }
                }

    private fun populateFields() {
        currentProduct?.let { product ->
            Log.d(TAG, "populateFields: Populating for product: ${product.name}, CategoryName: ${product.categoryName}, ImageResId: ${product.imageDrawableId}")
            nameEditText?.setText(product.name)
            descriptionEditText?.setText(product.description)
            priceEditText?.setText(product.price.toString())
            stockEditText?.setText(product.stock.toString())

            // Category spinner should have been set by loadCategories

            // IMPORTANT: Set up the product image spinner for the product's category *before* trying to select an image
            // This ensures the correct image list is loaded in the image spinner.
            setupProductImageSpinner(product.categoryName) // This will load correct images for the product's category

            // Now that the image spinner is populated for the correct category, find and select the product's image.
            selectedProductImageResId = product.imageDrawableId ?: R.drawable.ic_add_photo
            // productImageView?.setImageResource(selectedProductImageResId) // This is set by setupProductImageSpinner's selection logic

            val imageNameForProduct = (categorizedImages[product.categoryName]?.entries?.find { it.value == selectedProductImageResId }?.key)
                ?: (categorizedImages[GENERAL_IMAGES_CATEGORY_KEY]?.entries?.find { it.value == selectedProductImageResId }?.key)

            if (imageNameForProduct != null) {
                val imagePositionInCurrentSpinner = currentImageNamesInSpinner.indexOf(imageNameForProduct)
                if (imagePositionInCurrentSpinner != -1) {
                    if(spinnerProductImage?.selectedItemPosition != imagePositionInCurrentSpinner) {
                        spinnerProductImage?.setSelection(imagePositionInCurrentSpinner)
                    }
                    // The onItemSelected listener of spinnerProductImage should then set productImageView
                    Log.d(TAG, "populateFields: Product image spinner selection target: '$imageNameForProduct' (pos $imagePositionInCurrentSpinner)")
                } else{
                    Log.w(TAG, "populateFields: Image '$imageNameForProduct' (ResId ${product.imageDrawableId}) for product category NOT found in current image spinner items. Defaulting.")
                    setFallbackImageSelection( (categorizedImages[product.categoryName] ?: emptyMap()) + (categorizedImages[GENERAL_IMAGES_CATEGORY_KEY] ?: emptyMap()) )
                }
            } else {
                Log.w(TAG, "populateFields: No image name found for resId '${product.imageDrawableId}'. Defaulting.")
                setFallbackImageSelection( (categorizedImages[product.categoryName] ?: emptyMap()) + (categorizedImages[GENERAL_IMAGES_CATEGORY_KEY] ?: emptyMap()) )
            }
            Log.d(TAG, "populateFields: Fields populated for product: ${product.name}")
        }
    }

    private fun setFallbackImageSelection(imagesMapToUse: Map<String, Int>) {
        val defaultPlaceholderName = "Default Placeholder"
        // Use the current items in the adapter for finding positions
        val adapterItems = List(productImageAdapter.count) { i -> productImageAdapter.getItem(i).toString() }
        val defaultPlaceholderPosition = adapterItems.indexOf(defaultPlaceholderName)

        var newSelectionResId = R.drawable.ic_add_photo
        var newPositionToSelect = -1

        if (defaultPlaceholderPosition != -1) {
            newPositionToSelect = defaultPlaceholderPosition
            newSelectionResId = imagesMapToUse[defaultPlaceholderName] ?: R.drawable.ic_add_photo
        } else if (adapterItems.isNotEmpty()) {
            newPositionToSelect = 0
            newSelectionResId = imagesMapToUse[adapterItems[0]] ?: R.drawable.ic_add_photo
        } else {
            Log.w(TAG, "setFallbackImageSelection: No images in spinner to select from.")
            productImageView?.setImageResource(R.drawable.ic_add_photo) // Fallback image directly
            selectedProductImageResId = R.drawable.ic_add_photo
            return
        }

        if (newPositionToSelect != -1 && spinnerProductImage?.selectedItemPosition != newPositionToSelect) {
            spinnerProductImage?.setSelection(newPositionToSelect)
            // The spinner's onItemSelected will handle setting the image view and selectedProductImageResId
        } else if(newPositionToSelect != -1 && selectedProductImageResId != newSelectionResId) {
            // If position is already correct, but our internal tracking of ResId is off, or image view is wrong
            selectedProductImageResId = newSelectionResId
            productImageView?.setImageResource(selectedProductImageResId)
        }


        Log.d(
            TAG,
            "setFallbackImageSelection: Spinner set to fallback. Target Pos: $newPositionToSelect, Current Pos: ${spinnerProductImage?.selectedItemPosition}, ResId: $selectedProductImageResId"
        )
    }

    private fun saveProduct() {
        val name = nameEditText?.text.toString().trim()
        val description = descriptionEditText?.text.toString().trim()
        val priceText = priceEditText?.text.toString().trim()
        val stockText = stockEditText?.text.toString().trim()
        val selectedCategoryAdapterPosition = categorySpinner?.selectedItemPosition ?: -1

        if (!validateInput(name, description, priceText, stockText, selectedCategoryAdapterPosition)) {
            return
        }
        // Determine the actual selected Category object
        var selectedActualCategory: Category? = null
        val selectedCategoryNameInSpinner =
            categorySpinner?.getItemAtPosition(selectedCategoryAdapterPosition).toString()

        if (currentProduct == null && selectedCategoryAdapterPosition == 0 && selectedCategoryNameInSpinner == defaultCategoryPrompt) {
            // This case should be caught by validateInput, but defensive check
            Toast.makeText(this, "Internal error: Please select a valid category.", Toast.LENGTH_SHORT).show()
            return
        }
        // Adjust index to map from spinner (which might have prompt) to categoriesList
        val categoryListIndex: Int
        if (currentProduct == null) { // New product: prompt is at index 0 in spinnerCategoryNames
            // selectedCategoryAdapterPosition = 0 means prompt, 1 means first actual category
            if (selectedCategoryAdapterPosition > 0) { // Make sure it's not the prompt
                // categoriesList maps directly to spinnerCategoryNames *after* the prompt
                categoryListIndex = selectedCategoryAdapterPosition - 1
            } else {
                Toast.makeText(this, "Please select a valid category (not prompt).", Toast.LENGTH_LONG).show()
                return // Should be caught by validation, but defensive.
            }
        } else { // Editing product: no prompt in spinnerCategoryNames (usually)
            // categoriesList and spinnerCategoryNames should align if no prompt was added.
            categoryListIndex = selectedCategoryAdapterPosition
        }


        if (categoryListIndex >= 0 && categoryListIndex < categoriesList.size) {
            selectedActualCategory = categoriesList[categoryListIndex]
        } else {
            Toast.makeText(this, "Selected category is not valid. Please re-select.", Toast.LENGTH_LONG).show()
            Log.e(TAG, "saveProduct: Could not map spinner selection '$selectedCategoryNameInSpinner' (adapter pos $selectedCategoryAdapterPosition, derived list index $categoryListIndex) to a Category object from categoriesList (size ${categoriesList.size}).")
            return
        }

        if (selectedActualCategory == null) {
            Toast.makeText(this, "Critical error: Category not resolved.", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar?.visibility = View.VISIBLE
        saveButton?.isEnabled = false

        val productId = currentProduct?.id ?: db.collection("products").document().id
        val currentTime = System.currentTimeMillis()

        val productToSave = Product(
            id = productId,
            name = name,
            description = description,
            price = priceText.toDoubleOrNull() ?: 0.0,
            stock = stockText.toIntOrNull() ?: 0,
            categoryId = selectedActualCategory.id,
            categoryName = selectedActualCategory.name, // Use name from resolved Category object
            imageDrawableId = selectedProductImageResId,
            imageUrl = currentProduct?.imageUrl, // Keep existing image URL if any, or null
            isActive = currentProduct?.isActive ?: true,
            createdAt = currentProduct?.createdAt ?: currentTime,
            updatedAt = currentTime
        )
        val productDocumentRef = db.collection("products").document(productToSave.id)

        productDocumentRef.set(productToSave)
            .addOnSuccessListener {
                if (isFinishing || isDestroyed) return@addOnSuccessListener
                progressBar?.visibility = View.GONE
                saveButton?.isEnabled = true
                val message = if (currentProduct == null) "added" else "updated"
                Toast.makeText(
                    this,
                    "Product '${productToSave.name}' $message successfully.",
                    Toast.LENGTH_LONG
                ).show()
                Log.d(TAG, "saveProduct: Success. Firestore ID: ${productToSave.id}")
                setResult(Activity.RESULT_OK)
                finish()
            }
            .addOnFailureListener { e ->
                    if (isFinishing || isDestroyed) return@addOnFailureListener
                    progressBar?.visibility = View.GONE
                    saveButton?.isEnabled = true
                    val actionType = if (currentProduct == null) "add" else "update"
                    Toast.makeText(this, "Failed to $actionType product: ${e.message}",
                        Toast.LENGTH_LONG).show()
                    Log.e(TAG, "saveProduct: Error Firestore $actionType for ${productToSave.name}", e
                    )

                }
            }
        private fun validateInput(name: String, description: String, priceText: String, stockText: String, categoryAdapterPosition: Int): Boolean {
            var isValid = true
            if (name.isEmpty()) {
                nameEditText?.error = "Product name is required"
                isValid = false
            } else {
                nameEditText?.error = null
            }

            if (description.isEmpty()) {
                descriptionEditText?.error = "Description is required"
                isValid = false
            } else {
                descriptionEditText?.error = null
            }

            if (priceText.isEmpty()) {
                priceEditText?.error = "Price is required"
                isValid = false
            } else {
                try {
                    priceText.toDouble()
                    priceEditText?.error = null
                } catch (e: NumberFormatException) {
                    priceEditText?.error = "Invalid price format"
                    isValid = false
                }
            }

            if (stockText.isEmpty()) {
                stockEditText?.error = "Stock is required"
                isValid = false

            } else {
                try {
                    stockText.toInt()
                    stockEditText?.error = null
                } catch (e: NumberFormatException) {
                    stockEditText?.error = "Invalid stock format"
                    isValid = false // Set isValid to false here too
                }
            }
            // The unconditional isValid = false was removed from here.

            val selectedCategoryItemName = categorySpinner?.getItemAtPosition(categoryAdapterPosition)?.toString()

            if (categoryAdapterPosition == -1 || selectedCategoryItemName == null) {
                Toast.makeText(this, "Please select a category.", Toast.LENGTH_SHORT).show()
                isValid = false
            } else if (currentProduct == null && categoryAdapterPosition == 0 && selectedCategoryItemName == defaultCategoryPrompt) {
                Toast.makeText(this, "Please select a category.", Toast.LENGTH_SHORT).show()
                isValid = false
            } else if (selectedCategoryItemName.contains("No categories available", ignoreCase = true) ||
                selectedCategoryItemName.contains("Error loading categories", ignoreCase = true)
            ) {
                Toast.makeText(this, "Cannot save. No valid categories available for selection.", Toast.LENGTH_LONG).show()
                isValid = false
            }
            Log.d(TAG, "validateInput: Input validation result: $isValid")
            return isValid
        }
}

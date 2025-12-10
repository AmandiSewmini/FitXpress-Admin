package com.example.admin.fitxpress

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.get
// import com.bumptech.glide.Glide
import com.example.admin.fitxpress.models.Trainee
import com.example.admin.fitxpress.models.WorkingHours
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.getValue
import kotlin.io.path.exists

class AddEditTrainerActivity : AppCompatActivity() {

    private lateinit var imageUrlEditText: TextInputEditText
    private lateinit var nameEditText: TextInputEditText // Changed from EditText?
    private lateinit var emailEditText: TextInputEditText // Changed from EditText?
    private lateinit var specializationEditText: TextInputEditText // Changed from EditText?
    private lateinit var experienceEditText: TextInputEditText // Changed from EditText?
    private lateinit var feeEditText: TextInputEditText
    private lateinit var nextAvailableEditText: TextInputEditText
    private lateinit var startHoursEditText: TextInputEditText
    private lateinit var endHoursEditText: TextInputEditText
    private lateinit var statusSwitch: SwitchMaterial

    private lateinit var saveButton: Button
    private lateinit var backButton: ImageView
    private lateinit var activityTitle: TextView
    private lateinit var progressBar: ProgressBar

    private var currentTrainee: Trainee? = null //Holds the Trainee object if the activity is in "Edit" mode. It's null for "Add" mode
    private var currentTraineeId: String? = null
    private var selectedNextAvailableCalendar: Calendar = Calendar.getInstance()

      // Firebase Database reference
    private lateinit var database: FirebaseDatabase

    companion object {

        private const val TAG = "AddEditTrainerActivity"
        const val EXTRA_TRAINEE_ID = "trainee_id" //A constant string used as a key when passing a trainee's ID to this activity via an Intent
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_trainer)

        database = Firebase.database
        setupViews() // Initialize all views

        currentTraineeId = intent.getStringExtra(EXTRA_TRAINEE_ID)

        if (currentTraineeId != null) {
            activityTitle.text = getString(R.string.edit_trainer)
            Log.d(TAG, "onCreate: Edit mode for trainee ID: $currentTraineeId")
            loadTraineeData(currentTraineeId!!)
        } else {
            activityTitle.text = getString(R.string.add_trainer)
            Log.d(TAG, "onCreate: Add mode for new trainee.")
            // Set default values for a new trainee form if needed
            updateNextAvailableEditTextDisplay() // Show current date/time as default
            startHoursEditText.setText("9")      // Default 9 AM (as per WorkingHours default)
            endHoursEditText.setText("17")     // Default 5 PM (as per WorkingHours default)
            statusSwitch.isChecked = true      // Default to active
        }

        setupClickListeners()
    }

    private fun setupViews() {
        try {
            imageUrlEditText = findViewById(R.id.imageUrlEditText)
            nameEditText = findViewById(R.id.nameEditText)
            emailEditText = findViewById(R.id.emailEditText)
            specializationEditText = findViewById(R.id.specializationEditText)
            experienceEditText = findViewById(R.id.experienceEditText)
            feeEditText = findViewById(R.id.feeEditText)
            nextAvailableEditText = findViewById(R.id.nextAvailableEditText)
            startHoursEditText = findViewById(R.id.startHoursEditText)
            endHoursEditText = findViewById(R.id.endHoursEditText)
            statusSwitch = findViewById(R.id.statusSwitch)

            saveButton = findViewById(R.id.saveButton)
            backButton = findViewById(R.id.backButton)
            activityTitle = findViewById(R.id.activityTitle)
            progressBar = findViewById(R.id.progressBar)

            Log.d(TAG, "setupViews: All views initialized.")

        } catch (e: Exception) {
            Log.e(TAG, "setupViews: Error initializing views", e)
            Toast.makeText(this, "Error setting up UI components: ${e.message}", Toast.LENGTH_LONG).show()
            finish() // Critical error, cannot proceed
        }
    }

    private fun setupClickListeners() {
        nextAvailableEditText.setOnClickListener {
            showDateTimePickerDialog()
        }
        saveButton.setOnClickListener {
            saveTraineeDataToFirebase()
        }
        backButton.setOnClickListener {
            finish()
        }
    }
// Fetches data for a specific trainer from Firebase Realtime Database.
    private fun loadTraineeData(traineeId: String) {
        progressBar.visibility = View.VISIBLE
        val traineeRef = database.getReference("trainees").child(traineeId) // Assuming "trainees" is your DB node

        traineeRef.get().addOnSuccessListener { dataSnapshot ->
            progressBar.visibility = View.GONE
            if (dataSnapshot.exists()) {
                currentTrainee = dataSnapshot.getValue(Trainee::class.java)
                currentTrainee?.let {
                    Log.d(TAG, "Trainee data loaded: ${it.name}")
                    populateFields(it) //Calls populateFields(it) to fill the UI form with the loaded data.
                }
            } else {
                Log.w(TAG, "loadTraineeData: No trainee found with ID: $traineeId")
                Toast.makeText(this, "Trainee not found.", Toast.LENGTH_LONG).show()
                // finish() // Or allow creating a new one if ID was somehow invalid
            }
        }.addOnFailureListener { exception ->
            progressBar.visibility = View.GONE
            Log.e(TAG, "Error loading trainee data", exception)
            Toast.makeText(this, "Error loading trainee: ${exception.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun populateFields(trainee: Trainee) {
        nameEditText.setText(trainee.name)
        emailEditText.setText(trainee.email)
        specializationEditText.setText(trainee.specialty) // Trainee model uses 'specialty'
        experienceEditText.setText(trainee.experience)
        feeEditText.setText(trainee.fee)
        imageUrlEditText.setText(trainee.imageUrl)

        selectedNextAvailableCalendar.timeInMillis = trainee.nextAvailableTime
        updateNextAvailableEditTextDisplay()

        startHoursEditText.setText(trainee.workingHours.startHour.toString())
        endHoursEditText.setText(trainee.workingHours.endHour.toString())
        statusSwitch.isChecked = trainee.isActive

        Log.d(TAG, "populateFields: Fields populated for trainee: ${trainee.name}")


    }

    private fun showDateTimePickerDialog() {
        val calendarToShow = Calendar.getInstance().apply {
            if (nextAvailableEditText.text?.isNotEmpty() == true || currentTrainee != null) {
                timeInMillis = selectedNextAvailableCalendar.timeInMillis
            }
        }

        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            selectedNextAvailableCalendar.set(Calendar.YEAR, year)
            selectedNextAvailableCalendar.set(Calendar.MONTH, month)
            selectedNextAvailableCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            TimePickerDialog(this, { _, hourOfDay, minute ->
                selectedNextAvailableCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedNextAvailableCalendar.set(Calendar.MINUTE, minute)
                selectedNextAvailableCalendar.set(Calendar.SECOND, 0) // Optional: Reset seconds
                selectedNextAvailableCalendar.set(Calendar.MILLISECOND, 0) // Optional: Reset milliseconds
                updateNextAvailableEditTextDisplay()
            }, calendarToShow.get(Calendar.HOUR_OF_DAY), calendarToShow.get(Calendar.MINUTE), false) // false for 12-hour AM/PM if preferred, true for 24-hour
                .show()
        }, calendarToShow.get(Calendar.YEAR), calendarToShow.get(Calendar.MONTH), calendarToShow.get(Calendar.DAY_OF_MONTH))
            .show()
    }

    private fun updateNextAvailableEditTextDisplay() {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) // Date and Time format
        nextAvailableEditText.setText(sdf.format(selectedNextAvailableCalendar.time))
    }

    private fun saveTraineeDataToFirebase() {
        val name = nameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val specialty = specializationEditText.text.toString().trim()
        val experience = experienceEditText.text.toString().trim()
        val fee = feeEditText.text.toString().trim()
        val imageUrl = imageUrlEditText.text.toString().trim()
        val nextTimestamp = selectedNextAvailableCalendar.timeInMillis
        val startHourStr = startHoursEditText.text.toString().trim()
        val endHourStr = endHoursEditText.text.toString().trim()
        val isActive = statusSwitch.isChecked

        if (!validateInput(name, email, specialty, experience, imageUrl, fee, startHourStr, endHourStr)) {
            return
        }

        // Safe to convert after validation
        val startHour = startHourStr.toInt()
        val endHour = endHourStr.toInt()

        progressBar.visibility = View.VISIBLE
        saveButton.isEnabled = false

        val traineeIdToSave = currentTraineeId ?: database.getReference("trainees").push().key ?: UUID.randomUUID().toString()


        val traineeToSave = Trainee(
            id = traineeIdToSave,
            name = name,
            email = email,
            specialty = specialty, // Matches Trainee data class
            experience = experience,
            fee = fee,
            imageUrl = imageUrl,
            nextAvailableTime = nextTimestamp,
            workingHours = WorkingHours(startHour, endHour),
            isActive = isActive
        )

        database.getReference("trainees").child(traineeIdToSave).setValue(traineeToSave)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                saveButton.isEnabled = true
                val action = if (currentTraineeId != null) "updated" else "added"
                Log.d(TAG, "Trainee data successfully $action in Realtime Database!")
                Toast.makeText(this, "Trainee ${traineeToSave.name} $action.", Toast.LENGTH_SHORT).show()

                val resultIntent = Intent()
                // Optionally pass back the ID if the calling activity needs it
                // resultIntent.putExtra(EXTRA_TRAINEE_ID, traineeIdToSave)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                saveButton.isEnabled = true
                Log.w(TAG, "Error writing trainee data to Realtime Database", e)
                Toast.makeText(this, "Error saving trainee: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun validateInput(
        name: String, email: String, specialty: String,
        experience: String, imageUrl: String, fee: String,
        startHourStr: String, endHourStr: String
    ): Boolean {
        var isValid = true

        if (name.isEmpty()) {
            nameEditText.error = "Trainee name is required"; isValid = false
        } else { nameEditText.error = null }

        if (email.isEmpty()) {
            emailEditText.error = "Email is required"; isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.error = "Invalid email format"; isValid = false
        } else { emailEditText.error = null }

        if (specialty.isEmpty()) {
            specializationEditText.error = "Specialty is required"; isValid = false
        } else { specializationEditText.error = null }

        if (experience.isEmpty()) { // Assuming experience is just a string now, not necessarily years
            experienceEditText.error = "Experience details are required"; isValid = false
        } else { experienceEditText.error = null }

        if (fee.isEmpty()) { // Assuming fee is a string, e.g., "500", "Contact for price"
            feeEditText.error = "Fee details are required"; isValid = false
        } else { feeEditText.error = null }

        if (imageUrl.isEmpty()) {
            imageUrlEditText.error = "Image URL is required"; isValid = false
        } else if (!Patterns.WEB_URL.matcher(imageUrl).matches()) { // Basic URL validation
            imageUrlEditText.error = "Invalid Image URL format"; isValid = false
        } else { imageUrlEditText.error = null }


        val startHour = startHourStr.toIntOrNull()
        val endHour = endHourStr.toIntOrNull()

        if (startHourStr.isEmpty() || startHour == null || startHour < 0 || startHour > 23) {
            startHoursEditText.error = "Valid start hour (0-23) required"; isValid = false
        } else { startHoursEditText.error = null }

        if (endHourStr.isEmpty() || endHour == null || endHour < 0 || endHour > 23) {
            endHoursEditText.error = "Valid end hour (0-23) required"; isValid = false
        } else { endHoursEditText.error = null }

        if (startHour != null && endHour != null && startHour >= endHour) {
            endHoursEditText.error = "End hour must be after start hour"
            isValid = false
        } else {
            // Clear error if individually valid and relationship is now correct
            if (endHoursEditText.error == "End hour must be after start hour" && startHour != null && endHour != null && startHour < endHour){
                endHoursEditText.error = null
            }
        }
        if (nextAvailableEditText.text.isNullOrEmpty()) {
            // Error on this non-focusable field might not be very visible. Toast helps.
            Toast.makeText(this, "Next available date/time is required.", Toast.LENGTH_LONG).show()
            nextAvailableEditText.error = "Required" // May not be very visible
            isValid = false
        } else { nextAvailableEditText.error = null }

        if (!isValid) {
            Log.w(TAG, "Input validation failed.")
            // Consider showing a general Toast if multiple fields have errors
            Toast.makeText(this, "Please correct the errors in the form.", Toast.LENGTH_LONG).show()
        }
        return isValid
    }

    override fun onResume() {
        super.onResume()
        // Re-check title, useful if the activity was paused and resumed
        activityTitle.text = if (currentTraineeId != null) getString(R.string.edit_trainer) else getString(R.string.add_trainer)
        Log.d(TAG, "onResume: Activity title re-set to: ${activityTitle.text}")
    }


}

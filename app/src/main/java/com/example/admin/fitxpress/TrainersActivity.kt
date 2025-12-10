//package com.example.admin.fitxpress
//
//import android.content.Intent
//import android.os.Bundle
//import android.view.View
//import android.widget.*
//import androidx.appcompat.app.AlertDialog
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.widget.addTextChangedListener
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.example.admin.fitxpress.adapters.TrainersAdapter
//import com.example.admin.fitxpress.models.Trainee
//import com.google.android.material.floatingactionbutton.FloatingActionButton
//// import com.google.firebase.firestore.FirebaseFirestore
//// import com.google.firebase.firestore.Query
//
//class TrainersActivity : AppCompatActivity() {
//
//    // private lateinit var db: FirebaseFirestore
//
//    private lateinit var trainersRecyclerView: RecyclerView
//    private lateinit var addTrainerFab: FloatingActionButton
//    private lateinit var searchEditText: EditText
//    private lateinit var backButton: ImageView
//    private lateinit var progressBar: ProgressBar
//    private lateinit var emptyStateLayout: LinearLayout
//
//    private val trainersList = mutableListOf<Trainee>()
//    private val filteredTrainersList = mutableListOf<Trainee>()
//    private lateinit var trainersAdapter: TrainersAdapter
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_trainers)
//
//        // Firebase initialization commented out for simplified flow
//        // db = FirebaseFirestore.getInstance()
//
//        setupViews()
//        setupRecyclerView()
//        loadTrainers() // This will now load dummy trainers
//    }
//
//    private fun setupViews() {
//        trainersRecyclerView = findViewById(R.id.trainersRecyclerView)
//        addTrainerFab = findViewById(R.id.addTrainerFab)
//        searchEditText = findViewById(R.id.searchEditText)
//        backButton = findViewById(R.id.backButton)
//        progressBar = findViewById(R.id.progressBar)
//        emptyStateLayout = findViewById(R.id.emptyStateLayout)
//
//        addTrainerFab.setOnClickListener {
//            val intent = Intent(this, AddEditTrainerActivity::class.java)
//            startActivity(intent)
//        }
//
//        backButton.setOnClickListener {
//            finish()
//        }
//
//        searchEditText.addTextChangedListener { text ->
//            filterTrainers(text.toString())
//        }
//    }
//
//    private fun setupRecyclerView() {
//        trainersRecyclerView.layoutManager = LinearLayoutManager(this)
//        trainersAdapter = TrainersAdapter(
//            trainers = filteredTrainersList,
//            onEditClick = { trainer ->
//                editTrainer(trainer)
//            },
//            onDeleteClick = { trainer ->
//                showDeleteDialog(trainer)
//            },
//            onStatusToggle = { trainer ->
//                toggleTrainerStatus(trainer)
//            },
//            onVerificationToggle = { trainer ->
//                toggleTrainerVerification(trainer)
//            }
//        )
//        trainersRecyclerView.adapter = trainersAdapter
//    }
//
//    private fun loadTrainers() {
//        progressBar.visibility = View.VISIBLE
//        // Dummy trainers for testing UI flow without Firebase
//        trainersList.clear()
//        trainersList.add(Trainer(id = "t1", name = "John Doe", email = "john.doe@example.com", specialization = "Personal Trainer", experienceYears = 5, rating = 4.8, clientsCount = 25, imageUrl = "https://example.com/john.jpg", isActive = true, isVerified = true, joinDate = System.currentTimeMillis() - (5L * 365 * 24 * 60 * 60 * 1000)))
//        trainersList.add(Trainer(id = "t2", name = "Jane Smith", email = "jane.smith@example.com", specialization = "Yoga Instructor", experienceYears = 8, rating = 4.9, clientsCount = 40, imageUrl = "https://example.com/jane.jpg", isActive = true, isVerified = true, joinDate = System.currentTimeMillis() - (8L * 365 * 24 * 60 * 60 * 1000)))
//        trainersList.add(Trainer(id = "t3", name = "Mike Johnson", email = "mike.j@example.com", specialization = "Strength & Conditioning", experienceYears = 3, rating = 4.5, clientsCount = 15, imageUrl = "https://example.com/mike.jpg", isActive = false, isVerified = false, joinDate = System.currentTimeMillis() - (3L * 365 * 24 * 60 * 60 * 1000)))
//        trainersList.add(Trainer(id = "t4", name = "Sarah Lee", email = "sarah.l@example.com", specialization = "Nutrition Coach", experienceYears = 10, rating = 5.0, clientsCount = 50, imageUrl = "https://example.com/sarah.jpg", isActive = true, isVerified = false, joinDate = System.currentTimeMillis() - (10L * 365 * 24 * 60 * 60 * 1000)))
//        trainersList.add(Trainer(id = "t5", name = "David Chen", email = "david.c@example.com", specialization = "CrossFit Coach", experienceYears = 7, rating = 4.7, clientsCount = 30, imageUrl = "https://example.com/david.jpg", isActive = true, isVerified = true, joinDate = System.currentTimeMillis() - (7L * 365 * 24 * 60 * 60 * 1000)))
//
//        progressBar.visibility = View.GONE
//        filterTrainers(searchEditText.text.toString())
//        updateEmptyState()
//        Toast.makeText(this, "Dummy trainers loaded.", Toast.LENGTH_SHORT).show()
//
//        // Firebase data loading commented out
//        /*
//        db.collection("trainers")
//            .orderBy("name", Query.Direction.ASCENDING)
//            .addSnapshotListener { snapshot, e ->
//                progressBar.visibility = View.GONE
//                if (e != null) {
//                    Toast.makeText(this, "Error loading trainers: ${e.message}", Toast.LENGTH_SHORT).show()
//                    return@addSnapshotListener
//                }
//                trainersList.clear()
//                snapshot?.documents?.forEach { doc ->
//                    val trainer = doc.toObject(Trainer::class.java)
//                    trainer?.let {
//                        it.id = doc.id
//                        trainersList.add(it)
//                    }
//                }
//                filterTrainers(searchEditText.text.toString())
//                updateEmptyState()
//            }
//        */
//    }
//
//    private fun filterTrainers(query: String) {
//        filteredTrainersList.clear()
//        if (query.isEmpty()) {
//            filteredTrainersList.addAll(trainersList)
//        } else {
//            filteredTrainersList.addAll(
//                trainersList.filter {
//                    it.name.contains(query, ignoreCase = true) ||
//                            it.email.contains(query, ignoreCase = true) ||
//                            it.specialization.contains(query, ignoreCase = true)
//                }
//            )
//        }
//        trainersAdapter.notifyDataSetChanged()
//        updateEmptyState()
//    }
//
//    private fun updateEmptyState() {
//        if (filteredTrainersList.isEmpty()) {
//            emptyStateLayout.visibility = View.VISIBLE
//            trainersRecyclerView.visibility = View.GONE
//        } else {
//            emptyStateLayout.visibility = View.GONE
//            trainersRecyclerView.visibility = View.VISIBLE
//        }
//    }
//
//    private fun editTrainer(trainer: Trainer) {
//        val intent = Intent(this, AddEditTrainerActivity::class.java)
//        intent.putExtra("trainer", trainer)
//        startActivity(intent)
//    }
//
//    private fun showDeleteDialog(trainer: Trainer) {
//        AlertDialog.Builder(this)
//            .setTitle("Delete Trainer")
//            .setMessage("Are you sure you want to delete '${trainer.name}'?")
//            .setPositiveButton("Delete") { _, _ ->
//                deleteTrainer(trainer) // This will now show a toast
//            }
//            .setNegativeButton("Cancel", null)
//            .show()
//    }
//
//    private fun deleteTrainer(trainer: Trainer) {
//        progressBar.visibility = View.VISIBLE
//        // Firebase delete logic commented out
//        progressBar.visibility = View.GONE
//        Toast.makeText(this, "Trainer '${trainer.name}' deleted (bypassed Firebase delete).", Toast.LENGTH_SHORT).show()
//        // Manually remove from dummy list for UI update
//        trainersList.remove(trainer)
//        filterTrainers(searchEditText.text.toString())
//        /*
//        db.collection("trainers").document(trainer.id)
//            .delete()
//            .addOnSuccessListener {
//                progressBar.visibility = View.GONE
//                Toast.makeText(this, "Trainer deleted successfully", Toast.LENGTH_SHORT).show()
//            }
//            .addOnFailureListener { e ->
//                Toast.makeText(this, "Error deleting trainer: ${e.message}", Toast.LENGTH_SHORT).show()
//            }
//        */
//    }
//
//    private fun toggleTrainerStatus(trainer: Trainer) {
//        val newStatus = !trainer.isActive
//        val statusText = if (newStatus) "activated" else "deactivated"
//        // Firebase update logic commented out
//        Toast.makeText(this, "Trainer '${trainer.name}' status $statusText (bypassed Firebase update).", Toast.LENGTH_SHORT).show()
//        // Manually update status in dummy list for UI update
//        val index = trainersList.indexOf(trainer)
//        if (index != -1) {
//            trainersList[index] = trainer.copy(isActive = newStatus)
//            filterTrainers(searchEditText.text.toString())
//        }
//        /*
//        db.collection("trainers").document(trainer.id)
//            .update("isActive", newStatus, "updatedAt", System.currentTimeMillis())
//            .addOnSuccessListener {
//                Toast.makeText(this, "Trainer status $statusText successfully", Toast.LENGTH_SHORT).show()
//            }
//            .addOnFailureListener { e ->
//                Toast.makeText(this, "Error updating status: ${e.message}", Toast.LENGTH_SHORT).show()
//            }
//        */
//    }
//
//    private fun toggleTrainerVerification(trainer: Trainer) {
//        val newVerificationStatus = !trainer.isVerified
//        val statusText = if (newVerificationStatus) "verified" else "unverified"
//        Toast.makeText(this, "Trainer '${trainer.name}' verification $statusText (bypassed Firebase update).", Toast.LENGTH_SHORT).show()
//        val index = trainersList.indexOf(trainer)
//        if (index != -1) {
//            trainersList[index] = trainer.copy(isVerified = newVerificationStatus)
//            filterTrainers(searchEditText.text.toString())
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        // Refresh trainers list when returning to this activity
//        loadTrainers()
//    }
//}


package com.example.admin.fitxpress

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.core.view.isEmpty
import androidx.core.view.size
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.admin.fitxpress.adapters.TrainersAdapter
import com.example.admin.fitxpress.models.Trainee // Use your Trainee model
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.* // Firebase Realtime Database

class TrainersActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference // Firebase Realtime Database

    private lateinit var trainersRecyclerView: RecyclerView
    private lateinit var addTrainerFab: FloatingActionButton
    private lateinit var searchEditText: EditText
    private lateinit var backButton: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyStateLayout: LinearLayout

    private val originalTraineesList = mutableListOf<Trainee>() // Stores all trainees from Firebase
    private val filteredTraineesList = mutableListOf<Trainee>() // For displaying in RecyclerView
    private lateinit var trainersAdapter: TrainersAdapter

    private var traineesValueEventListener: ValueEventListener? = null // To remove listener later

    companion object {
        private const val TAG = "TrainersActivity"
    }

    // Activity result launcher for Add/Edit Trainee
    private val addEditTraineeResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // The ValueEventListener should handle updates automatically.
                // If not, you might want to refresh explicitly here.
                Log.d(TAG, "Returned from AddEditTrainerActivity with RESULT_OK.")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trainers) // Your existing XML for the activity

        // Firebase Realtime Database initialization
        // Make sure AddEditTrainerActivity also saves to "trainees"
        database = FirebaseDatabase.getInstance().getReference("trainees")

        setupViews()
        setupRecyclerView()
        // loadAndListenForTrainees() will be called in onResume
    }

    private fun setupViews() {
        trainersRecyclerView = findViewById(R.id.trainersRecyclerView)
        addTrainerFab = findViewById(R.id.addTrainerFab)
        searchEditText = findViewById(R.id.searchEditText)
        backButton = findViewById(R.id.backButton)
        progressBar = findViewById(R.id.progressBar)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)

        addTrainerFab.setOnClickListener {
            val intent = Intent(this, AddEditTrainerActivity::class.java)
            // For adding a new trainee, no ID is passed.
            // AddEditTrainerActivity should handle the case where EXTRA_TRAINEE_ID is null.
            addEditTraineeResultLauncher.launch(intent)
        }

        backButton.setOnClickListener {
            finish()
        }

        searchEditText.addTextChangedListener { text ->
            filterTrainees(text.toString())
        }
    }

    private fun setupRecyclerView() {
        trainersRecyclerView.layoutManager = LinearLayoutManager(this)
        trainersAdapter = TrainersAdapter(
            trainees = filteredTraineesList, // Pass the filtered list (which is of type Trainee)
            onEditClick = { trainee ->
                editTrainee(trainee)
            },
            onDeleteClick = { trainee ->
                showDeleteConfirmationDialog(trainee)
            },
            onStatusToggle = { trainee ->
                toggleTraineeStatusInFirebase(trainee)
            }
            // onVerificationToggle is removed as Trainee model doesn't have isVerified
        )
        trainersRecyclerView.adapter = trainersAdapter
    }

    private fun loadAndListenForTrainees() {
        progressBar.visibility = View.VISIBLE
        emptyStateLayout.visibility = View.GONE
        trainersRecyclerView.visibility = View.GONE // Hide RV while loading

        // Remove any existing listener before adding a new one to prevent multiple listeners
        if (traineesValueEventListener != null) {
            database.removeEventListener(traineesValueEventListener!!)
        }

        traineesValueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                progressBar.visibility = View.GONE
                originalTraineesList.clear()

                // ****** ADD THESE LOGS ******
                Log.d(TAG, "onDataChange - Firebase path: ${snapshot.ref.toString()}")
                Log.d(TAG, "onDataChange - Raw snapshot value (entire node): ${snapshot.value}")
                // ***************************

                if (snapshot.exists()) {
                    // Log.d(TAG, "onDataChange - Snapshot has ${snapshot.childrenCount} children.") // Optional log

                    snapshot.children.forEach { dataSnapshot ->
                        // ****** ADD THESE LOGS ******
                        Log.d(TAG, "onDataChange - Processing child key: ${dataSnapshot.key}")
                        Log.d(TAG, "onDataChange - Child data (raw JSON): ${dataSnapshot.value}")
                        // ***************************

                        try {
                            // VVVVVV  CORRECTED PART VVVVVV
                            val trainee = dataSnapshot.getValue(Trainee::class.java)
                            // ^^^^^^  CORRECTED PART ^^^^^^

                            if (trainee != null) {
                                originalTraineesList.add(trainee)
                                // ****** ADD SUCCESS LOG ******
                                Log.d(TAG, "onDataChange - SUCCESS Deserialized: ${trainee.name}, isActive: ${trainee.isActive}, id: ${trainee.id}")
                            } else {
                                Log.w(TAG, "onDataChange - Deserialized trainee is NULL for key: ${dataSnapshot.key} with raw data: ${dataSnapshot.value}")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "onDataChange - ERROR deserializing trainee for key: ${dataSnapshot.key} with raw data: ${dataSnapshot.value}", e)
                        }
                    }
                    // This log is good, keep it.
                    Log.d(TAG, "Trainees loaded from Firebase (originalTraineesList size): ${originalTraineesList.size}")
                } else {
                    Log.d(TAG, "No trainees found in Firebase at path /trainees.")
                }
                filterTrainees(searchEditText.text.toString()) // Apply current search/filter
            }

            override fun onCancelled(error: DatabaseError) {
                progressBar.visibility = View.GONE
                Log.e(TAG, "Firebase data loading cancelled or failed.", error.toException())
                Toast.makeText(this@TrainersActivity, "Error loading data: ${error.message}", Toast.LENGTH_LONG).show()
                updateEmptyState() // Still update UI to show empty state if error
            }
        }


        database.addValueEventListener(traineesValueEventListener!!)
    }

    private fun filterTrainees(query: String) {
        filteredTraineesList.clear()
        if (query.isEmpty()) {
            filteredTraineesList.addAll(originalTraineesList.sortedBy { it.name }) // Optional: sort
        } else {
            val lowerCaseQuery = query.lowercase().trim()
            filteredTraineesList.addAll(
                originalTraineesList.filter { trainee ->
                    trainee.name.lowercase().contains(lowerCaseQuery) ||
                            trainee.email.lowercase().contains(lowerCaseQuery) ||
                            trainee.specialty.lowercase().contains(lowerCaseQuery) // Use 'specialty'
                }.sortedBy { it.name } // Optional: sort filtered results
            )
        }
        trainersAdapter.updateTrainees(filteredTraineesList) // Use adapter's update method
        updateEmptyState()
    }

    private fun updateEmptyState() {
        if (filteredTraineesList.isEmpty()) {
            emptyStateLayout.visibility = View.VISIBLE
            trainersRecyclerView.visibility = View.GONE
        } else {
            emptyStateLayout.visibility = View.GONE
            trainersRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun editTrainee(trainee: Trainee) {
        if (trainee.id.isEmpty()) {
            Toast.makeText(this, "Cannot edit trainee: Missing ID.", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Attempted to edit trainee with empty ID: ${trainee.name}")
            return
        }
        val intent = Intent(this, AddEditTrainerActivity::class.java)
        // AddEditTrainerActivity expects "EXTRA_TRAINEE_ID"
        intent.putExtra(AddEditTrainerActivity.EXTRA_TRAINEE_ID, trainee.id)
        addEditTraineeResultLauncher.launch(intent)
    }

    private fun showDeleteConfirmationDialog(trainee: Trainee) {
        if (trainee.id.isEmpty()) {
            Toast.makeText(this, "Cannot delete trainee: Missing ID.", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Attempted to delete trainee with empty ID: ${trainee.name}")
            return
        }
        AlertDialog.Builder(this)
            .setTitle("Delete Trainee")
            .setMessage("Are you sure you want to delete '${trainee.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                deleteTraineeFromFirebase(trainee)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteTraineeFromFirebase(trainee: Trainee) {
        progressBar.visibility = View.VISIBLE
        database.child(trainee.id).removeValue()
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                Log.d(TAG, "Trainee '${trainee.name}' deleted successfully from Firebase.")
                Toast.makeText(this, "'${trainee.name}' deleted.", Toast.LENGTH_SHORT).show()
                // The ValueEventListener will automatically refresh the list.
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Log.e(TAG, "Error deleting trainee '${trainee.name}' from Firebase.", e)
                Toast.makeText(this, "Error deleting: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun toggleTraineeStatusInFirebase(trainee: Trainee) {
        if (trainee.id.isEmpty()) {
            Toast.makeText(this, "Cannot toggle status: Missing ID.", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Attempted to toggle status for trainee with empty ID: ${trainee.name}")
            return
        }
        val newStatus = !trainee.isActive // The statusSwitch in adapter already gives the new desired state
        val updatedData = mapOf("isActive" to newStatus)

        progressBar.visibility = View.VISIBLE
        database.child(trainee.id).updateChildren(updatedData)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                val statusMessage = if (newStatus) "activated" else "deactivated"
                Log.d(TAG, "Trainee '${trainee.name}' status $statusMessage in Firebase.")
                Toast.makeText(this, "'${trainee.name}' $statusMessage.", Toast.LENGTH_SHORT).show()
                // ValueEventListener will refresh. If you want immediate UI feedback before DB confirms:
                // val index = originalTraineesList.indexOfFirst { it.id == trainee.id }
                // if (index != -1) {
                //    originalTraineesList[index] = originalTraineesList[index].copy(isActive = newStatus)
                //    filterTrainees(searchEditText.text.toString())
                // }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Log.e(TAG, "Error updating status for '${trainee.name}' in Firebase.", e)
                Toast.makeText(this, "Error updating status: ${e.message}", Toast.LENGTH_SHORT).show()
                // Optional: Revert the switch in the adapter if the DB update fails.
                // This requires finding the item and notifying the adapter.
                // trainersAdapter.notifyItemChanged(filteredTraineesList.indexOfFirst { it.id == trainee.id })
            }
    }

    override fun onResume() {
        super.onResume()
        loadAndListenForTrainees() // Start listening when activity is active
    }

    override fun onPause() {
        super.onPause()
        // Stop listening when activity is not active to save resources
        if (traineesValueEventListener != null) {
            database.removeEventListener(traineesValueEventListener!!)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Final cleanup
        if (traineesValueEventListener != null) {
            database.removeEventListener(traineesValueEventListener!!)
            traineesValueEventListener = null
        }
    }
}



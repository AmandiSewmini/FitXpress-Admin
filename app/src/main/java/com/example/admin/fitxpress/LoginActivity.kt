package com.example.admin.fitxpress

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button


    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Companion object for logging tag
    companion object {
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupViews()
        setupClickListeners()
    }

    private fun setupViews() {
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
    }

    private fun setupClickListeners() {
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()


            if (validateInput(email, password)) {
                // Input is valid, now attempt Firebase login
                loginButton.isEnabled = false // Disable button to prevent multiple clicks
                signInAdmin(email, password)
            }
        }
    }

    private fun signInAdmin(email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success")
                    val firebaseUser = mAuth.currentUser
                    firebaseUser?.let {
                        // User successfully signed in with Firebase Auth, now check if they are an admin
                        checkIfAdmin(it.uid)
                    } ?: run {
                        // This case should ideally not happen if task.isSuccessful is true
                        Log.e(TAG, "Login successful but FirebaseUser is null.")
                        Toast.makeText(baseContext, "Login error. Please try again.", Toast.LENGTH_SHORT).show()
                        loginButton.isEnabled = true // Re-enable button
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext, "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    loginButton.isEnabled = true // Re-enable button
                }
            }
    }
    private fun checkIfAdmin(uid: String) {
        db.collection("admins").document(uid).get()
            .addOnSuccessListener { document ->
                loginButton.isEnabled = true
                if (document != null && document.exists()) {
                    // Check if 'isAdmin' field exists and is true, or just document.exists() if that's your logic
                    val isAdmin = document.getBoolean("isAdmin") // Example: Assuming you have an 'isAdmin' boolean field
                    if (isAdmin == true) { // Explicitly check if it's true
                        Log.d(TAG, "User $uid is an admin.")
                        Toast.makeText(this, "Admin Login Successful!", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this, DashboardActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        Log.d(TAG, "User $uid is in admins collection but isAdmin is not true (or field missing). Access denied.")
                        Toast.makeText(this, "Access Denied. Not an authorized admin.", Toast.LENGTH_LONG).show()
                        mAuth.signOut()
                    }
                } else {
                    Log.d(TAG, "User $uid is NOT in admins collection. Access denied.")
                    Toast.makeText(this, "Access Denied. Not an authorized admin.", Toast.LENGTH_LONG).show()
                    mAuth.signOut()
                }
            }
            .addOnFailureListener { exception ->
                loginButton.isEnabled = true
                Log.e(TAG, "Error checking admin status for $uid:", exception)
                Toast.makeText(this, "Error checking admin status: ${exception.message}", Toast.LENGTH_LONG).show()
                mAuth.signOut()
            }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            emailEditText.error = "Email is required"
            emailEditText.requestFocus()
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.error = "Please enter a valid email"
            emailEditText.requestFocus()
            return false
        }

        if (password.isEmpty()) {
            passwordEditText.error = "Password is required"
            passwordEditText.requestFocus()
            return false
        }
        // You could add password length validation here if desired
        // if (password.length < 6) {
        //     passwordEditText.error = "Password must be at least 6 characters"
        // passwordEditText.requestFocus()
        //     return false
        // }

        return true
    }
    // Optional: Check if a user is already signed in when the activity starts
    // This is useful if the app was closed and reopened, and the admin was already logged in.
//    override fun onStart() {
//        super.onStart()
//        val currentUser = mAuth.currentUser
//        if (currentUser != null) {
//            // If user is already logged in, verify they are still an admin
//            // This prevents issues if their admin status was revoked while they were logged out
//            Log.d(TAG, "User ${currentUser.uid} already signed in. Verifying admin status...")
//            // You might want to show a loading indicator here
//            loginButton.isEnabled = false // Temporarily disable login button
//            checkIfAdmin(currentUser.uid)
//        }
//    }
}

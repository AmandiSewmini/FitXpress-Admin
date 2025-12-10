package com.example.admin.fitxpress

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.admin.fitxpress.models.User
import com.example.admin.fitxpress.adapters.UsersAdapter

// Removed: import com.google.android.material.floatingactionbutton.FloatingActionButton

class UsersActivity : AppCompatActivity() {

    private lateinit var usersRecyclerView: RecyclerView
    private lateinit var usersAdapter: UsersAdapter
    private lateinit var backButton: ImageView
    private lateinit var searchEditText: EditText
    // Removed: private lateinit var addUserFab: FloatingActionButton

    private val dummyUsers = mutableListOf(
        User(
            id = "user1",
            name = "Alice Smith",
            email = "alice.smith@example.com",
            phone = "+94711234567",
            status = "Active",
            totalOrders = 15,
            totalSpent = 25000.0,
            joinDate = "Jan 15, 2023",
            lastLogin = "Dec 20, 2024",
            isVerified = true
        ),
        User(
            id = "user2",
            name = "Bob Johnson",
            email = "bob.j@example.com",
            phone = "+94779876543",
            status = "Inactive",
            totalOrders = 5,
            totalSpent = 8000.0,
            joinDate = "Mar 01, 2023",
            lastLogin = "Nov 10, 2024",
            isVerified = false
        ),
        User(
            id = "user3",
            name = "Charlie Brown",
            email = "charlie.b@example.com",
            phone = "+94701122334",
            status = "Active",
            totalOrders = 22,
            totalSpent = 45000.0,
            joinDate = "Jun 20, 2022",
            lastLogin = "Dec 25, 2024",
            isVerified = true
        ),
        User(
            id = "user4",
            name = "Diana Prince",
            email = "diana.p@example.com",
            phone = "+94765544332",
            status = "Active",
            totalOrders = 8,
            totalSpent = 12000.0,
            joinDate = "Sep 10, 2023",
            lastLogin = "Dec 28, 2024",
            isVerified = true
        ),
        User(
            id = "user5",
            name = "Eve Adams",
            email = "eve.a@example.com",
            phone = "+94728765432",
            status = "Inactive",
            totalOrders = 3,
            totalSpent = 3500.0,
            joinDate = "Apr 05, 2024",
            lastLogin = "Oct 01, 2024",
            isVerified = false
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users)

        backButton = findViewById(R.id.backButton)
        searchEditText = findViewById(R.id.searchEditText)
        usersRecyclerView = findViewById(R.id.usersRecyclerView)
        // Removed: addUserFab = findViewById(R.id.addUserFab)

        setupRecyclerView()
        setupListeners() // Re-added setupListeners as it contains backButton and searchEditText listeners
    }

    private fun setupRecyclerView() {
        usersRecyclerView.layoutManager = LinearLayoutManager(this)
        usersAdapter = UsersAdapter(
            users = dummyUsers,
            onUserClick = { user ->
                // Handle user item click, e.g., open user details activity
                Toast.makeText(this, "Clicked on user: ${user.name}", Toast.LENGTH_SHORT).show()
                // val intent = Intent(this, UserDetailsActivity::class.java)
                // intent.putExtra("user", user)
                // startActivity(intent)
            },
            onViewOrdersClick = { user ->
                // Handle view orders button click
                Toast.makeText(this, "View orders for: ${user.name}", Toast.LENGTH_SHORT).show()
                // val intent = Intent(this, UserOrdersActivity::class.java)
                // intent.putExtra("userId", user.id)
                // startActivity(intent)
            },
            onDeleteClick = { user ->
                // Handle delete button click
                Toast.makeText(this, "Delete user: ${user.name}", Toast.LENGTH_SHORT).show()
                // Implement actual delete logic here (e.g., Firebase delete)
                // For dummy data, you can remove it from the list:
                dummyUsers.remove(user)
                usersAdapter.notifyDataSetChanged()
            },
            onStatusChange = { user, isChecked ->
                // Handle status switch change
                val newStatus = if (isChecked) "Active" else "Inactive"
                Toast.makeText(this, "${user.name} status changed to $newStatus", Toast.LENGTH_SHORT).show()
                // Update status in your data source (e.g., Firebase)
            }
        )
        usersRecyclerView.adapter = usersAdapter
    }

    private fun setupListeners() {
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Removed addUserFab.setOnClickListener as the button is no longer in the layout

        // Search functionality (not implemented for dummy data, but view is ready)
        searchEditText.setOnEditorActionListener { v, actionId, event ->
            // Handle search action if needed
            false
        }
    }
}

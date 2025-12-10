package com.example.admin.fitxpress

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.admin.fitxpress.adapters.DashboardAdapter
import com.example.admin.fitxpress.models.DashboardItem
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth

class DashboardActivity : AppCompatActivity() {


    private var toolbar: MaterialToolbar? = null
    private var dashboardRecyclerView: RecyclerView? = null
    private var dashboardAdapter: DashboardAdapter? = null
    private var totalOrdersText: TextView? = null
    private var totalUsersText: TextView? = null
    private var totalRevenueText: TextView? = null
    private var pendingOrdersText: TextView? = null
    private var notificationIcon: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {

            setContentView(R.layout.activity_dashboard)

            // Initialize all views
            initializeAllViews()

            // Setup UI components
            setupAllComponents()

            // Load data - will load default/dummy data
            loadDashboardData()

        } catch (e: Exception) {
            handleFatalError("Failed to initialize activity", e)
        }
    }


    private fun initializeAllViews() {
        try {
            // Find all views with null safety
            toolbar = findViewById(R.id.toolbar)
            dashboardRecyclerView = findViewById(R.id.dashboardRecyclerView)
            totalOrdersText = findViewById(R.id.totalOrdersText)
            totalUsersText = findViewById(R.id.totalUsersText)
            totalRevenueText = findViewById(R.id.totalRevenueText)
            pendingOrdersText = findViewById(R.id.pendingOrdersText)
            notificationIcon = findViewById(R.id.notificationIcon)

            // Verify critical views are found
            if (toolbar == null || dashboardRecyclerView == null) {
                throw Exception("Critical views not found in layout")
            }

        } catch (e: Exception) {
            throw Exception("View initialization failed: ${e.message}")
        }
    }

    private fun setupAllComponents() {
        try {
            setupToolbar()
            setupRecyclerView()
            setupNotificationIcon()
        } catch (e: Exception) {
            Toast.makeText(this, "Setup failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupToolbar() {
        toolbar?.let { tb ->
            try {
                setSupportActionBar(tb)
                supportActionBar?.apply {
                    title = "FitXpress Admin"
                    setDisplayShowTitleEnabled(true)
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Toolbar setup failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        dashboardRecyclerView?.let { recyclerView ->
            try {
                // Set layout manager
                recyclerView.layoutManager = GridLayoutManager(this, 2)

                // Create dashboard items with safe class references
                val dashboardItems = createDashboardItems()

                // Create and set adapter
                dashboardAdapter = DashboardAdapter(dashboardItems) { item ->
                    handleDashboardItemClick(item)
                }

                recyclerView.adapter = dashboardAdapter

            } catch (e: Exception) {
                Toast.makeText(this, "RecyclerView setup failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createDashboardItems(): List<DashboardItem> {
        return try {
            listOf(
                DashboardItem("Products", R.drawable.ic_products, ProductsActivity::class.java),
                DashboardItem("Categories", R.drawable.ic_categories, CategoriesActivity::class.java),
                DashboardItem("Trainers", R.drawable.ic_trainers, TrainersActivity::class.java),
                DashboardItem("Orders", R.drawable.ic_orders, OrdersActivity::class.java),
                DashboardItem("Users", R.drawable.ic_users, UsersActivity::class.java),
                DashboardItem("Notifications", R.drawable.ic_notifications, NotificationsActivity::class.java)
            )
        } catch (e: Exception) {
            // Return empty list if there's an error
            emptyList()
        }
    }

    private fun handleDashboardItemClick(item: DashboardItem) {
        try {
            val intent = Intent(this, item.activityClass)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "${item.title} feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupNotificationIcon() {
        notificationIcon?.setOnClickListener {
            try {
                startActivity(Intent(this, NotificationsActivity::class.java))
            } catch (e: Exception) {
                Toast.makeText(this, "Notifications coming soon!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadDashboardData() {
        showLoadingState()
        showDefaultData() // Show default data instead of loading from Firebase
        //Toast.makeText(this, "Dummy dashboard data loaded.", Toast.LENGTH_SHORT).show()
    }

    private fun showLoadingState() {
        totalOrdersText?.text = "..."
        totalUsersText?.text = "..."
        totalRevenueText?.text = "..."
        pendingOrdersText?.text = "..."
    }

    private fun showDefaultData() {
        totalOrdersText?.text = "156" // Dummy data
        totalUsersText?.text = "89" // Dummy data
        totalRevenueText?.text = "Rs.1000" // Dummy data
        pendingOrdersText?.text = "12" // Dummy data
    }

    private fun redirectToLogin() {
        try {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            handleFatalError("Failed to redirect to login", e)
        }
    }

    private fun handleFatalError(message: String, exception: Exception) {
        Toast.makeText(this, "$message: ${exception.message}", Toast.LENGTH_LONG).show()
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return try {
            menuInflater.inflate(R.menu.main_menu, menu)
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                showLogoutDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLogoutDialog() {
        try {
            AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout") { _, _ ->
                    performLogout() // This will now just redirect to login
                }
                .setNegativeButton("Cancel", null)
                .show()
        } catch (e: Exception) {
            performLogout()
        }
    }

    private fun performLogout() {
        try {
            FirebaseAuth.getInstance().signOut()
            redirectToLogin()
            Toast.makeText(this, "Logged out (Firebase bypassed).", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Logout failed: ${e.message}", Toast.LENGTH_SHORT).show()
            redirectToLogin()
        }
    }

    override fun onResume() {
        super.onResume()
        try {

            loadDashboardData() // Reloads dummy data
        } catch (e: Exception) {
            Toast.makeText(this, "Resume failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

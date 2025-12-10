package com.example.admin.fitxpress

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.admin.fitxpress.adapters.OrdersAdapter
import com.example.admin.fitxpress.models.Order
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class OrdersActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var ordersRecyclerView: RecyclerView
    private lateinit var ordersAdapter: OrdersAdapter
    private lateinit var tabLayout: TabLayout
    private lateinit var searchEditText: EditText
    private lateinit var backButton: ImageView

    private val ordersList = mutableListOf<Order>()
    private val filteredOrdersList = mutableListOf<Order>()
    private var currentFilter = "all"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders)

        db = FirebaseFirestore.getInstance()
        setupViews()
        setupTabs()
        loadOrders()
    }

    private fun setupViews() {
        ordersRecyclerView = findViewById(R.id.ordersRecyclerView)
        tabLayout = findViewById(R.id.tabLayout)
        searchEditText = findViewById(R.id.searchEditText)
        backButton = findViewById(R.id.backButton)

        ordersRecyclerView.layoutManager = LinearLayoutManager(this)

        ordersAdapter = OrdersAdapter(filteredOrdersList) { order, action ->
            when (action) {
                "update_status" -> showStatusUpdateDialog(order)
                "view_details" -> viewOrderDetails(order)
            }
        }
        ordersRecyclerView.adapter = ordersAdapter

        backButton.setOnClickListener {
            finish()
        }

        searchEditText.addTextChangedListener { text ->
            filterOrders(text.toString())
        }
    }

    private fun setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("All"))
        tabLayout.addTab(tabLayout.newTab().setText("Pending"))
        tabLayout.addTab(tabLayout.newTab().setText("Processing"))
        tabLayout.addTab(tabLayout.newTab().setText("Shipped"))
        tabLayout.addTab(tabLayout.newTab().setText("Delivered"))
        tabLayout.addTab(tabLayout.newTab().setText("Cancelled"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentFilter = when (tab?.position) {
                    0 -> "all"
                    1 -> "pending"
                    2 -> "processing"
                    3 -> "shipped"
                    4 -> "delivered"
                    5 -> "cancelled"
                    else -> "all"
                }
                filterOrders(searchEditText.text.toString())
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun loadOrders() {
        // Create sample orders for testing
        createSampleOrders()

        db.collection("orders")
            .orderBy("orderDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(this, "Error loading orders: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                ordersList.clear()
                snapshot?.documents?.forEach { doc ->
                    val order = doc.toObject(Order::class.java)
                    order?.let {
                        it.id = doc.id
                        ordersList.add(it)
                    }
                }

                filterOrders(searchEditText.text.toString())
            }
    }

    private fun createSampleOrders() {
        // Create sample orders for testing
        val sampleOrders = listOf(
            Order(
                id = "ORD001",
                userId = "user1",
                userName = "John Doe",
                userEmail = "john@example.com",
                total = 2500.0,
                status = "pending",
                orderDate = System.currentTimeMillis() - 86400000, // 1 day ago
                deliveryAddress = "123 Main St, Colombo",
                paymentMethod = "Card"
            ),
            Order(
                id = "ORD002",
                userId = "user2",
                userName = "Jane Smith",
                userEmail = "jane@example.com",
                total = 1800.0,
                status = "processing",
                orderDate = System.currentTimeMillis() - 172800000, // 2 days ago
                deliveryAddress = "456 Oak Ave, Kandy",
                paymentMethod = "Cash"
            ),
            Order(
                id = "ORD003",
                userId = "user3",
                userName = "Mike Johnson",
                userEmail = "mike@example.com",
                total = 3200.0,
                status = "shipped",
                orderDate = System.currentTimeMillis() - 259200000, // 3 days ago
                deliveryAddress = "789 Pine Rd, Galle",
                paymentMethod = "Card"
            ),
            Order(
                id = "ORD004",
                userId = "user4",
                userName = "Sarah Wilson",
                userEmail = "sarah@example.com",
                total = 1500.0,
                status = "delivered",
                orderDate = System.currentTimeMillis() - 345600000, // 4 days ago
                deliveryAddress = "321 Elm St, Negombo",
                paymentMethod = "Online"
            )
        )

        ordersList.addAll(sampleOrders)
        filterOrders("")
    }

    private fun filterOrders(query: String) {
        filteredOrdersList.clear()

        var filteredByStatus = if (currentFilter == "all") {
            ordersList
        } else {
            ordersList.filter { it.status == currentFilter }
        }

        if (query.isEmpty()) {
            filteredOrdersList.addAll(filteredByStatus)
        } else {
            filteredOrdersList.addAll(
                filteredByStatus.filter {
                    it.userName.contains(query, ignoreCase = true) ||
                            it.userEmail.contains(query, ignoreCase = true) ||
                            it.id.contains(query, ignoreCase = true)
                }
            )
        }

        ordersAdapter.notifyDataSetChanged()
    }

    private fun showStatusUpdateDialog(order: Order) {
        val statuses = arrayOf("pending", "processing", "shipped", "delivered", "cancelled")
        val statusNames = arrayOf("Pending", "Processing", "Shipped", "Delivered", "Cancelled")
        val currentIndex = statuses.indexOf(order.status)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Update Order Status")
            .setSingleChoiceItems(statusNames, currentIndex) { dialog, which ->
                val newStatus = statuses[which]
                updateOrderStatus(order, newStatus)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateOrderStatus(order: Order, newStatus: String) {
        db.collection("orders").document(order.id)
            .update("status", newStatus)
            .addOnSuccessListener {
                Toast.makeText(this, "Order status updated to $newStatus", Toast.LENGTH_SHORT).show()
                // Update local list
                val index = ordersList.indexOfFirst { it.id == order.id }
                if (index != -1) {
                    ordersList[index] = ordersList[index].copy(status = newStatus)
                    filterOrders(searchEditText.text.toString())
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error updating status: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun viewOrderDetails(order: Order) {
        // For now, show a toast. You can create OrderDetailsActivity later
        Toast.makeText(this, "Opening details for Order #${order.id}", Toast.LENGTH_SHORT).show()

        // Uncomment when OrderDetailsActivity is created:
        // val intent = Intent(this, OrderDetailsActivity::class.java)
        // intent.putExtra("order", order)
        // startActivity(intent)
    }
}

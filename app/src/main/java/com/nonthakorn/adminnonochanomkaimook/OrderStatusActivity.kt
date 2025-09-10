package com.nonthakorn.adminnonochanomkaimook

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Order Status Enum
enum class OrderStatus {
    PROCESSING,
    COMPLETED,
    CANCELLED
}

// Order Item Data Model
data class OrderItem(
    val id: String,
    val menuName: String,
    val cupSize: String,
    val toppings: String,
    val orderTime: Date,
    var status: OrderStatus,
    val imageResource: Int
) {
    fun getFormattedTime(): String {
        val formatter = SimpleDateFormat("HH:mm - dd MMM yyyy", Locale("th", "TH"))
        return formatter.format(orderTime)
    }

    fun getStatusText(): String {
        return when (status) {
            OrderStatus.PROCESSING -> "กำลังดำเนินการ"
            OrderStatus.COMPLETED -> "เสร็จสิ้น"
            OrderStatus.CANCELLED -> "ยกเลิก"
        }
    }

    fun getStatusColor(): Int {
        return when (status) {
            OrderStatus.PROCESSING -> R.color.status_processing
            OrderStatus.COMPLETED -> R.color.status_completed
            OrderStatus.CANCELLED -> R.color.status_cancelled
        }
    }
}

// Order ViewHolder
class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val imageOrder: ImageView = itemView.findViewById(R.id.imageOrder)
    private val tvOrderId: TextView = itemView.findViewById(R.id.tvOrderId)
    private val tvOrderTime: TextView = itemView.findViewById(R.id.tvOrderTime)
    private val tvMenuName: TextView = itemView.findViewById(R.id.tvMenuName)
    private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
    private val tvCupSize: TextView = itemView.findViewById(R.id.tvCupSize)
    private val tvToppings: TextView = itemView.findViewById(R.id.tvToppings)
    private val btnComplete: Button = itemView.findViewById(R.id.btnComplete)
    private val btnProcessing: Button = itemView.findViewById(R.id.btnProcessing)
    private val btnCancel: Button = itemView.findViewById(R.id.btnCancel)

    fun bind(
        orderItem: OrderItem,
        position: Int,
        onStatusChanged: (OrderItem, OrderStatus) -> Unit
    ) {
        // Set basic information
        imageOrder.setImageResource(orderItem.imageResource)
        tvOrderId.text = "Order #${String.format("%03d", position + 1)}"
        tvOrderTime.text = orderItem.getFormattedTime()
        tvMenuName.text = orderItem.menuName
        tvCupSize.text = orderItem.cupSize
        tvToppings.text = if (orderItem.toppings.isNotEmpty()) orderItem.toppings else "ไม่มี"

        // Set status
        updateStatusDisplay(orderItem)

        // Set button click listeners
        btnComplete.setOnClickListener {
            if (orderItem.status != OrderStatus.COMPLETED) {
                onStatusChanged(orderItem, OrderStatus.COMPLETED)
            }
        }

        btnProcessing.setOnClickListener {
            if (orderItem.status != OrderStatus.PROCESSING) {
                onStatusChanged(orderItem, OrderStatus.PROCESSING)
            }
        }

        btnCancel.setOnClickListener {
            if (orderItem.status != OrderStatus.CANCELLED) {
                onStatusChanged(orderItem, OrderStatus.CANCELLED)
            }
        }

        // Update button states
        updateButtonStates(orderItem)
    }

    private fun updateStatusDisplay(orderItem: OrderItem) {
        tvStatus.text = orderItem.getStatusText()
        val statusColor = ContextCompat.getColor(itemView.context, orderItem.getStatusColor())
        tvStatus.setBackgroundColor(statusColor)
    }

    private fun updateButtonStates(orderItem: OrderItem) {
        // Reset all buttons to normal state
        resetButtonStates()

        // Highlight current status button
        when (orderItem.status) {
            OrderStatus.PROCESSING -> {
                btnProcessing.alpha = 1.0f
                btnProcessing.isEnabled = false
            }
            OrderStatus.COMPLETED -> {
                btnComplete.alpha = 1.0f
                btnComplete.isEnabled = false
            }
            OrderStatus.CANCELLED -> {
                btnCancel.alpha = 1.0f
                btnCancel.isEnabled = false
            }
        }
    }

    private fun resetButtonStates() {
        btnComplete.alpha = 0.7f
        btnComplete.isEnabled = true
        btnProcessing.alpha = 0.7f
        btnProcessing.isEnabled = true
        btnCancel.alpha = 0.7f
        btnCancel.isEnabled = true
    }
}

// Order Adapter
class OrderAdapter(
    private var orderItems: List<OrderItem>,
    private val onStatusChanged: (OrderItem, OrderStatus) -> Unit
) : RecyclerView.Adapter<OrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orderItems[position], position, onStatusChanged)
    }

    override fun getItemCount(): Int = orderItems.size

    fun updateOrderItems(newItems: List<OrderItem>) {
        orderItems = newItems
        notifyDataSetChanged()
    }

    fun getOrdersByStatus(status: OrderStatus): List<OrderItem> {
        return orderItems.filter { it.status == status }
    }

    fun updateOrderStatus(orderItem: OrderItem, newStatus: OrderStatus) {
        orderItem.status = newStatus
        val position = orderItems.indexOf(orderItem)
        if (position != -1) {
            notifyItemChanged(position)
        }
    }
}

// Main OrderStatusActivity
class OrderStatusActivity : AppCompatActivity() {

    private lateinit var recyclerViewOrders: RecyclerView
    private lateinit var orderAdapter: OrderAdapter

    // Navigation views
    private lateinit var navAnalytics: LinearLayout
    private lateinit var navMenu: LinearLayout
    private lateinit var navDelete: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_order_status)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupRecyclerView()
        loadOrderData()
        setupNavigationListeners()
    }

    private fun initViews() {
        recyclerViewOrders = findViewById(R.id.recyclerViewOrders)
        navAnalytics = findViewById(R.id.nav_analytics)
        navMenu = findViewById(R.id.nav_menu)
        navDelete = findViewById(R.id.nav_delete)
    }

    private fun setupRecyclerView() {
        orderAdapter = OrderAdapter(emptyList()) { orderItem, newStatus ->
            onOrderStatusChanged(orderItem, newStatus)
        }

        recyclerViewOrders.apply {
            adapter = orderAdapter
            layoutManager = LinearLayoutManager(this@OrderStatusActivity)
        }
    }

    private fun loadOrderData() {
        // Sample order data
        val sampleOrders = listOf(
            OrderItem(
                id = "1",
                menuName = "กาแฟลาเต้",
                cupSize = "Large",
                toppings = "Extra Shot, Whipped Cream",
                orderTime = Date(System.currentTimeMillis() - 300000), // 5 minutes ago
                status = OrderStatus.PROCESSING,
                imageResource = R.drawable.logo_nono
            ),
            OrderItem(
                id = "2",
                menuName = "เอสเปรสโซ่",
                cupSize = "Medium",
                toppings = "Sugar",
                orderTime = Date(System.currentTimeMillis() - 600000), // 10 minutes ago
                status = OrderStatus.COMPLETED,
                imageResource = R.drawable.logo_nono
            ),
            OrderItem(
                id = "3",
                menuName = "คาปูชิโน่",
                cupSize = "Small",
                toppings = "Cinnamon Powder",
                orderTime = Date(System.currentTimeMillis() - 900000), // 15 minutes ago
                status = OrderStatus.PROCESSING,
                imageResource = R.drawable.logo_nono
            ),
            OrderItem(
                id = "4",
                menuName = "ชาเขียวลาเต้",
                cupSize = "Large",
                toppings = "Matcha Powder, Honey",
                orderTime = Date(System.currentTimeMillis() - 1200000), // 20 minutes ago
                status = OrderStatus.CANCELLED,
                imageResource = R.drawable.logo_nono
            ),
            OrderItem(
                id = "5",
                menuName = "มอคค่า",
                cupSize = "Medium",
                toppings = "Chocolate Syrup",
                orderTime = Date(System.currentTimeMillis() - 1500000), // 25 minutes ago
                status = OrderStatus.PROCESSING,
                imageResource = R.drawable.logo_nono
            )
        )

        orderAdapter.updateOrderItems(sampleOrders)
    }

    private fun onOrderStatusChanged(orderItem: OrderItem, newStatus: OrderStatus) {
        orderAdapter.updateOrderStatus(orderItem, newStatus)

        val statusMessage = when (newStatus) {
            OrderStatus.PROCESSING -> "เปลี่ยนสถานะเป็น: กำลังดำเนินการ"
            OrderStatus.COMPLETED -> "เปลี่ยนสถานะเป็น: เสร็จสิ้น"
            OrderStatus.CANCELLED -> "เปลี่ยนสถานะเป็น: ยกเลิก"
        }

        Toast.makeText(this, "${orderItem.menuName} - $statusMessage", Toast.LENGTH_SHORT).show()

        // Here you can save the status change to database
        // saveOrderStatusToDatabase(orderItem)
    }

    private fun setupNavigationListeners() {
        navAnalytics.setOnClickListener {
            showOrderAnalytics()
        }

        navMenu.setOnClickListener {
            // Navigate back to menu
            finish()
        }

        navDelete.setOnClickListener {
            clearCompletedOrders()
        }
    }

    private fun showOrderAnalytics() {
        val processingOrders = orderAdapter.getOrdersByStatus(OrderStatus.PROCESSING)
        val completedOrders = orderAdapter.getOrdersByStatus(OrderStatus.COMPLETED)
        val cancelledOrders = orderAdapter.getOrdersByStatus(OrderStatus.CANCELLED)

        val message = buildString {
            append("📊 Order Analytics\n\n")
            append("🟡 กำลังดำเนินการ: ${processingOrders.size} orders\n")
            append("🟢 เสร็จสิ้น: ${completedOrders.size} orders\n")
            append("🔴 ยกเลิก: ${cancelledOrders.size} orders\n")
            append("📋 รวม: ${orderAdapter.itemCount} orders")
        }

        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun clearCompletedOrders() {
        val completedOrders = orderAdapter.getOrdersByStatus(OrderStatus.COMPLETED)
        if (completedOrders.isNotEmpty()) {
            Toast.makeText(this, "ลบออเดอร์ที่เสร็จสิ้นแล้ว ${completedOrders.size} รายการ", Toast.LENGTH_SHORT).show()
            // Implement logic to remove completed orders
        } else {
            Toast.makeText(this, "ไม่มีออเดอร์ที่เสร็จสิ้นให้ลบ", Toast.LENGTH_SHORT).show()
        }
    }
}
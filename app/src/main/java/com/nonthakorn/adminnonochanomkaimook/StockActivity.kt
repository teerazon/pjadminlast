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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// Stock Item Data Model
data class StockItem(
    val id: String,
    val name: String,
    var quantity: Int,
    var originalQuantity: Int = quantity, // เพิ่มเพื่อเก็บค่าเดิม
    val imageResource: Int,
    val minStock: Int = 5, // minimum stock level
    val maxStock: Int = 100 // maximum stock level
) {
    fun isLowStock(): Boolean = quantity <= minStock
    fun isOutOfStock(): Boolean = quantity <= 0
    fun hasChanged(): Boolean = quantity != originalQuantity // ตรวจสอบว่ามีการเปลี่ยนแปลงหรือไม่
}

// Stock ViewHolder
class StockViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val imageProduct: ImageView = itemView.findViewById(R.id.imageProduct)
    private val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
    private val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
    private val btnDecrease: ImageView = itemView.findViewById(R.id.btnDecrease)
    private val btnIncrease: ImageView = itemView.findViewById(R.id.btnIncrease)
    private val btnConfirm: Button = itemView.findViewById(R.id.btnConfirm) // เพิ่มปุ่มตกลง

    fun bind(
        stockItem: StockItem,
        onQuantityChanged: (StockItem, Int) -> Unit,
        onConfirmClicked: (StockItem) -> Unit // เพิ่ม callback สำหรับปุ่มตกลง
    ) {
        imageProduct.setImageResource(stockItem.imageResource)
        tvProductName.text = stockItem.name
        updateQuantityDisplay(stockItem)
        updateConfirmButtonState(stockItem)

        btnIncrease.setOnClickListener {
            if (stockItem.quantity < stockItem.maxStock) {
                stockItem.quantity++
                updateQuantityDisplay(stockItem)
                updateConfirmButtonState(stockItem)
                onQuantityChanged(stockItem, stockItem.quantity)
            } else {
                Toast.makeText(itemView.context, "ไม่สามารถเพิ่มเกินจำนวนสูงสุดได้", Toast.LENGTH_SHORT).show()
            }
        }

        btnDecrease.setOnClickListener {
            if (stockItem.quantity > 0) {
                stockItem.quantity--
                updateQuantityDisplay(stockItem)
                updateConfirmButtonState(stockItem)
                onQuantityChanged(stockItem, stockItem.quantity)
            }
        }

        btnConfirm.setOnClickListener {
            onConfirmClicked(stockItem)
        }
    }

    private fun updateQuantityDisplay(stockItem: StockItem) {
        tvQuantity.text = stockItem.quantity.toString()

        // Change color based on stock level
        when {
            stockItem.isOutOfStock() -> {
                tvQuantity.setTextColor(itemView.context.getColor(android.R.color.holo_red_dark))
            }
            stockItem.isLowStock() -> {
                tvQuantity.setTextColor(itemView.context.getColor(android.R.color.holo_orange_dark))
            }
            else -> {
                tvQuantity.setTextColor(itemView.context.getColor(android.R.color.holo_green_dark))
            }
        }
    }

    private fun updateConfirmButtonState(stockItem: StockItem) {
        // แสดงปุ่มตกลงเฉพาะเมื่อมีการเปลี่ยนแปลง
        if (stockItem.hasChanged()) {
            btnConfirm.visibility = View.VISIBLE
            btnConfirm.text = "ตกลง"
            btnConfirm.alpha = 1.0f
        } else {
            btnConfirm.visibility = View.GONE
        }
    }
}

// Stock Adapter
class StockAdapter(
    private var stockItems: List<StockItem>,
    private val onQuantityChanged: (StockItem, Int) -> Unit,
    private val onConfirmClicked: (StockItem) -> Unit // เพิ่ม callback
) : RecyclerView.Adapter<StockViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_stock, parent, false)
        return StockViewHolder(view)
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        holder.bind(stockItems[position], onQuantityChanged, onConfirmClicked)
    }

    override fun getItemCount(): Int = stockItems.size

    fun updateStockItems(newItems: List<StockItem>) {
        stockItems = newItems
        notifyDataSetChanged()
    }

    fun getLowStockItems(): List<StockItem> {
        return stockItems.filter { it.isLowStock() }
    }

    fun getOutOfStockItems(): List<StockItem> {
        return stockItems.filter { it.isOutOfStock() }
    }

    fun getChangedItems(): List<StockItem> {
        return stockItems.filter { it.hasChanged() }
    }

    fun confirmItemUpdate(stockItem: StockItem) {
        stockItem.originalQuantity = stockItem.quantity
        notifyItemChanged(stockItems.indexOf(stockItem))
    }
}

// Main StockActivity
class StockActivity : AppCompatActivity() {

    private lateinit var recyclerViewStock: RecyclerView
    private lateinit var stockAdapter: StockAdapter

    // Navigation views
    private lateinit var navAnalytics: LinearLayout
    private lateinit var navMenu: LinearLayout
    private lateinit var navDelete: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_stock)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupRecyclerView()
        loadStockData()
        setupNavigationListeners()
    }

    private fun initViews() {
        recyclerViewStock = findViewById(R.id.recyclerViewStock)
        navAnalytics = findViewById(R.id.nav_analytics)
        navMenu = findViewById(R.id.nav_menu)
        navDelete = findViewById(R.id.nav_delete)
    }

    private fun setupRecyclerView() {
        stockAdapter = StockAdapter(
            emptyList(),
            onQuantityChanged = { stockItem, newQuantity ->
                onStockQuantityChanged(stockItem, newQuantity)
            },
            onConfirmClicked = { stockItem ->
                onConfirmStockUpdate(stockItem)
            }
        )

        recyclerViewStock.apply {
            adapter = stockAdapter
            layoutManager = LinearLayoutManager(this@StockActivity)
        }
    }

    private fun loadStockData() {
        // Sample stock data
        val sampleStockItems = listOf(
            StockItem("1", "โกโก้", 0, 0, R.drawable.stock1),
            StockItem("2", "ครีมเทียม", 0, 0, R.drawable.stock2),
            StockItem("3", "สตอร์เบอร์รี่", 0, 0, R.drawable.stock3),
            StockItem("4", "โยเกิร์ด", 0, 0, R.drawable.stock4),
            StockItem("5", "โคล่า", 0, 0, R.drawable.stock5),
            StockItem("6", "น้ำแข็ง", 0, 0, R.drawable.stock6),
            StockItem("7", "น้ำตาล", 0, 0, R.drawable.stock7),
            StockItem("8", "น้ำเชื่อม", 0, 0, R.drawable.stock8),
            StockItem("9", "นมข้น", 0, 0, R.drawable.stock9),
            StockItem("10", "นมสด", 0, 0, R.drawable.stock10),
            StockItem("11", "ผงนมสด", 0, 0, R.drawable.stock11),
            StockItem("12", "ผงชาเย็น", 0, 0, R.drawable.stock12),
            StockItem("13", "ผงแคนตาลูบ", 0, 0, R.drawable.stock13),
            StockItem("14", "ผงชาเขียว", 0, 0, R.drawable.stock14),
            StockItem("15", "ผงเผือก", 0, 0, R.drawable.stock15),
            StockItem("16", "ไข่มุก", 0, 0, R.drawable.stock16),
            StockItem("17", "เยลลี่", 0, 0, R.drawable.stock17)
        )

        stockAdapter.updateStockItems(sampleStockItems)
    }

    private fun onStockQuantityChanged(stockItem: StockItem, newQuantity: Int) {
        // อัปเดตจำนวนสินค้าชั่วคราว (ยังไม่บันทึก)
        // การเช็คสถานะจะทำใน ViewHolder
    }

    private fun onConfirmStockUpdate(stockItem: StockItem) {
        // บันทึกการเปลี่ยนแปลงจริง ๆ
        stockAdapter.confirmItemUpdate(stockItem)

        val message = when {
            stockItem.isOutOfStock() -> "${stockItem.name} หมดสต็อก! อัปเดตเรียบร้อย"
            stockItem.isLowStock() -> "${stockItem.name} สต็อกเหลือน้อย (${stockItem.quantity}) อัปเดตเรียบร้อย"
            else -> "${stockItem.name} อัปเดตจำนวนเป็น ${stockItem.quantity} เรียบร้อย"
        }

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

        // ที่นี่คุณสามารถบันทึกข้อมูลลงฐานข้อมูลได้
        // saveToDatabase(stockItem)
    }

    private fun setupNavigationListeners() {
        navAnalytics.setOnClickListener {
            showAnalytics()
        }

        navMenu.setOnClickListener {
            // ตรวจสอบว่ามีการเปลี่ยนแปลงที่ยังไม่ได้บันทึกหรือไม่
            val changedItems = stockAdapter.getChangedItems()
            if (changedItems.isNotEmpty()) {
                Toast.makeText(this, "มีสินค้า ${changedItems.size} รายการที่ยังไม่ได้บันทึก", Toast.LENGTH_LONG).show()
            } else {
                finish()
            }
        }

        navDelete.setOnClickListener {
            clearLowStockItems()
        }
    }

    private fun showAnalytics() {
        val lowStockItems = stockAdapter.getLowStockItems()
        val outOfStockItems = stockAdapter.getOutOfStockItems()
        val changedItems = stockAdapter.getChangedItems()

        val message = buildString {
            append("📊 Stock Analytics\n\n")
            append("🔴 Out of Stock: ${outOfStockItems.size} items\n")
            append("🟡 Low Stock: ${lowStockItems.size} items\n")
            append("🟢 Normal Stock: ${stockAdapter.itemCount - lowStockItems.size - outOfStockItems.size} items\n")
            append("⏳ Unsaved Changes: ${changedItems.size} items")
        }

        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun clearLowStockItems() {
        val changedItems = stockAdapter.getChangedItems()
        if (changedItems.isNotEmpty()) {
            Toast.makeText(this, "กรุณาบันทึกการเปลี่ยนแปลงก่อน", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Clear low stock items", Toast.LENGTH_SHORT).show()
            // Implement logic to handle low stock items
        }
    }
}
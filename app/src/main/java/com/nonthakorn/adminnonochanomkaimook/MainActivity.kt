package com.nonthakorn.adminnonochanomkaimook

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // ค้นหา Button และ TextView จาก ID ที่กำหนดไว้ใน XML
        val nav_delete: ImageView = findViewById(R.id.nav_delete)

        // ค้นหา Button และ TextView จาก ID ที่กำหนดไว้ใน XML
        val nav_menu: ImageView = findViewById(R.id.nav_menu)

        // ตั้งค่าการคลิกสำหรับปุ่ม
        nav_delete.setOnClickListener {
            // สร้าง Intent เพื่อย้ายไปยัง HomeActivity
            val intent = Intent(this, StockActivity::class.java)
            startActivity(intent)
        }

        nav_menu.setOnClickListener {
            // สร้าง Intent เพื่อย้ายไปยัง HomeActivity
            val intent = Intent(this, OrderStatusActivity::class.java)
            startActivity(intent)
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
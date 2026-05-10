package com.example.shtrih2

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val barcode = intent.getStringExtra("BARCODE") ?: ""
        val product = intent.getStringExtra("PRODUCT") ?: ""

//        findViewById<TextView>(R.id.tvBarcode).text = barcode

        val parts = barcode.split(";")
        if (parts.size >= 4) {
            findViewById<TextView>(R.id.tvId).text = parts[0].trim()
            findViewById<TextView>(R.id.tvSn).text = parts[1].trim()
            findViewById<TextView>(R.id.tvModel).text = parts[2].trim()
            findViewById<TextView>(R.id.tvMac).text = parts[3].trim()
        } else {
            // fallback если формат неожиданный
            findViewById<TextView>(R.id.tvId).text = product
        }

        findViewById<Button>(R.id.btnOk).setOnClickListener { finish() }
        findViewById<ImageView>(R.id.btnClose).setOnClickListener { finish() }
    }
}

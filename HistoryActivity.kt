package com.example.shtrih2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.apache.poi.xssf.usermodel.XSSFWorkbook

class HistoryActivity : AppCompatActivity() {

    private val app by lazy { application as App }
    private val history by lazy { app.history }
    private lateinit var adapter: HistoryAdapter

    private val exportLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                exportToExcel(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val rvHistory = findViewById<RecyclerView>(R.id.rvHistory)
        rvHistory.layoutManager = LinearLayoutManager(this)
        adapter = HistoryAdapter(history)
        rvHistory.adapter = adapter

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<ImageView>(R.id.btnExport).setOnClickListener { startExport() }
        findViewById<Button>(R.id.btnClear).setOnClickListener {
            history.clear()
            adapter.notifyDataSetChanged()
            Toast.makeText(this, "История очищена", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startExport() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_TITLE, "scanned_barcodes.xlsx")
        }
        exportLauncher.launch(intent)
    }

    private fun exportToExcel(uri: Uri) {
        try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("История")

            for ((index, item) in history.withIndex()) {
                val row = sheet.createRow(index)
                val cellA = row.createCell(0)
                cellA.setCellValue((index + 1).toDouble())
                val cellC = row.createCell(2)
                cellC.setCellValue("${item.barcode};${item.value};${item.count}")
            }

            contentResolver.openOutputStream(uri)?.use { outputStream ->
                workbook.write(outputStream)
                workbook.close()
            }

            Toast.makeText(this, "Файл сохранен", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Ошибка сохранения", Toast.LENGTH_LONG).show()
        }
    }
}

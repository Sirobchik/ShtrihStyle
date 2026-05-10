package com.example.shtrih2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook

class MainActivity : AppCompatActivity() {

    private val app by lazy { application as App }
    private val database by lazy { app.database }

    // Лаунчер сканера
    private val scanLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            val code = result.contents
            val product = database[code] ?: "Не найдено"
            app.history.add(HistoryItem(code, product))
            val intent = Intent(this, ResultActivity::class.java).apply {
                putExtra("BARCODE", code)
                putExtra("PRODUCT", product)
            }
            startActivity(intent)
        } else {
            Toast.makeText(this, "Сканирование отменено", Toast.LENGTH_SHORT).show()
        }
    }

    // Лаунчер выбора файла (открывается сразу в Downloads)
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                loadExcel(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnLoad = findViewById<Button>(R.id.btnLoad)
        val btnScan = findViewById<Button>(R.id.btnScan)

        btnLoad.setOnClickListener {
            openFilePicker()
        }

        btnScan.setOnClickListener {
            startScan()
        }

        findViewById<ImageView>(R.id.tab_folder).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        findViewById<ImageView>(R.id.tab_scan).setOnClickListener {
            startScan()
        }

        findViewById<ImageView>(R.id.tab_download).setOnClickListener {
            openFilePicker()
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            val downloadsUri = Uri.parse("content://com.android.externalstorage.documents/document/primary%3ADownload")
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, downloadsUri)
        }
        filePickerLauncher.launch(intent)
    }

    private fun startScan() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(BarcodeFormat.CODE_128.name)
            setPrompt("Наведите камеру на штрих-код")
            setBeepEnabled(true)
            setOrientationLocked(true)
            //setCaptureActivity(PortraitCaptureActivity::class.java)
        }
        scanLauncher.launch(options)
    }


    private fun loadExcel(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val workbook: Workbook = XSSFWorkbook(inputStream)
            val sheet = workbook.getSheetAt(0)

            database.clear()

            for (row in sheet) {
                val barcodeCell = row.getCell(2)
                val nameCell = row.getCell(0)
                try {
                    if (barcodeCell != null && nameCell != null) {
                        val barcode = barcodeCell.stringCellValue.trim()
                        val name = nameCell.numericCellValue.toString()
                        Log.e("System.err","${barcode} ${name}")
                        database[barcode] = name
                    }

                }catch (e: Exception){}

            }

            Toast.makeText(this, "Excel загружен: " + database.size + " записей", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Ошибка загрузки Excel", Toast.LENGTH_LONG).show()
        }
    }
}

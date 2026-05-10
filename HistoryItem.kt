package com.example.shtrih2

data class HistoryItem(
    val barcode: String,
    val value: String,
    val timestamp: Long = System.currentTimeMillis()
)

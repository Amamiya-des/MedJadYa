package com.example.medjadya.data.model

data class RefillItemUi(
    val id: Int,
    val name: String,
    val doseText: String,
    val remain: Int,
    val total: Int
)

enum class StockStatus { OK, LOW, CRITICAL }

fun stockStatus(remain: Int, total: Int): StockStatus {
    if (total <= 0) return StockStatus.OK
    if (remain.toDouble() <= total.toDouble() / 5.0) return StockStatus.CRITICAL
    if (remain.toDouble() <= total.toDouble() / 3.0) return StockStatus.LOW
    return StockStatus.OK
}

fun statusLabel(status: StockStatus): String = when (status) {
    StockStatus.CRITICAL -> "ใกล้หมด"
    StockStatus.LOW -> "เหลือน้อย"
    StockStatus.OK -> ""
}


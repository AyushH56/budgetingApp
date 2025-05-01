package com.example.poegroup4

data class Transaction(
    val amount: Double = 0.0,
    val roundedAmount: Double = 0.0,
    val emergencyFund: Double = 0.0,
    val category: String = "",
    val description: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val date: String = "",
    val photoUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

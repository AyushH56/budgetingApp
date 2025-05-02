package com.example.poegroup4


//Data class for budget
data class Budget(
    val category: String,
    val budgetedAmount: Double,
    val spentAmount: Double,
    val availableAmount: String
)
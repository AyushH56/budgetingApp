package com.example.poegroup4

//Data class for Overview
data class OverviewItem(
    val category: String = "",
    val month: String = "",
    val minBudget: Double = 0.0,
    val maxBudget: Double = 0.0,
    val amountSpent: Double = 0.0
)

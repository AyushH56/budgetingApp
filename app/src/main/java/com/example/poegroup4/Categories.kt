package com.example.poegroup4

//Data class for saving categories to Db
data class Categories(
    var categoryId: String? = null,
    val catName: String = "",
    val catBudget: Double = 0.0,
    val userId: String? = null
)

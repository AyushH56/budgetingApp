package com.example.poegroup4

import android.os.Bundle
import android.widget.RadioGroup
import android.widget.TextView

class AnalyticsActivity : BaseActivity() {

    private lateinit var periodGroup: RadioGroup
    private lateinit var selectedDateText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate into the content frame (BaseActivity provides drawer)
        layoutInflater.inflate(R.layout.activity_analytics, findViewById(R.id.content_frame))

        // Set the title in the top action bar
        supportActionBar?.title = "Analytics"

        // Initialize views
        periodGroup = findViewById(R.id.periodRadioGroup)
        selectedDateText = findViewById(R.id.selectedDateText)

        // You can add logic later, this sets up the screen correctly
    }
}

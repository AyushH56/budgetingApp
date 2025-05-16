package com.example.poegroup4

import android.os.Bundle

class ProgressDashboardActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate layout into drawer's content area
        layoutInflater.inflate(R.layout.activity_progress_dashboard, findViewById(R.id.content_frame))

        // Set title in the top app bar
        supportActionBar?.title = "Progress Dashboard"

        // Initialize UI components later as needed
    }
}

package com.example.poegroup4

import android.os.Bundle

class MyTreeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate layout into drawer's content area
        layoutInflater.inflate(R.layout.activity_my_tree, findViewById(R.id.content_frame))

        // Set title in the top app bar
        supportActionBar?.title = "My Tree"

        // Initialize UI components later as needed
    }
}

package com.example.poegroup4

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegisterNow: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Finds Id's
        etEmail = findViewById(R.id.loginEmail)
        etPassword = findViewById(R.id.loginPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegisterNow = findViewById(R.id.tvRegisterNow)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Save user data
            val sharedPref = getSharedPreferences("UserData", MODE_PRIVATE)
            val registeredEmail = sharedPref.getString("email", null)
            val registeredPassword = sharedPref.getString("password", null)

            //Validation Messages
            if (email == registeredEmail && password == registeredPassword) {
                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                // TODO: Navigate to Home Screen
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Invalid Credentials!", Toast.LENGTH_SHORT).show()
            }
        }

        // Register Now Text
        tvRegisterNow.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()

        }
    }
}
